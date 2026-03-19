package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Short, low, fluttery flights between nearby positions.
 * Produces a characteristic undulating flight pattern.
 */
public class FlutteringFlightGoal extends Goal {
    private final AbstractFlyingBird bird;
    private final double speed;
    private final int minDist;
    private final int maxDist;

    private Vec3 target;
    private int cooldown;
    private int flightTicks;

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
        // 3% chance per tick, only when on ground
        if (!this.bird.onGround() || this.bird.getRandom().nextFloat() > 0.03f) {
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
        this.bird.setFlying(true);
        this.flightTicks = 0;
        this.bird.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, this.speed);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.flightTicks++;
        // Add slight sinusoidal undulation to Y movement for fluttery effect
        Vec3 motion = this.bird.getDeltaMovement();
        double undulation = Math.sin(this.flightTicks * 0.5) * 0.04;
        this.bird.setDeltaMovement(motion.x, motion.y + undulation, motion.z);
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isFlying() && !this.bird.onGround() && !this.bird.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);
        this.target = null;
        // Cooldown: 40-120 ticks
        this.cooldown = 40 + this.bird.getRandom().nextInt(81);
    }
}
