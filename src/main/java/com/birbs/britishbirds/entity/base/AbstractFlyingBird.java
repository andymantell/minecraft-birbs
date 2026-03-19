package com.birbs.britishbirds.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFlyingBird extends AbstractBritishBird {

    private boolean isFlying = false;

    protected AbstractFlyingBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        nav.setCanOpenDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createFlyingBirdAttributes() {
        return createBirdAttributes()
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    @Override
    public void tick() {
        super.tick();
        // Auto-detect flying state: flying when not on ground and not in water
        if (!this.onGround() && !this.isInWater()) {
            this.isFlying = true;
        } else if (this.onGround()) {
            this.isFlying = false;
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        // Flying birds don't take fall damage - do nothing
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isFlying && !this.isInWater()) {
            // Apply reduced gravity when flying
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0)); // counteract some gravity
        }
        super.travel(travelVector);
    }
}
