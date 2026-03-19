package com.birbs.britishbirds.entity.raptor;

import com.birbs.britishbirds.ai.feeding.HuntingGoal;
import com.birbs.britishbirds.ai.flight.QuarteringFlightGoal;
import com.birbs.britishbirds.ai.schedule.ActivityScheduleGoal;
import com.birbs.britishbirds.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BarnOwlEntity extends RaptorEntity {

    private static final EntityDataAccessor<Boolean> IS_HOVERING =
            SynchedEntityData.defineId(BarnOwlEntity.class, EntityDataSerializers.BOOLEAN);

    public BarnOwlEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HOVERING, false);
    }

    @Override
    protected void registerSpeciesGoals() {
        this.goalSelector.addGoal(0, new ActivityScheduleGoal(this, ActivityScheduleGoal.Mode.NOCTURNAL));
        this.goalSelector.addGoal(2, new QuarteringFlightGoal(this));
        this.goalSelector.addGoal(4, new HuntingGoal(this, Rabbit.class, 16.0, 1.2));
    }

    public static AttributeSupplier.Builder createBarnOwlAttributes() {
        return createRaptorAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.3);
    }

    @Override
    public boolean hasSexualDimorphism() {
        return true;
    }

    public boolean isHovering() {
        return this.entityData.get(IS_HOVERING);
    }

    public void setHovering(boolean hovering) {
        this.entityData.set(IS_HOVERING, hovering);
    }

    @Override
    public int getAmbientSoundInterval() {
        long dayTime = this.level().getOverworldClockTime() % 24000;
        // Active at night: more frequent calls
        if (dayTime >= 12000) {
            return 600; // 30 seconds
        }
        // Silent during day (roosting)
        return Integer.MAX_VALUE;
    }

    @Nullable
    @Override
    public SoundEvent getSongSound() {
        return ModSounds.BARN_OWL_SCREECH;
    }

    @Nullable
    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.BARN_OWL_HISS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BARN_OWL_CHIRRUP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BARN_OWL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BARN_OWL_DEATH;
    }
}
