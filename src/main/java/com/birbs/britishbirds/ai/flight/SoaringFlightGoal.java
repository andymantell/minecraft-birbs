package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * High-altitude circling on thermals for the Peregrine Falcon.
 * Flies in wide circles 20-40 blocks above ground.
 */
public class SoaringFlightGoal extends Goal {
    private final AbstractFlyingBird bird;
    private Vec3 circleCenter;
    private double circleAngle;
    private int soaringTicks;
    private int maxSoaringTicks;
    private int cooldown;
    private static final double CIRCLE_RADIUS = 15.0;
    private static final double FLIGHT_SPEED = 0.4;

    public SoaringFlightGoal(AbstractFlyingBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Must be outdoors (can see sky)
        if (!this.bird.level().canSeeSky(this.bird.blockPosition())) {
            return false;
        }
        // Don't start if actively hunting (target set)
        if (this.bird.getTarget() != null) {
            return false;
        }
        // 3% chance per tick to start soaring
        if (this.bird.getRandom().nextFloat() > 0.03f) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.soaringTicks >= this.maxSoaringTicks) {
            return false;
        }
        // Stop if actively hunting
        if (this.bird.getTarget() != null) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.bird.setFlying(true);
        this.soaringTicks = 0;
        this.maxSoaringTicks = 200 + this.bird.getRandom().nextInt(401); // 200-600 ticks
        this.circleCenter = this.bird.position();
        this.circleAngle = this.bird.getRandom().nextDouble() * Math.PI * 2.0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.soaringTicks++;

        // Increment angle for circular movement
        this.circleAngle += 0.04; // ~157 ticks per full circle
        if (this.circleAngle > Math.PI * 2.0) {
            this.circleAngle -= Math.PI * 2.0;
        }

        // Calculate target position on circle
        double targetX = this.circleCenter.x + Math.cos(this.circleAngle) * CIRCLE_RADIUS;
        double targetZ = this.circleCenter.z + Math.sin(this.circleAngle) * CIRCLE_RADIUS;

        // Desired altitude: 20-40 blocks above ground
        BlockPos groundPos = findGroundBelow(targetX, targetZ);
        double desiredY = groundPos.getY() + 20.0 + this.bird.getRandom().nextDouble() * 20.0;
        // Smooth altitude: don't recalculate every tick, just maintain current desired
        double currentY = this.bird.getY();
        if (currentY < desiredY - 2.0) {
            this.bird.setDeltaMovement(this.bird.getDeltaMovement().add(0, 0.04, 0));
        } else if (currentY > desiredY + 2.0) {
            this.bird.setDeltaMovement(this.bird.getDeltaMovement().add(0, -0.02, 0));
        }

        // Navigate toward the next circle point
        this.bird.getNavigation().moveTo(targetX, Math.max(currentY, desiredY), targetZ, FLIGHT_SPEED);

        // Occasional wing flap impulse between long glides
        if (this.soaringTicks % 40 == 0) {
            this.bird.setDeltaMovement(this.bird.getDeltaMovement().add(0, 0.05, 0));
        }
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);
        this.cooldown = 100 + this.bird.getRandom().nextInt(201); // 100-300 ticks
    }

    private BlockPos findGroundBelow(double x, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        int startY = (int) this.bird.getY();
        for (int y = startY; y > startY - 60; y--) {
            BlockPos check = new BlockPos(bx, y, bz);
            if (!this.bird.level().getBlockState(check).isAir()) {
                return check;
            }
        }
        return this.bird.blockPosition();
    }
}
