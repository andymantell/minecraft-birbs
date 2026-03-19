package com.birbs.britishbirds.entity.raptor;

import com.birbs.britishbirds.ai.movement.PerchingGoal;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for raptor species (Barn Owl, Peregrine Falcon, etc.).
 * Raptors are larger, tougher, and have attack capability.
 */
public abstract class RaptorEntity extends AbstractFlyingBird {

    protected RaptorEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createRaptorAttributes() {
        return createFlyingBirdAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new PerchingGoal(this, 16, 200, 600));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Let subclasses add species-specific goals
        registerSpeciesGoals();
    }

    /**
     * Override this to add species-specific goals.
     * Called after all shared raptor goals have been registered.
     */
    protected abstract void registerSpeciesGoals();
}
