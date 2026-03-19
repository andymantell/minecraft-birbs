package com.birbs.britishbirds.ai.schedule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LightLayer;

import java.util.EnumSet;

/**
 * Controls when an entity is active vs roosting based on time of day.
 * When the entity should be inactive (roosting), this goal takes priority
 * and makes the entity seek a dark spot and stay still.
 */
public class ActivityScheduleGoal extends Goal {

    public enum Mode {
        DIURNAL,
        NOCTURNAL,
        CREPUSCULAR
    }

    private final PathfinderMob entity;
    private final Mode mode;
    private BlockPos roostTarget;
    private boolean reachedRoost;

    public ActivityScheduleGoal(PathfinderMob entity, Mode mode) {
        this.entity = entity;
        this.mode = mode;
        // High priority: uses all flags to block other goals when roosting
        this.setFlags(EnumSet.allOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        // This goal activates when the entity should be ROOSTING (inactive)
        return shouldRoost();
    }

    @Override
    public boolean canContinueToUse() {
        return shouldRoost();
    }

    private boolean shouldRoost() {
        long time = this.entity.level().getOverworldClockTime() % 24000;
        return switch (this.mode) {
            case DIURNAL ->
                // Diurnal: active 0-12000 (day), roost 12000-24000 (night)
                    time >= 12000;
            case NOCTURNAL ->
                // Nocturnal: active 12000-24000 (night), roost 0-12000 (day)
                    time < 12000;
            case CREPUSCULAR ->
                // Crepuscular: active 11000-13000 (dusk) and 23000-1000 (dawn), roost otherwise
                    !((time >= 11000 && time < 13000) || time >= 23000 || time < 1000);
        };
    }

    @Override
    public void start() {
        this.roostTarget = null;
        this.reachedRoost = false;
        // Find a dark spot to roost
        this.roostTarget = findDarkBlock();
        if (this.roostTarget != null) {
            this.entity.getNavigation().moveTo(
                    this.roostTarget.getX() + 0.5,
                    this.roostTarget.getY(),
                    this.roostTarget.getZ() + 0.5,
                    1.0
            );
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.reachedRoost) {
            // Stay still at roost
            this.entity.getNavigation().stop();
            this.entity.setDeltaMovement(0, this.entity.getDeltaMovement().y, 0);
            return;
        }

        if (this.roostTarget != null) {
            double distSqr = this.entity.distanceToSqr(
                    this.roostTarget.getX() + 0.5,
                    this.roostTarget.getY(),
                    this.roostTarget.getZ() + 0.5
            );
            if (distSqr < 2.0) {
                this.reachedRoost = true;
                this.entity.getNavigation().stop();
            } else if (this.entity.getNavigation().isDone()) {
                // Navigation finished but we're not close enough, try again
                this.roostTarget = findDarkBlock();
                if (this.roostTarget != null) {
                    this.entity.getNavigation().moveTo(
                            this.roostTarget.getX() + 0.5,
                            this.roostTarget.getY(),
                            this.roostTarget.getZ() + 0.5,
                            1.0
                    );
                }
            }
        } else {
            // Just stay still if no roost found
            this.entity.getNavigation().stop();
            this.entity.setDeltaMovement(0, this.entity.getDeltaMovement().y, 0);
        }
    }

    @Override
    public void stop() {
        this.roostTarget = null;
        this.reachedRoost = false;
    }

    private BlockPos findDarkBlock() {
        BlockPos entityPos = this.entity.blockPosition();
        for (int i = 0; i < 15; i++) {
            int dx = this.entity.getRandom().nextInt(17) - 8;
            int dy = this.entity.getRandom().nextInt(7) - 3;
            int dz = this.entity.getRandom().nextInt(17) - 8;
            BlockPos candidate = entityPos.offset(dx, dy, dz);
            if (this.entity.level().getBlockState(candidate).isAir()
                    && this.entity.level().getBrightness(LightLayer.BLOCK, candidate) < 7
                    && !this.entity.level().getBlockState(candidate.below()).isAir()) {
                return candidate;
            }
        }
        // If no dark block found, just stay where we are
        return null;
    }
}
