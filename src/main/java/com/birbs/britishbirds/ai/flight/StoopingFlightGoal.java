package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import com.birbs.britishbirds.entity.raptor.PeregrineFalconEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * The Peregrine Falcon's signature high-speed dive (stoop).
 * Multi-phase state machine: CLIMBING -> TARGETING -> DIVING -> STRIKING -> PULLOUT
 */
public class StoopingFlightGoal extends Goal {
    private final PeregrineFalconEntity falcon;
    private StoopPhase phase;
    private int phaseTicks;
    private int cooldown;
    private LivingEntity prey;
    private Vec3 climbTarget;
    private static final double DETECTION_RANGE = 32.0;
    private static final double STOOP_ATTACK_RANGE = 2.0;
    private static final int STOOP_DAMAGE_MULTIPLIER = 3;
    private static final int TARGETING_TIMEOUT = 200;

    private enum StoopPhase {
        CLIMBING, TARGETING, DIVING, STRIKING, PULLOUT
    }

    public StoopingFlightGoal(PeregrineFalconEntity falcon) {
        this.falcon = falcon;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Must be outdoors
        if (!this.falcon.level().canSeeSky(this.falcon.blockPosition())) {
            return false;
        }
        // 1% chance per tick to initiate a stoop
        if (this.falcon.getRandom().nextFloat() > 0.01f) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != null;
    }

    @Override
    public void start() {
        this.falcon.setFlying(true);
        this.phase = StoopPhase.CLIMBING;
        this.phaseTicks = 0;
        this.prey = null;
        this.falcon.setStooping(false);

        // Set climb target: 30+ blocks above current position
        BlockPos groundPos = findGroundBelow();
        double climbY = groundPos.getY() + 30.0 + this.falcon.getRandom().nextDouble() * 10.0;
        this.climbTarget = new Vec3(
                this.falcon.getX() + (this.falcon.getRandom().nextDouble() - 0.5) * 10.0,
                climbY,
                this.falcon.getZ() + (this.falcon.getRandom().nextDouble() - 0.5) * 10.0
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.phase == null) return;
        this.phaseTicks++;

        switch (this.phase) {
            case CLIMBING -> tickClimbing();
            case TARGETING -> tickTargeting();
            case DIVING -> tickDiving();
            case STRIKING -> tickStriking();
            case PULLOUT -> tickPullout();
        }
    }

    private void tickClimbing() {
        // Fly upward toward climb target
        this.falcon.getNavigation().moveTo(
                this.climbTarget.x, this.climbTarget.y, this.climbTarget.z, 0.5);

        // Add upward velocity boost
        this.falcon.setDeltaMovement(this.falcon.getDeltaMovement().add(0, 0.06, 0));

        // Transition when high enough or after enough time
        if (this.falcon.getY() >= this.climbTarget.y - 2.0 || this.phaseTicks > 200) {
            this.phase = StoopPhase.TARGETING;
            this.phaseTicks = 0;
        }
    }

    private void tickTargeting() {
        // Hover at altitude and look for prey below
        Vec3 motion = this.falcon.getDeltaMovement();
        this.falcon.setDeltaMovement(motion.x * 0.3, 0.01, motion.z * 0.3);

        // Search for prey in a wide area below
        AABB searchBox = new AABB(
                this.falcon.getX() - DETECTION_RANGE, this.falcon.getY() - DETECTION_RANGE,
                this.falcon.getZ() - DETECTION_RANGE,
                this.falcon.getX() + DETECTION_RANGE, this.falcon.getY(),
                this.falcon.getZ() + DETECTION_RANGE
        );

        List<LivingEntity> candidates = this.falcon.level().getEntitiesOfClass(
                LivingEntity.class, searchBox, this::isValidPrey
        );

        if (!candidates.isEmpty()) {
            // Pick closest prey
            this.prey = candidates.stream()
                    .min((a, b) -> Double.compare(
                            this.falcon.distanceToSqr(a), this.falcon.distanceToSqr(b)))
                    .orElse(null);

            if (this.prey != null) {
                this.phase = StoopPhase.DIVING;
                this.phaseTicks = 0;
                this.falcon.setStooping(true);
                return;
            }
        }

        // Abort if no prey found after timeout
        if (this.phaseTicks > TARGETING_TIMEOUT) {
            this.phase = null; // abort, return to soaring
        }
    }

    private void tickDiving() {
        if (this.prey == null || !this.prey.isAlive()) {
            this.phase = StoopPhase.PULLOUT;
            this.phaseTicks = 0;
            this.falcon.setStooping(false);
            return;
        }

        // Look at prey
        this.falcon.getLookControl().setLookAt(this.prey, 60.0f, 60.0f);

        // Calculate dive vector toward prey
        Vec3 toTarget = this.prey.position().subtract(this.falcon.position()).normalize();

        // Apply large downward velocity: -1.5 to -2.0 per tick (scaled)
        double speed = -1.5 - this.falcon.getRandom().nextDouble() * 0.5;
        this.falcon.setDeltaMovement(
                toTarget.x * Math.abs(speed) * 0.5,
                speed,
                toTarget.z * Math.abs(speed) * 0.5
        );

        // Disable navigation during dive
        this.falcon.getNavigation().stop();

        // Check if close enough to strike
        double distSqr = this.falcon.distanceToSqr(this.prey);
        if (distSqr <= STOOP_ATTACK_RANGE * STOOP_ATTACK_RANGE) {
            this.phase = StoopPhase.STRIKING;
            this.phaseTicks = 0;
        }

        // Safety: if we've been diving too long without hitting, pull out
        if (this.phaseTicks > 100) {
            this.phase = StoopPhase.PULLOUT;
            this.phaseTicks = 0;
            this.falcon.setStooping(false);
        }
    }

    private void tickStriking() {
        this.falcon.setStooping(false);

        if (this.prey != null && this.prey.isAlive()) {
            if (this.falcon.level() instanceof ServerLevel serverLevel) {
                // Amplified damage: base attack * stoop multiplier
                float baseDamage = (float) this.falcon.getAttributeValue(
                        net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                float stoopDamage = baseDamage * STOOP_DAMAGE_MULTIPLIER;
                this.prey.hurt(
                        this.falcon.damageSources().mobAttack(this.falcon),
                        stoopDamage
                );
            }
        }

        // Immediately transition to pullout
        this.phase = StoopPhase.PULLOUT;
        this.phaseTicks = 0;
    }

    private void tickPullout() {
        // Briefly climb back up after strike
        this.falcon.setDeltaMovement(this.falcon.getDeltaMovement().add(0, 0.15, 0));

        if (this.phaseTicks > 30) {
            this.phase = null; // Done
        }
    }

    @Override
    public void stop() {
        this.falcon.setStooping(false);
        this.prey = null;
        this.phase = null;
        // Cooldown: 400-800 ticks
        this.cooldown = 400 + this.falcon.getRandom().nextInt(401);
    }

    private boolean isValidPrey(LivingEntity entity) {
        if (!entity.isAlive()) return false;
        if (entity == this.falcon) return false;
        // Chickens are primary prey
        if (entity instanceof Chicken) return true;
        // Small British birds are also prey (smaller than peregrine)
        if (entity instanceof AbstractBritishBird bird) {
            return bird.getBbWidth() < this.falcon.getBbWidth();
        }
        return false;
    }

    private BlockPos findGroundBelow() {
        BlockPos pos = this.falcon.blockPosition();
        for (int y = pos.getY(); y > pos.getY() - 60; y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            if (!this.falcon.level().getBlockState(check).isAir()) {
                return check;
            }
        }
        return pos;
    }
}
