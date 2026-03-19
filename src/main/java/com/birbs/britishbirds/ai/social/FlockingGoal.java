package com.birbs.britishbirds.ai.social;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Simplified boids-style flocking behaviour. The bird stays near others
 * of the same species with cohesion and separation forces.
 */
public class FlockingGoal extends Goal {
    private final PathfinderMob bird;
    private final double searchRadius;
    private final double minSeparation;

    private int activeTicks;
    private int activeDuration;

    private static final int MIN_ACTIVE_TICKS = 100;
    private static final int MAX_ACTIVE_TICKS = 200;

    public FlockingGoal(PathfinderMob bird, double searchRadius, double minSeparation) {
        this.bird = bird;
        this.searchRadius = searchRadius;
        this.minSeparation = minSeparation;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 1% chance per tick to activate
        if (this.bird.getRandom().nextFloat() > 0.01f) {
            return false;
        }
        // Only activate if there are flock-mates nearby
        List<? extends PathfinderMob> nearby = findFlockMates();
        return !nearby.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.activeTicks < this.activeDuration;
    }

    @Override
    public void start() {
        this.activeTicks = 0;
        this.activeDuration = MIN_ACTIVE_TICKS + this.bird.getRandom().nextInt(MAX_ACTIVE_TICKS - MIN_ACTIVE_TICKS + 1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.activeTicks++;

        // Only recalculate movement every 10 ticks for performance
        if (this.activeTicks % 10 != 0) {
            return;
        }

        List<? extends PathfinderMob> flockMates = findFlockMates();
        if (flockMates.isEmpty()) {
            return;
        }

        Vec3 birdPos = this.bird.position();

        // Cohesion: move toward average position of flock
        double avgX = 0, avgY = 0, avgZ = 0;
        for (PathfinderMob mate : flockMates) {
            avgX += mate.getX();
            avgY += mate.getY();
            avgZ += mate.getZ();
        }
        avgX /= flockMates.size();
        avgY /= flockMates.size();
        avgZ /= flockMates.size();

        // Separation: push away from too-close flock-mates
        double sepX = 0, sepY = 0, sepZ = 0;
        double minSepSq = this.minSeparation * this.minSeparation;
        for (PathfinderMob mate : flockMates) {
            double distSq = this.bird.distanceToSqr(mate);
            if (distSq < minSepSq && distSq > 0.01) {
                Vec3 away = birdPos.subtract(mate.position()).normalize();
                double strength = 1.0 - (Math.sqrt(distSq) / this.minSeparation);
                sepX += away.x * strength;
                sepY += away.y * strength;
                sepZ += away.z * strength;
            }
        }

        // Combine: move toward flock center but away from too-close mates
        double targetX = avgX + sepX * 2.0;
        double targetY = avgY + sepY * 2.0;
        double targetZ = avgZ + sepZ * 2.0;

        this.bird.getNavigation().moveTo(targetX, targetY, targetZ, 1.0);
    }

    @Override
    public void stop() {
        this.bird.getNavigation().stop();
    }

    private List<? extends PathfinderMob> findFlockMates() {
        return this.bird.level().getEntitiesOfClass(
                this.bird.getClass(),
                this.bird.getBoundingBox().inflate(this.searchRadius),
                entity -> entity != this.bird && entity.isAlive()
        );
    }
}
