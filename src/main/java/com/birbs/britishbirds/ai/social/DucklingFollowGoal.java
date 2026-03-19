package com.birbs.britishbirds.ai.social;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * Baby ducks follow the nearest adult of the same type in a line.
 * If multiple babies exist, they space out behind the parent.
 */
public class DucklingFollowGoal extends Goal {
    private final PathfinderMob duckling;
    private PathfinderMob parent;

    private static final double SEARCH_RADIUS = 16.0;
    private static final double MIN_FOLLOW_DISTANCE = 1.5;
    private static final double MAX_FOLLOW_DISTANCE = 3.0;

    public DucklingFollowGoal(PathfinderMob duckling) {
        this.duckling = duckling;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only active for baby entities
        if (!this.duckling.isBaby()) {
            return false;
        }
        this.parent = findNearestAdult();
        return this.parent != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.duckling.isBaby() || this.parent == null || !this.parent.isAlive()) {
            return false;
        }
        double dist = this.duckling.distanceTo(this.parent);
        return dist < SEARCH_RADIUS;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.parent == null) return;

        double dist = this.duckling.distanceTo(this.parent);

        // Calculate follow distance based on position among siblings
        double followDist = MIN_FOLLOW_DISTANCE + getSiblingIndex() * 0.8;
        followDist = Math.min(followDist, MAX_FOLLOW_DISTANCE);

        if (dist > followDist) {
            // Match parent's speed
            double speed = 1.0;
            if (dist > followDist * 2.0) {
                speed = 1.5; // Run to catch up
            }
            this.duckling.getNavigation().moveTo(this.parent, speed);
        } else {
            this.duckling.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.parent = null;
        this.duckling.getNavigation().stop();
    }

    private PathfinderMob findNearestAdult() {
        List<? extends PathfinderMob> nearby = this.duckling.level().getEntitiesOfClass(
                this.duckling.getClass(),
                this.duckling.getBoundingBox().inflate(SEARCH_RADIUS),
                entity -> entity != this.duckling && entity.isAlive() && !entity.isBaby()
        );
        PathfinderMob nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (PathfinderMob mob : nearby) {
            double dist = this.duckling.distanceTo(mob);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = mob;
            }
        }
        return nearest;
    }

    /**
     * Determines which position this duckling is in the line of siblings.
     * Returns 0 for the closest sibling to the parent, 1 for next, etc.
     */
    private int getSiblingIndex() {
        if (this.parent == null) return 0;

        List<? extends PathfinderMob> siblings = this.duckling.level().getEntitiesOfClass(
                this.duckling.getClass(),
                this.parent.getBoundingBox().inflate(SEARCH_RADIUS),
                entity -> entity != this.duckling && entity.isBaby() && entity.isAlive()
        );

        int index = 0;
        double myDist = this.duckling.distanceTo(this.parent);
        for (PathfinderMob sibling : siblings) {
            if (sibling.distanceTo(this.parent) < myDist) {
                index++;
            }
        }
        return index;
    }
}
