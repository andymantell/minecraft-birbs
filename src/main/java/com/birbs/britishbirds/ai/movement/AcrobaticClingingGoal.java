package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.songbird.BlueTitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Blue Tit hangs upside-down from leaf or log blocks, mimicking their
 * real-life acrobatic feeding behaviour on branches and feeders.
 */
public class AcrobaticClingingGoal extends Goal {
    private final BlueTitEntity bird;
    private final int searchRadius;

    private BlockPos clingTarget;
    private int clingTimer;
    private int clingDuration;
    private int cooldown;
    private boolean isClinging;

    private static final int MIN_CLING_TICKS = 40;
    private static final int MAX_CLING_TICKS = 120;
    private static final int MIN_COOLDOWN = 60;
    private static final int MAX_COOLDOWN = 200;

    public AcrobaticClingingGoal(BlueTitEntity bird, int searchRadius) {
        this.bird = bird;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 3% chance per tick to activate
        if (this.bird.getRandom().nextFloat() > 0.03f) {
            return false;
        }
        this.clingTarget = findClingBlock();
        return this.clingTarget != null;
    }

    private BlockPos findClingBlock() {
        BlockPos birdPos = this.bird.blockPosition();
        for (int i = 0; i < 15; i++) {
            int dx = this.bird.getRandom().nextInt(this.searchRadius * 2 + 1) - this.searchRadius;
            int dy = this.bird.getRandom().nextInt(5) - 1; // slightly biased upward
            int dz = this.bird.getRandom().nextInt(this.searchRadius * 2 + 1) - this.searchRadius;
            BlockPos candidate = birdPos.offset(dx, dy, dz);
            if (isClingable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isClingable(BlockPos pos) {
        BlockState state = this.bird.level().getBlockState(pos);
        // Leaves or logs are clingable
        boolean isClingBlock = state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
        if (!isClingBlock) {
            return false;
        }
        // Need air below to hang from
        BlockState below = this.bird.level().getBlockState(pos.below());
        return below.isAir();
    }

    @Override
    public boolean canContinueToUse() {
        return this.clingTarget != null && this.clingTimer < this.clingDuration;
    }

    @Override
    public void start() {
        this.clingTimer = 0;
        this.clingDuration = MIN_CLING_TICKS + this.bird.getRandom().nextInt(MAX_CLING_TICKS - MIN_CLING_TICKS + 1);
        this.isClinging = false;
        // Navigate to the underside of the block
        this.bird.getNavigation().moveTo(
                this.clingTarget.getX() + 0.5,
                this.clingTarget.getY() - 0.5,
                this.clingTarget.getZ() + 0.5,
                1.0
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.clingTarget == null) {
            return;
        }

        double distSqr = this.bird.distanceToSqr(
                this.clingTarget.getX() + 0.5,
                this.clingTarget.getY() - 0.5,
                this.clingTarget.getZ() + 0.5
        );

        if (distSqr < 1.5 || this.isClinging) {
            if (!this.isClinging) {
                this.isClinging = true;
                this.bird.getNavigation().stop();
                // Snap to hanging position just below the block
                this.bird.setPos(
                        this.clingTarget.getX() + 0.5,
                        this.clingTarget.getY() - 0.5,
                        this.clingTarget.getZ() + 0.5
                );
                this.bird.setHangingUpsideDown(true);
            }
            // Hold position
            this.bird.setDeltaMovement(0, 0, 0);
            this.bird.setNoGravity(true);
            this.clingTimer++;
        } else if (this.bird.getNavigation().isDone()) {
            // Navigation failed — give up
            this.clingTarget = null;
        }
    }

    @Override
    public void stop() {
        if (this.isClinging) {
            this.bird.setHangingUpsideDown(false);
            this.bird.setNoGravity(false);
        }
        this.clingTarget = null;
        this.isClinging = false;
        this.cooldown = MIN_COOLDOWN + this.bird.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
    }
}
