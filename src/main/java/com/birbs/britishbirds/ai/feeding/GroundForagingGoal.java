package com.birbs.britishbirds.ai.feeding;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Bird hops around pecking at the ground searching for food.
 * Active when on ground during daytime, with a low chance per tick.
 */
public class GroundForagingGoal extends Goal {
    private final AbstractFlyingBird bird;
    private int duration;
    private int timer;
    private int peckTimer;
    private boolean pecking;

    private static final int MIN_DURATION = 60;   // 3 seconds
    private static final int MAX_DURATION = 180;   // 9 seconds
    private static final int PECK_INTERVAL_MIN = 15;
    private static final int PECK_INTERVAL_MAX = 40;
    private static final int PECK_DURATION = 10;   // ticks the peck animation lasts

    public GroundForagingGoal(AbstractFlyingBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only during daytime (MC time 0-12000)
        long dayTime = this.bird.level().getOverworldClockTime() % 24000;
        if (dayTime > 12000) {
            return false;
        }
        // Must be on the ground
        if (!this.bird.onGround()) {
            return false;
        }
        // 2% chance per tick
        return this.bird.getRandom().nextFloat() < 0.02f;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.onGround() && this.timer < this.duration;
    }

    @Override
    public void start() {
        this.timer = 0;
        this.duration = MIN_DURATION + this.bird.getRandom().nextInt(MAX_DURATION - MIN_DURATION + 1);
        this.peckTimer = PECK_INTERVAL_MIN + this.bird.getRandom().nextInt(PECK_INTERVAL_MAX - PECK_INTERVAL_MIN + 1);
        this.pecking = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.timer++;
        this.peckTimer--;

        if (this.pecking) {
            // During peck, stop navigation briefly
            this.bird.getNavigation().stop();
            if (this.peckTimer <= 0) {
                this.pecking = false;
                // Schedule next peck
                this.peckTimer = PECK_INTERVAL_MIN + this.bird.getRandom().nextInt(PECK_INTERVAL_MAX - PECK_INTERVAL_MIN + 1);
            }
        } else if (this.peckTimer <= 0) {
            // Start a peck
            this.pecking = true;
            this.peckTimer = PECK_DURATION;
        }
    }

    @Override
    public void stop() {
        this.pecking = false;
    }

    /**
     * Returns whether the bird is currently in a pecking animation.
     */
    public boolean isPecking() {
        return this.pecking;
    }
}
