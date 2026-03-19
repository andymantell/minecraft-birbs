package com.birbs.britishbirds.entity.raptor;

import com.birbs.britishbirds.ai.feeding.HuntingGoal;
import com.birbs.britishbirds.ai.flight.SoaringFlightGoal;
import com.birbs.britishbirds.ai.flight.StoopingFlightGoal;
import com.birbs.britishbirds.entity.songbird.SmallPasserineEntity;
import com.birbs.britishbirds.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PeregrineFalconEntity extends RaptorEntity {

    private static final EntityDataAccessor<Boolean> IS_STOOPING =
            SynchedEntityData.defineId(PeregrineFalconEntity.class, EntityDataSerializers.BOOLEAN);

    public PeregrineFalconEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_STOOPING, false);
    }

    @Override
    protected void registerSpeciesGoals() {
        this.goalSelector.addGoal(1, new StoopingFlightGoal(this));
        this.goalSelector.addGoal(2, new SoaringFlightGoal(this));
        this.goalSelector.addGoal(4, new HuntingGoal(this, SmallPasserineEntity.class, 20.0, 1.2));
    }

    public static AttributeSupplier.Builder createPeregrineAttributes() {
        return createRaptorAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    public boolean hasSexualDimorphism() {
        return true;
    }

    public boolean isStooping() {
        return this.entityData.get(IS_STOOPING);
    }

    public void setStooping(boolean stooping) {
        this.entityData.set(IS_STOOPING, stooping);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 800;
    }

    @Nullable
    @Override
    public SoundEvent getSongSound() {
        return ModSounds.PEREGRINE_KAK;
    }

    @Nullable
    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.PEREGRINE_KAK;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        // Randomly pick between chitter and eechip
        if (this.random.nextBoolean()) {
            return ModSounds.PEREGRINE_CHITTER;
        }
        return ModSounds.PEREGRINE_EECHIP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PEREGRINE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PEREGRINE_DEATH;
    }
}
