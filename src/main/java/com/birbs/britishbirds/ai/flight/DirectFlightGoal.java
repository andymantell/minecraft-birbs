package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractWaterBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Strong, straight, rapid flight for water birds.
 * No sinusoidal undulation — direct line to target.
 * Activates very rarely (ducks should spend 90% of time on ground/water).
 * Uses direct velocity control (setDeltaMovement) rather than navigation.
 */
public class DirectFlightGoal extends Goal {
    private final AbstractWaterBird bird;
    private final double speed;

    private Vec3 target;
    private int cooldown;
    private int flightTicks;

    private static final int MIN_COOLDOWN = 600;
    private static final int MAX_COOLDOWN = 1200;
    private static final double MIN_DISTANCE = 15.0;
    private static final int MAX_FLIGHT_TICKS = 200;

    public DirectFlightGoal(AbstractWaterBird bird, double speed) {
        this.bird = bird;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Very rare: 0.5% chance per tick when on ground or water (reduced from 1%)
        if (this.bird.getRandom().nextFloat() > 0.005f) {
            return false;
        }
        this.target = findFlightTarget();
        return this.target != null;
    }

    private Vec3 findFlightTarget() {
        // Try to find a distant target
        Vec3 pos = LandRandomPos.getPos(this.bird, 30, 5);
        if (pos != null) {
            double dist = this.bird.position().distanceTo(pos);
            if (dist >= MIN_DISTANCE) {
                // Elevate the target to ensure flight altitude
                return new Vec3(pos.x, pos.y + 8.0, pos.z);
            }
        }
        // Fallback: manual random offset
        for (int attempt = 0; attempt < 5; attempt++) {
            double dx = (this.bird.getRandom().nextDouble() - 0.5) * 60.0;
            double dz = (this.bird.getRandom().nextDouble() - 0.5) * 60.0;
            double dy = this.bird.getRandom().nextDouble() * 8.0 + 5.0;
            Vec3 candidate = this.bird.position().add(dx, dy, dz);
            double dist = this.bird.position().distanceTo(candidate);
            if (dist >= MIN_DISTANCE) {
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
        Vec3 direction = this.target.subtract(this.bird.position()).normalize();
        double flightSpeed = 0.5 * this.speed; // Strong, fast flight

        // Direct line to target — no undulation for ducks
        this.bird.setDeltaMovement(
                direction.x * flightSpeed,
                direction.y * flightSpeed - 0.005, // Very slight downward bias
                direction.z * flightSpeed
        );

        // Face movement direction
        this.bird.setYRot((float) (Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI)));
        this.bird.yBodyRot = this.bird.getYRot();
    }

    @Override
    public boolean canContinueToUse() {
        // Land if close to target
        if (this.bird.position().distanceTo(this.target) < 2.0) {
            return false;
        }
        // Or been flying too long
        if (this.flightTicks > MAX_FLIGHT_TICKS) {
            return false;
        }
        // Also stop if we've somehow landed mid-flight
        if (this.bird.onGround() && this.flightTicks > 10) {
            return false;
        }
        return true;
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);  // Re-enables gravity
        this.target = null;
        this.cooldown = MIN_COOLDOWN + this.bird.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
    }
}
