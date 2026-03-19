package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.ai.BirdAIUtils;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Self-sufficient hopping movement for small passerines.
 * Picks a random nearby target, then hops toward it in discrete jumps
 * with pauses between hops. Replaces WaterAvoidingRandomStrollGoal
 * to prevent smooth running animation.
 */
public class HoppingMovementGoal extends Goal {
    private final AbstractFlyingBird bird;
    private final double hopSpeed;
    private final double hopHeight;
    private int hopCooldown;
    private int idleTicks;
    private Vec3 hopTarget;

    private static final int MAX_IDLE = 80;

    public HoppingMovementGoal(AbstractFlyingBird bird, double hopSpeed, double hopHeight) {
        this.bird = bird;
        this.hopSpeed = hopSpeed;
        this.hopHeight = hopHeight;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.bird.onGround() || this.bird.isFlying()) {
            return false;
        }
        // ~2% per tick chance to start hopping somewhere
        return this.bird.getRandom().nextFloat() < 0.02f;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.bird.onGround() || this.bird.isFlying()) return false;
        if (this.hopTarget == null) return false;
        if (this.bird.position().distanceToSqr(this.hopTarget) < 1.0) return false;
        if (this.idleTicks > MAX_IDLE) return false;
        return true;
    }

    @Override
    public void start() {
        this.hopCooldown = 0;
        this.idleTicks = 0;
        this.hopTarget = findHopTarget();
    }

    private Vec3 findHopTarget() {
        for (int attempt = 0; attempt < 5; attempt++) {
            double dx = (this.bird.getRandom().nextDouble() - 0.5) * 10.0;
            double dz = (this.bird.getRandom().nextDouble() - 0.5) * 10.0;
            Vec3 candidate = this.bird.position().add(dx, 0, dz);
            double dist = this.bird.position().distanceTo(candidate);
            if (dist >= 3.0 && dist <= 6.0) {
                return candidate;
            }
        }
        Vec3 look = this.bird.getLookAngle();
        return this.bird.position().add(look.x * 4, 0, look.z * 4);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.idleTicks++;

        if (this.hopCooldown > 0) {
            this.hopCooldown--;
            // Freeze horizontal motion between hops
            Vec3 currentMotion = this.bird.getDeltaMovement();
            this.bird.setDeltaMovement(0.0, currentMotion.y, 0.0);
        } else if (this.bird.onGround() && this.hopTarget != null) {
            // Face the target
            Vec3 toTarget = BirdAIUtils.safeDirection(this.bird.position(), this.hopTarget);
            if (toTarget != Vec3.ZERO) {
                float targetYaw = (float) (Math.atan2(-toTarget.x, toTarget.z) * (180.0 / Math.PI));
                this.bird.setYRot(targetYaw);
                this.bird.yBodyRot = targetYaw;
            }

            // Hop toward target
            this.bird.setDeltaMovement(toTarget.x * this.hopSpeed, this.hopHeight, toTarget.z * this.hopSpeed);
            this.bird.hurtMarked = true;

            // Random cooldown between hops: 5-15 ticks
            this.hopCooldown = 5 + this.bird.getRandom().nextInt(11);
            this.idleTicks = 0;
        }
    }

    @Override
    public void stop() {
        this.hopTarget = null;
    }
}
