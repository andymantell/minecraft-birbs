package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Makes the bird hop instead of walk smoothly when moving on the ground.
 * The bird alternates between short hops and brief pauses.
 */
public class HoppingMovementGoal extends Goal {
    private final AbstractFlyingBird bird;
    private final double hopSpeed;
    private final double hopHeight;
    private int hopCooldown;

    public HoppingMovementGoal(AbstractFlyingBird bird, double hopSpeed, double hopHeight) {
        this.bird = bird;
        this.hopSpeed = hopSpeed;
        this.hopHeight = hopHeight;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // Active when bird is on ground, not flying, and navigation is in progress
        return this.bird.onGround() && !this.bird.isFlying() && !this.bird.getNavigation().isDone();
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.onGround() && !this.bird.isFlying() && !this.bird.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.hopCooldown = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.hopCooldown > 0) {
            this.hopCooldown--;
            // During the pause between hops, freeze horizontal motion
            Vec3 currentMotion = this.bird.getDeltaMovement();
            this.bird.setDeltaMovement(0.0, currentMotion.y, 0.0);
        } else if (this.bird.onGround() && !this.bird.getNavigation().isDone()) {
            // Time to hop! Apply forward impulse toward navigation target + small upward boost
            Vec3 look = this.bird.getLookAngle();
            double forwardX = look.x * this.hopSpeed;
            double forwardZ = look.z * this.hopSpeed;
            this.bird.setDeltaMovement(forwardX, this.hopHeight, forwardZ);
            this.bird.hurtMarked = true;

            // Random cooldown between hops: 5-20 ticks
            this.hopCooldown = 5 + this.bird.getRandom().nextInt(16);
        }
    }

    @Override
    public void stop() {
        // Nothing special needed on stop
    }
}
