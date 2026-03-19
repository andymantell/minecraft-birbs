package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.ai.feeding.GroundForagingGoal;
import com.birbs.britishbirds.ai.flight.FlutteringFlightGoal;
import com.birbs.britishbirds.ai.movement.HoppingMovementGoal;
import com.birbs.britishbirds.ai.movement.PerchingGoal;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import com.birbs.britishbirds.entity.raptor.RaptorEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for small passerine (songbird) species like Robin, Blue Tit, etc.
 * Composes the shared songbird goals that all small passerines use.
 */
public abstract class SmallPasserineEntity extends AbstractFlyingBird {

    private final GroundForagingGoal foragingGoal;

    protected SmallPasserineEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.foragingGoal = new GroundForagingGoal(this);
    }

    @Override
    protected void registerGoals() {
        // Shared songbird goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, RaptorEntity.class, 12.0f, 1.2, 1.5));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new PerchingGoal(this, 8, 100, 300));
        this.goalSelector.addGoal(4, new FlutteringFlightGoal(this, 1.0, 3, 10));
        this.goalSelector.addGoal(5, this.foragingGoal);
        this.goalSelector.addGoal(6, new HoppingMovementGoal(this, 0.3, 0.3));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Let subclasses add species-specific goals
        registerSpeciesGoals();
    }

    /**
     * Override this to add species-specific goals.
     * Called after all shared songbird goals have been registered.
     */
    protected abstract void registerSpeciesGoals();

    /**
     * Returns whether this bird is currently pecking at the ground (for animation).
     */
    public boolean isPecking() {
        return this.foragingGoal.isPecking();
    }
}
