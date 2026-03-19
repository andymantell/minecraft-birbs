package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.ai.feeding.FollowDiggingPlayerGoal;
import com.birbs.britishbirds.ai.social.TerritorialGoal;
import com.birbs.britishbirds.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RobinEntity extends SmallPasserineEntity {

    public RobinEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerSpeciesGoals() {
        this.goalSelector.addGoal(2, new FollowDiggingPlayerGoal(this, 12.0, 1.2));
        this.goalSelector.addGoal(2, new TerritorialGoal(this, 16.0, 1.3));
    }

    public static AttributeSupplier.Builder createRobinAttributes() {
        return createFlyingBirdAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.35);
    }

    @Override
    public int getAmbientSoundInterval() {
        long dayTime = this.level().getOverworldClockTime() % 24000;
        // Dawn (0-2000) and dusk (11000-13000): sing more frequently
        if (dayTime < 2000 || (dayTime >= 11000 && dayTime < 13000)) {
            return 80; // 4 seconds
        }
        return 400; // 20 seconds
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
