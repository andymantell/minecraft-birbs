package com.birbs.britishbirds.ai.water;

import com.birbs.britishbirds.entity.base.AbstractWaterBird;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;

import java.util.EnumSet;

/**
 * Active swimming/paddling on water surfaces.
 * Navigates to random positions within water bodies.
 * Not to be confused with vanilla FloatGoal (which just prevents sinking).
 */
public class SwimmingGoal extends Goal {
    private final AbstractWaterBird bird;
    private int sessionTicks;
    private int sessionDuration;
    private int cooldown;

    private static final int MIN_SESSION = 100;
    private static final int MAX_SESSION = 300;
    private static final int MIN_COOLDOWN = 40;
    private static final int MAX_COOLDOWN = 100;

    public SwimmingGoal(AbstractWaterBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        return this.bird.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isInWater() && this.sessionTicks < this.sessionDuration;
    }

    @Override
    public void start() {
        this.sessionTicks = 0;
        this.sessionDuration = MIN_SESSION + this.bird.getRandom().nextInt(MAX_SESSION - MIN_SESSION + 1);
        navigateToWaterPos();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.sessionTicks++;

        // Pick new target periodically
        if (this.bird.getNavigation().isDone() || this.sessionTicks % 60 == 0) {
            navigateToWaterPos();
        }
    }

    private void navigateToWaterPos() {
        Level level = this.bird.level();
        BlockPos birdPos = this.bird.blockPosition();

        // Try to find a water block within 10 blocks
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = this.bird.getRandom().nextInt(21) - 10;
            int dz = this.bird.getRandom().nextInt(21) - 10;
            BlockPos target = birdPos.offset(dx, 0, dz);

            // Search vertically for water surface
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos check = target.offset(0, dy, 0);
                FluidState fluidState = level.getFluidState(check);
                if (fluidState.is(FluidTags.WATER)) {
                    // Navigate to the water surface
                    this.bird.getNavigation().moveTo(
                            check.getX() + 0.5, check.getY() + 0.5, check.getZ() + 0.5, 0.8);
                    return;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.cooldown = MIN_COOLDOWN + this.bird.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
    }
}
