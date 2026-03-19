package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractWaterBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Strong, straight, rapid flight for water birds.
 * No sinusoidal undulation — direct line to target.
 * Active when bird needs to travel long distance (> 15 blocks) or flee.
 */
public class DirectFlightGoal extends Goal {
    private final AbstractWaterBird bird;
    private final double speed;

    private Vec3 target;
    private int cooldown;

    private static final int MIN_COOLDOWN = 200;
    private static final int MAX_COOLDOWN = 400;
    private static final double MIN_DISTANCE = 15.0;

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
        // 1% chance per tick when on ground or water
        if (this.bird.getRandom().nextFloat() > 0.01f) {
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
                // Elevate the target to ensure flight
                return new Vec3(pos.x, pos.y + 5.0, pos.z);
            }
        }
        // Fallback: manual random offset
        for (int attempt = 0; attempt < 5; attempt++) {
            double dx = (this.bird.getRandom().nextDouble() - 0.5) * 60.0;
            double dz = (this.bird.getRandom().nextDouble() - 0.5) * 60.0;
            double dy = this.bird.getRandom().nextDouble() * 8.0 + 3.0;
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
        this.bird.setFlying(true);
        this.bird.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, this.speed);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isFlying() && !this.bird.onGround() && !this.bird.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);
        this.target = null;
        this.cooldown = MIN_COOLDOWN + this.bird.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
    }
}
