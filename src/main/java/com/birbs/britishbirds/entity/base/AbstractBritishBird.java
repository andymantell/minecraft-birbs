package com.birbs.britishbirds.entity.base;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBritishBird extends Animal {
    private static final EntityDataAccessor<Boolean> IS_MALE =
            SynchedEntityData.defineId(AbstractBritishBird.class, EntityDataSerializers.BOOLEAN);

    protected AbstractBritishBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_MALE, true);
    }

    public boolean isMale() {
        return this.entityData.get(IS_MALE);
    }

    public void setMale(boolean male) {
        this.entityData.set(IS_MALE, male);
    }

    /**
     * Whether this bird is currently in flight. Overridden by AbstractFlyingBird
     * and AbstractWaterBird with tracked data. Base returns false.
     */
    public boolean isFlying() {
        return false;
    }

    /**
     * Whether this species has visible differences between male and female.
     * Override to return true for species like Mallard.
     */
    public boolean hasSexualDimorphism() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsMale", this.isMale());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setMale(input.getBooleanOr("IsMale", true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        this.setMale(this.random.nextBoolean());
        return spawnData;
    }

    public static AttributeSupplier.Builder createBirdAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    // Abstract sound hooks for subclasses
    @Nullable
    public abstract SoundEvent getSongSound();

    @Nullable
    public abstract SoundEvent getAlarmSound();

    @Nullable
    @Override
    protected abstract SoundEvent getAmbientSound();

    @Override
    protected abstract SoundEvent getHurtSound(DamageSource source);

    @Override
    protected abstract SoundEvent getDeathSound();

    @Override
    public boolean isFood(ItemStack stack) {
        return false; // Breeding foods will be implemented in later phases
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null; // Breeding will be implemented in later phases
    }
}
