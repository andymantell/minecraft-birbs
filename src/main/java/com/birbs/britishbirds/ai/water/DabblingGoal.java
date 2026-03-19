package com.birbs.britishbirds.ai.water;

import com.birbs.britishbirds.entity.waterfowl.MallardEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Upend/tip-up feeding while on water.
 * Sets a dabbling flag for the model to read (body pitches forward 90 degrees, tail up).
 */
public class DabblingGoal extends Goal {
    private final PathfinderMob bird;
    private int dabblingTicks;
    private int dabblingDuration;
    private int cooldown;

    private static final int MIN_DURATION = 40;
    private static final int MAX_DURATION = 80;
    private static final int MIN_COOLDOWN = 100;
    private static final int MAX_COOLDOWN = 200;

    public DabblingGoal(PathfinderMob bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Active when in water, 2% chance per tick
        return this.bird.isInWater() && this.bird.getRandom().nextFloat() < 0.02f;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isInWater() && this.dabblingTicks < this.dabblingDuration;
    }

    @Override
    public void start() {
        this.dabblingTicks = 0;
        this.dabblingDuration = MIN_DURATION + this.bird.getRandom().nextInt(MAX_DURATION - MIN_DURATION + 1);
        setDabbling(true);
        // Stop movement while dabbling
        this.bird.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.dabblingTicks++;
        // Keep the bird stationary while dabbling
        this.bird.setDeltaMovement(this.bird.getDeltaMovement().multiply(0.0, 1.0, 0.0));
    }

    @Override
    public void stop() {
        setDabbling(false);
        this.cooldown = MIN_COOLDOWN + this.bird.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
    }

    private void setDabbling(boolean dabbling) {
        if (this.bird instanceof MallardEntity mallard) {
            mallard.setDabbling(dabbling);
        }
    }
}
