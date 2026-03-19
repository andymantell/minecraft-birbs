package com.birbs.britishbirds.entity.waterfowl;

import com.birbs.britishbirds.ai.social.DucklingFollowGoal;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MallardEntity extends WaterfowlEntity {

    private static final EntityDataAccessor<Boolean> IS_DABBLING =
            SynchedEntityData.defineId(MallardEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean isWaddling = false;

    public MallardEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_DABBLING, false);
    }

    @Override
    protected void registerSpeciesGoals() {
        this.goalSelector.addGoal(1, new DucklingFollowGoal(this));
    }

    public static AttributeSupplier.Builder createMallardAttributes() {
        return createWaterBirdAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.35);
    }

    @Override
    public boolean hasSexualDimorphism() {
        return true;
    }

    public boolean isDabbling() {
        return this.entityData.get(IS_DABBLING);
    }

    public void setDabbling(boolean dabbling) {
        this.entityData.set(IS_DABBLING, dabbling);
    }

    public boolean isWaddling() {
        return this.isWaddling;
    }

    public void setWaddling(boolean waddling) {
        this.isWaddling = waddling;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Nullable
    @Override
    public SoundEvent getSongSound() {
        return ModSounds.MALLARD_QUACK;
    }

    @Nullable
    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.MALLARD_QUACK;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        // Randomly pick between quack and raeb
        if (this.random.nextBoolean()) {
            return ModSounds.MALLARD_QUACK;
        }
        return ModSounds.MALLARD_RAEB;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MALLARD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MALLARD_DEATH;
    }
}
