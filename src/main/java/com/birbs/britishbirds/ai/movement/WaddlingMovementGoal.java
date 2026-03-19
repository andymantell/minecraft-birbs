package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.base.AbstractWaterBird;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Side-to-side waddling gait on land for water birds.
 * Unlike HoppingMovementGoal, there is no jump — just slowed movement
 * with a waddling flag for the model animation (body roll).
 */
public class WaddlingMovementGoal extends Goal {
    private final AbstractWaterBird bird;
    private boolean isWaddling = false;

    public WaddlingMovementGoal(AbstractWaterBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Active when on ground, not in water, and navigation is in progress
        return this.bird.onGround() && !this.bird.isInWater() && !this.bird.getNavigation().isDone();
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.onGround() && !this.bird.isInWater() && !this.bird.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.isWaddling = true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return false;
    }

    @Override
    public void stop() {
        this.isWaddling = false;
    }

    public boolean isWaddling() {
        return this.isWaddling;
    }
}
