package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.ai.BirdAIUtils;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Short, low, fluttery flights between nearby positions.
 * Produces a characteristic undulating flight pattern.
 * Uses direct velocity control (setDeltaMovement) rather than navigation,
 * since the bird uses GroundPathNavigation which can't route through air.
 */
public class FlutteringFlightGoal extends Goal {
    private final AbstractFlyingBird bird;
    private final double speed;
    private final int minDist;
    private final int maxDist;

    private Vec3 target;
    private int cooldown;
    private int flightTicks;
    private static final int MAX_FLIGHT_TICKS = 100;

    public FlutteringFlightGoal(AbstractFlyingBird bird, double speed, int minDist, int maxDist) {
        this.bird = bird;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 1% chance per tick (reduced from 3%), only when on ground
        if (!this.bird.onGround() || this.bird.getRandom().nextFloat() > 0.01f) {
            return false;
        }
        // Find a random target position
        this.target = findFlightTarget();
        return this.target != null;
    }

    private Vec3 findFlightTarget() {
        // Try LandRandomPos first with maxDist as horizontal range and minDist as vertical range
        Vec3 pos = LandRandomPos.getPos(this.bird, this.maxDist, this.minDist);
        if (pos != null) {
            double dist = this.bird.position().distanceTo(pos);
            if (dist >= this.minDist && dist <= this.maxDist) {
                return pos;
            }
        }
        // Fallback: manual random offset
        for (int attempt = 0; attempt < 5; attempt++) {
            double dx = (this.bird.getRandom().nextDouble() - 0.5) * 2.0 * this.maxDist;
            double dz = (this.bird.getRandom().nextDouble() - 0.5) * 2.0 * this.maxDist;
            double dy = this.bird.getRandom().nextDouble() * 3.0 + 1.0; // Slight upward bias
            Vec3 candidate = this.bird.position().add(dx, dy, dz);
            double dist = this.bird.position().distanceTo(candidate);
            if (dist >= this.minDist && dist <= this.maxDist) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void start() {
        this.bird.setFlying(true);  // Enables noGravity
        this.flightTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.flightTicks++;

        // Calculate direction vector toward target
        Vec3 direction = BirdAIUtils.safeDirection(this.bird.position(), this.target);
        double flightSpeed = 0.3 * this.speed;

        // Slight sinusoidal Y for undulation, with a small downward bias so birds don't float up
        double undulation = Math.sin(this.flightTicks * 0.3) * 0.03;
        this.bird.setDeltaMovement(
                direction.x * flightSpeed,
                direction.y * flightSpeed + undulation - 0.01,
                direction.z * flightSpeed
        );

        // Face movement direction
        this.bird.setYRot((float) (Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI)));
        this.bird.yBodyRot = this.bird.getYRot();
    }

    @Override
    public boolean canContinueToUse() {
        // Land if close to target or been flying too long
        if (this.bird.position().distanceTo(this.target) < 1.5) {
            return false;
        }
        if (this.flightTicks > MAX_FLIGHT_TICKS) {
            return false;
        }
        // Also stop if we've somehow landed
        if (this.bird.onGround() && this.flightTicks > 5) {
            return false;
        }
        return true;
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);  // Re-enables gravity, bird falls to ground
        this.target = null;
        // Cooldown: 100-300 ticks (longer than before — songbirds spend most time on ground)
        this.cooldown = 100 + this.bird.getRandom().nextInt(201);
    }
}
