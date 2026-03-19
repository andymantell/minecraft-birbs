package com.birbs.britishbirds.ai.feeding;

import com.birbs.britishbirds.entity.raptor.RaptorEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Configurable hunting AI for raptors.
 * Searches for prey, navigates toward it, and attacks when in range.
 */
public class HuntingGoal extends Goal {
    private final RaptorEntity raptor;
    private final Class<? extends LivingEntity> targetClass;
    private final double searchRange;
    private final double huntingSpeed;
    private static final double ATTACK_RANGE = 1.5;

    private LivingEntity prey;
    private int cooldown;
    private int huntTicks;
    private static final int MAX_HUNT_TICKS = 400;

    public HuntingGoal(RaptorEntity raptor, Class<? extends LivingEntity> targetClass,
                       double searchRange, double huntingSpeed) {
        this.raptor = raptor;
        this.targetClass = targetClass;
        this.searchRange = searchRange;
        this.huntingSpeed = huntingSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // Search for prey
        this.prey = findPrey();
        return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.prey == null || !this.prey.isAlive()) {
            return false;
        }
        if (this.huntTicks > MAX_HUNT_TICKS) {
            return false;
        }
        double distSqr = this.raptor.distanceToSqr(this.prey);
        return distSqr < this.searchRange * this.searchRange * 4; // Give up if prey gets too far
    }

    @Override
    public void start() {
        this.huntTicks = 0;
        if (this.prey != null) {
            this.raptor.getNavigation().moveTo(this.prey, this.huntingSpeed);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.huntTicks++;
        if (this.prey == null || !this.prey.isAlive()) {
            return;
        }

        // Look at prey
        this.raptor.getLookControl().setLookAt(this.prey, 30.0f, 30.0f);

        double distSqr = this.raptor.distanceToSqr(this.prey);

        if (distSqr <= ATTACK_RANGE * ATTACK_RANGE) {
            // Attack
            if (this.raptor.level() instanceof ServerLevel serverLevel) {
                this.raptor.doHurtTarget(serverLevel, this.prey);
            }
            this.prey = null; // Hunt complete
        } else {
            // Keep navigating toward prey
            if (this.huntTicks % 10 == 0) {
                this.raptor.getNavigation().moveTo(this.prey, this.huntingSpeed);
            }
        }
    }

    @Override
    public void stop() {
        this.prey = null;
        // Cooldown 300-600 ticks
        this.cooldown = 300 + this.raptor.getRandom().nextInt(301);
    }

    private LivingEntity findPrey() {
        AABB searchBox = this.raptor.getBoundingBox().inflate(this.searchRange);
        List<? extends LivingEntity> candidates = this.raptor.level().getEntitiesOfClass(
                this.targetClass, searchBox, LivingEntity::isAlive
        );
        if (candidates.isEmpty()) {
            return null;
        }
        // Pick the closest
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : candidates) {
            double dist = this.raptor.distanceToSqr(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }
        return closest;
    }
}
