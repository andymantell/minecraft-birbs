package com.birbs.britishbirds.ai.feeding;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;

import java.util.EnumSet;
import java.util.List;

/**
 * Robin follows a player holding a hoe or shovel (digging tools),
 * mimicking real robins that follow gardeners to catch exposed insects.
 */
public class FollowDiggingPlayerGoal extends Goal {
    private final PathfinderMob bird;
    private final double searchRange;
    private final double followDistMin;
    private final double followDistMax;
    private final double speed;

    private Player targetPlayer;
    private int loseInterestTimer;

    private static final int LOSE_INTEREST_TICKS = 100; // 5 seconds

    public FollowDiggingPlayerGoal(PathfinderMob bird, double searchRange, double speed) {
        this.bird = bird;
        this.searchRange = searchRange;
        this.followDistMin = 2.0;
        this.followDistMax = 4.0;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<Player> nearbyPlayers = this.bird.level().getEntitiesOfClass(
                Player.class,
                this.bird.getBoundingBox().inflate(this.searchRange),
                this::isHoldingDiggingTool
        );
        if (nearbyPlayers.isEmpty()) {
            return false;
        }
        // Pick the closest one
        this.targetPlayer = null;
        double closestDist = Double.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            double dist = this.bird.distanceToSqr(player);
            if (dist < closestDist) {
                closestDist = dist;
                this.targetPlayer = player;
            }
        }
        return this.targetPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        if (this.bird.distanceToSqr(this.targetPlayer) > this.searchRange * this.searchRange * 4) {
            return false; // Player too far away
        }
        if (!isHoldingDiggingTool(this.targetPlayer)) {
            this.loseInterestTimer++;
            return this.loseInterestTimer < LOSE_INTEREST_TICKS;
        } else {
            this.loseInterestTimer = 0;
        }
        return true;
    }

    @Override
    public void start() {
        this.loseInterestTimer = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) {
            return;
        }
        // Look at the player
        this.bird.getLookControl().setLookAt(this.targetPlayer, 30.0f, 30.0f);

        double distSqr = this.bird.distanceToSqr(this.targetPlayer);
        if (distSqr > this.followDistMax * this.followDistMax) {
            // Move toward the player
            this.bird.getNavigation().moveTo(this.targetPlayer, this.speed);
        } else if (distSqr < this.followDistMin * this.followDistMin) {
            // Too close, stop moving
            this.bird.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.loseInterestTimer = 0;
        this.bird.getNavigation().stop();
    }

    private boolean isHoldingDiggingTool(Player player) {
        return player.getMainHandItem().getItem() instanceof HoeItem
                || player.getMainHandItem().getItem() instanceof ShovelItem
                || player.getOffhandItem().getItem() instanceof HoeItem
                || player.getOffhandItem().getItem() instanceof ShovelItem;
    }
}
