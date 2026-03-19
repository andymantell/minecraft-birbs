package com.birbs.britishbirds.entity.waterfowl;

import com.birbs.britishbirds.ai.flight.DirectFlightGoal;
import com.birbs.britishbirds.ai.movement.WaddlingMovementGoal;
import com.birbs.britishbirds.ai.social.FlockingGoal;
import com.birbs.britishbirds.ai.water.DabblingGoal;
import com.birbs.britishbirds.ai.water.SwimmingGoal;
import com.birbs.britishbirds.entity.base.AbstractWaterBird;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for waterfowl species (ducks, geese, etc.).
 * Composes shared waterfowl goals for swimming, dabbling, waddling, and flight.
 */
public abstract class WaterfowlEntity extends AbstractWaterBird {

    protected WaterfowlEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new SwimmingGoal(this));
        this.goalSelector.addGoal(3, new DabblingGoal(this));
        this.goalSelector.addGoal(4, new WaddlingMovementGoal(this));
        this.goalSelector.addGoal(5, new DirectFlightGoal(this, 1.0));
        this.goalSelector.addGoal(6, new FlockingGoal(this, 16.0, 2.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Let subclasses add species-specific goals
        registerSpeciesGoals();
    }

    /**
     * Override this to add species-specific goals.
     * Called after all shared waterfowl goals have been registered.
     */
    protected abstract void registerSpeciesGoals();
}
