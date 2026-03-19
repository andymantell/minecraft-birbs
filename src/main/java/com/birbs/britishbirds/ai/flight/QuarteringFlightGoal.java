package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.ai.BirdAIUtils;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Barn Owl's quartering hunting flight: slow, low, wavering flight over open ground.
 * When prey is detected, the owl hovers briefly then dives to attack.
 * Uses direct velocity control (setDeltaMovement) for smooth, wavering flight.
 */
public class QuarteringFlightGoal extends Goal {
    private final AbstractFlyingBird bird;
    private Vec3 target;
    private int flightTicks;
    private int maxFlightTicks;
    private int cooldown;
    private boolean hovering;
    private int hoverTicks;
    private LivingEntity preyTarget;

    public QuarteringFlightGoal(AbstractFlyingBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Active at night only
        long time = this.bird.level().getOverworldClockTime() % 24000;
        if (time < 12000) {
            return false;
        }
        // 2% chance per tick
        if (this.bird.getRandom().nextFloat() > 0.02f) {
            return false;
        }
        this.target = findQuarteringTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.hovering && this.hoverTicks > 20) {
            return false; // Done hovering, attack phase complete
        }
        if (this.flightTicks >= this.maxFlightTicks && !this.hovering) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.bird.setFlying(true);  // Enables noGravity
        this.flightTicks = 0;
        this.maxFlightTicks = 200 + this.bird.getRandom().nextInt(200);
        this.hovering = false;
        this.hoverTicks = 0;
        this.preyTarget = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.flightTicks++;

        if (this.hovering) {
            // Hover in place briefly before diving
            this.hoverTicks++;
            // Nearly stop horizontal, maintain altitude
            Vec3 motion = this.bird.getDeltaMovement();
            this.bird.setDeltaMovement(motion.x * 0.1, 0.01, motion.z * 0.1);

            if (this.hoverTicks >= 15 && this.preyTarget != null && this.preyTarget.isAlive()) {
                // Dive toward prey — direct velocity
                Vec3 toTarget = BirdAIUtils.safeDirection(this.bird.position(), this.preyTarget.position());
                this.bird.setDeltaMovement(toTarget.x * 0.8, toTarget.y * 0.8, toTarget.z * 0.8);
            }
            return;
        }

        // Calculate direction to current waypoint target
        if (this.target == null) return;
        Vec3 direction = BirdAIUtils.safeDirection(this.bird.position(), this.target);
        double flightSpeed = 0.25; // Slow quartering flight

        // Wavering flight path: sinusoidal lateral drift
        double lateralDrift = Math.sin(this.flightTicks * 0.15) * 0.02;
        // Apply drift perpendicular to flight direction
        double angle = Math.atan2(direction.z, direction.x) + Math.PI / 2.0;
        double driftX = lateralDrift * Math.cos(angle);
        double driftZ = lateralDrift * Math.sin(angle);

        // Maintain altitude 2-4 blocks above ground
        BlockPos groundPos = findGroundBelow();
        double desiredY = groundPos.getY() + 2.0 + this.bird.getRandom().nextDouble() * 2.0;
        double currentY = this.bird.getY();
        double ySpeed;
        if (currentY < desiredY - 0.5) {
            ySpeed = 0.04;
        } else if (currentY > desiredY + 0.5) {
            ySpeed = -0.03;
        } else {
            ySpeed = (desiredY - currentY) * 0.02;
        }

        this.bird.setDeltaMovement(
                direction.x * flightSpeed + driftX,
                ySpeed,
                direction.z * flightSpeed + driftZ
        );

        // Face movement direction
        this.bird.setYRot((float) (Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI)));
        this.bird.yBodyRot = this.bird.getYRot();

        // Check for prey within 8 blocks
        AABB searchBox = this.bird.getBoundingBox().inflate(8.0);
        List<LivingEntity> nearbyEntities = this.bird.level().getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e instanceof net.minecraft.world.entity.animal.rabbit.Rabbit
        );
        if (!nearbyEntities.isEmpty()) {
            this.preyTarget = nearbyEntities.getFirst();
            this.hovering = true;
            this.hoverTicks = 0;
        }

        // Pick new waypoint when close to current target
        if (this.bird.position().distanceTo(this.target) < 3.0) {
            Vec3 newTarget = findQuarteringTarget();
            if (newTarget != null) {
                this.target = newTarget;
            }
        }
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);  // Re-enables gravity
        this.hovering = false;
        this.preyTarget = null;
        this.cooldown = 200 + this.bird.getRandom().nextInt(201);
    }

    private Vec3 findQuarteringTarget() {
        for (int attempt = 0; attempt < 10; attempt++) {
            double dx = (this.bird.getRandom().nextDouble() - 0.5) * 30.0;
            double dz = (this.bird.getRandom().nextDouble() - 0.5) * 30.0;
            double targetX = this.bird.getX() + dx;
            double targetZ = this.bird.getZ() + dz;

            // Find ground level at target position
            BlockPos targetGround = findGroundAt(targetX, targetZ);
            if (targetGround != null) {
                double targetY = targetGround.getY() + 2.0 + this.bird.getRandom().nextDouble() * 2.0;
                return new Vec3(targetX, targetY, targetZ);
            }
        }
        return null;
    }

    private BlockPos findGroundBelow() {
        BlockPos pos = this.bird.blockPosition();
        for (int y = pos.getY(); y > pos.getY() - 20; y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            if (!this.bird.level().getBlockState(check).isAir()) {
                return check;
            }
        }
        return pos;
    }

    private BlockPos findGroundAt(double x, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        int startY = (int) this.bird.getY() + 5;
        for (int y = startY; y > startY - 30; y--) {
            BlockPos check = new BlockPos(bx, y, bz);
            if (!this.bird.level().getBlockState(check).isAir()) {
                return check;
            }
        }
        return null;
    }
}
