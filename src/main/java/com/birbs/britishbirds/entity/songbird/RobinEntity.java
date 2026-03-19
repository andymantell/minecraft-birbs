package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.ai.flight.FlutteringFlightGoal;
import com.birbs.britishbirds.ai.movement.HoppingMovementGoal;
import com.birbs.britishbirds.ai.movement.PerchingGoal;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import com.birbs.britishbirds.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RobinEntity extends AbstractFlyingBird {

    public RobinEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new PerchingGoal(this, 8, 100, 300));
        this.goalSelector.addGoal(4, new FlutteringFlightGoal(this, 1.0, 3, 10));
        this.goalSelector.addGoal(5, new HoppingMovementGoal(this, 0.3, 0.3));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createRobinAttributes() {
        return createFlyingBirdAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.35);
    }

    @Nullable
    @Override
    public SoundEvent getSongSound() {
        return ModSounds.ROBIN_SONG;
    }

    @Nullable
    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.ROBIN_ALARM;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ROBIN_CALL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ROBIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBIN_DEATH;
    }
}
