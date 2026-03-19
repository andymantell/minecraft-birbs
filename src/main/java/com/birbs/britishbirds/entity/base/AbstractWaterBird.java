package com.birbs.britishbirds.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Base class for water birds (ducks, geese, swans, etc.).
 * Water birds can swim, walk on land, and fly.
 */
public abstract class AbstractWaterBird extends AbstractBritishBird {

    private boolean isFlying = false;
    private boolean isSwimming = false;

    protected AbstractWaterBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        // Make water attractive for pathfinding
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        nav.setCanOpenDoors(false);
        return nav;
    }

    public static AttributeSupplier.Builder createWaterBirdAttributes() {
        return createBirdAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.35);
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public boolean isSwimming() {
        return this.isSwimming;
    }

    public void setSwimming(boolean swimming) {
        this.isSwimming = swimming;
    }

    @Override
    public void tick() {
        super.tick();
        // Auto-detect flying state: flying when not on ground and not in water
        if (!this.onGround() && !this.isInWater()) {
            this.isFlying = true;
            this.isSwimming = false;
        } else if (this.isInWater()) {
            this.isSwimming = true;
            this.isFlying = false;
        } else if (this.onGround()) {
            this.isFlying = false;
            this.isSwimming = false;
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        // Water birds don't take fall damage
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInWater()) {
            // Swimming: apply paddling movement
            this.moveRelative(0.02f, travelVector);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            // Keep at water surface level - gentle buoyancy
            if (this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > this.getBbHeight() * 0.5) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.02, 0.0));
            }
        } else if (this.isFlying) {
            // Apply reduced gravity when flying
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
            super.travel(travelVector);
        } else {
            super.travel(travelVector);
        }
    }
}
