package com.birbs.britishbirds.ai.social;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Chases away other entities of the same species within a territory radius.
 * Based on real robin behaviour — robins are famously territorial.
 */
public class TerritorialGoal extends Goal {
    private final PathfinderMob bird;
    private final double territoryRadius;
    private final double chaseSpeed;

    private PathfinderMob intruder;
    private int chaseTicks;
    private int chaseDuration;
    private int cooldown;

    private static final int MIN_CHASE_TICKS = 100;  // 5 seconds
    private static final int MAX_CHASE_TICKS = 200;   // 10 seconds
    private static final double KNOCKBACK_RANGE_SQ = 4.0; // 2 blocks squared

    public TerritorialGoal(PathfinderMob bird, double territoryRadius, double chaseSpeed) {
        this.bird = bird;
        this.territoryRadius = territoryRadius;
        this.chaseSpeed = chaseSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Find nearest same-species non-baby intruder
        List<? extends PathfinderMob> nearby = this.bird.level().getEntitiesOfClass(
                this.bird.getClass(),
                this.bird.getBoundingBox().inflate(this.territoryRadius),
                entity -> entity != this.bird && !entity.isBaby()
        );
        if (nearby.isEmpty()) {
            return false;
        }
        // Pick the closest intruder
        this.intruder = null;
        double closestDist = Double.MAX_VALUE;
        for (PathfinderMob candidate : nearby) {
            double dist = this.bird.distanceToSqr(candidate);
            if (dist < closestDist) {
                closestDist = dist;
                this.intruder = candidate;
            }
        }
        return this.intruder != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.intruder == null || !this.intruder.isAlive()) {
            return false;
        }
        if (this.chaseTicks >= this.chaseDuration) {
            return false;
        }
        // Stop if intruder is well outside territory
        return this.bird.distanceToSqr(this.intruder) < this.territoryRadius * this.territoryRadius * 4;
    }

    @Override
    public void start() {
        this.chaseTicks = 0;
        this.chaseDuration = MIN_CHASE_TICKS + this.bird.getRandom().nextInt(MAX_CHASE_TICKS - MIN_CHASE_TICKS + 1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.intruder == null) {
            return;
        }
        this.chaseTicks++;

        // Look at the intruder
        this.bird.getLookControl().setLookAt(this.intruder, 30.0f, 30.0f);

        // Chase the intruder
        this.bird.getNavigation().moveTo(this.intruder, this.chaseSpeed);

        // When close enough, push the intruder away
        double distSqr = this.bird.distanceToSqr(this.intruder);
        if (distSqr < KNOCKBACK_RANGE_SQ) {
            Vec3 pushDir = this.intruder.position().subtract(this.bird.position()).normalize();
            this.intruder.setDeltaMovement(
                    this.intruder.getDeltaMovement().add(pushDir.x * 0.3, 0.1, pushDir.z * 0.3)
            );
            this.intruder.hurtMarked = true;
        }
    }

    @Override
    public void stop() {
        this.intruder = null;
        // Cooldown: 200-600 ticks (10-30 seconds)
        this.cooldown = 200 + this.bird.getRandom().nextInt(401);
    }
}
