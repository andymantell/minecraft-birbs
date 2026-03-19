package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Makes the bird find and sit on fence posts, walls, or logs.
 */
public class PerchingGoal extends Goal {
    private final AbstractFlyingBird bird;
    private final int searchRadius;
    private final int minPerchTicks;
    private final int maxPerchTicks;

    private BlockPos perchTarget;
    private int perchTimer;
    private int perchDuration;
    private int cooldown;
    private boolean isPerching;

    public PerchingGoal(AbstractFlyingBird bird, int searchRadius, int minPerchTicks, int maxPerchTicks) {
        this.bird = bird;
        this.searchRadius = searchRadius;
        this.minPerchTicks = minPerchTicks;
        this.maxPerchTicks = maxPerchTicks;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 5% chance per tick
        if (this.bird.getRandom().nextFloat() > 0.05f) {
            return false;
        }
        // Search for a perchable block nearby
        this.perchTarget = findPerchBlock();
        return this.perchTarget != null;
    }

    private BlockPos findPerchBlock() {
        BlockPos birdPos = this.bird.blockPosition();
        for (int i = 0; i < 10; i++) {
            int dx = this.bird.getRandom().nextInt(this.searchRadius * 2 + 1) - this.searchRadius;
            int dy = this.bird.getRandom().nextInt(5) - 2;
            int dz = this.bird.getRandom().nextInt(this.searchRadius * 2 + 1) - this.searchRadius;
            BlockPos candidate = birdPos.offset(dx, dy, dz);
            if (isPerchable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isPerchable(BlockPos pos) {
        BlockState state = this.bird.level().getBlockState(pos);
        // Check if it's a fence, fence gate, wall, or log
        boolean isPerchBlock = state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof WallBlock
                || state.is(BlockTags.LOGS, s -> true);
        if (!isPerchBlock) {
            return false;
        }
        // Check that there's air above for the bird to sit on
        BlockState above = this.bird.level().getBlockState(pos.above());
        return above.isAir();
    }

    @Override
    public boolean canContinueToUse() {
        return this.perchTarget != null && this.perchTimer < this.perchDuration;
    }

    @Override
    public void start() {
        this.perchTimer = 0;
        this.perchDuration = this.minPerchTicks + this.bird.getRandom().nextInt(this.maxPerchTicks - this.minPerchTicks + 1);
        this.isPerching = false;
        // Navigate to top of the perch block
        this.bird.getNavigation().moveTo(
                this.perchTarget.getX() + 0.5,
                this.perchTarget.getY() + 1.0,
                this.perchTarget.getZ() + 0.5,
                1.0
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.perchTarget == null) {
            return;
        }

        double distSqr = this.bird.distanceToSqr(
                this.perchTarget.getX() + 0.5,
                this.perchTarget.getY() + 1.0,
                this.perchTarget.getZ() + 0.5
        );

        if (distSqr < 1.0 || this.isPerching) {
            // Arrived at perch - stop navigation and hold still
            if (!this.isPerching) {
                this.isPerching = true;
                this.bird.getNavigation().stop();
                // Snap to perch position
                this.bird.setPos(
                        this.perchTarget.getX() + 0.5,
                        this.perchTarget.getY() + 1.0,
                        this.perchTarget.getZ() + 0.5
                );
            }
            this.bird.setDeltaMovement(0, this.bird.getDeltaMovement().y, 0);
            this.perchTimer++;
        } else if (this.bird.getNavigation().isDone()) {
            // Navigation failed to reach target, retry or give up
            this.perchTarget = null;
        }
    }

    @Override
    public void stop() {
        this.perchTarget = null;
        this.isPerching = false;
        // Set cooldown before perching again: 100-300 ticks
        this.cooldown = 100 + this.bird.getRandom().nextInt(201);
    }
}
