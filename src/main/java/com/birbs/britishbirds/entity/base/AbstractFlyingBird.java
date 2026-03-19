package com.birbs.britishbirds.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFlyingBird extends AbstractBritishBird {

    private boolean isFlying = false;

    protected AbstractFlyingBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        // Use default GroundPathNavigation and default MoveControl
        // Birds walk on the ground most of the time.
        // Flight goals handle air movement directly via setDeltaMovement.
    }

    // DO NOT override createNavigation — use the default GroundPathNavigation
    // DO NOT set FlyingMoveControl — use the default MoveControl

    public static AttributeSupplier.Builder createFlyingBirdAttributes() {
        return createBirdAttributes()
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
        if (flying) {
            this.setNoGravity(true);  // Disable vanilla gravity when in flight
        } else {
            this.setNoGravity(false); // Re-enable gravity when landing
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Auto-land if flying but on ground (flight goal ended, touched down)
        if (this.isFlying && this.onGround()) {
            this.setFlying(false);
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

    // No travel() override — no more upward velocity hack
}
