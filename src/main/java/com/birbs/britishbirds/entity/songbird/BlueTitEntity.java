package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.ai.movement.AcrobaticClingingGoal;
import com.birbs.britishbirds.ai.social.FlockingGoal;
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

public class BlueTitEntity extends SmallPasserineEntity {

    private static final EntityDataAccessor<Boolean> IS_HANGING_UPSIDE_DOWN =
            SynchedEntityData.defineId(BlueTitEntity.class, EntityDataSerializers.BOOLEAN);

    public BlueTitEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HANGING_UPSIDE_DOWN, false);
    }

    @Override
    protected void registerSpeciesGoals() {
        this.goalSelector.addGoal(2, new AcrobaticClingingGoal(this, 6));
        this.goalSelector.addGoal(2, new FlockingGoal(this, 16.0, 2.0));
    }

    public static AttributeSupplier.Builder createBlueTitAttributes() {
        return createFlyingBirdAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    public boolean isHangingUpsideDown() {
        return this.entityData.get(IS_HANGING_UPSIDE_DOWN);
    }

    public void setHangingUpsideDown(boolean hanging) {
        this.entityData.set(IS_HANGING_UPSIDE_DOWN, hanging);
    }

    @Override
    public int getAmbientSoundInterval() {
        long dayTime = this.level().getOverworldClockTime() % 24000;
        // Dawn (0-2000) and dusk (11000-13000): sing more frequently
        // Slightly later than Robin (interval 100 vs 80)
        if (dayTime < 2000 || (dayTime >= 11000 && dayTime < 13000)) {
            return 100; // 5 seconds
        }
        return 500; // 25 seconds
    }

    @Nullable
    @Override
    public SoundEvent getSongSound() {
        return ModSounds.BLUE_TIT_SONG;
    }

    @Nullable
    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.BLUE_TIT_ALARM;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BLUE_TIT_CALL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BLUE_TIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BLUE_TIT_DEATH;
    }
}
