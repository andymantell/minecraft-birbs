package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.entity.songbird.BlueTitEntity;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final ResourceKey<EntityType<?>> ROBIN_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"));

    public static final EntityType<RobinEntity> ROBIN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"),
            EntityType.Builder.<RobinEntity>of(RobinEntity::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f)
                    .clientTrackingRange(10)
                    .build(ROBIN_KEY)
    );

    public static final ResourceKey<EntityType<?>> BLUE_TIT_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "blue_tit"));

    public static final EntityType<BlueTitEntity> BLUE_TIT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "blue_tit"),
            EntityType.Builder.<BlueTitEntity>of(BlueTitEntity::new, MobCategory.CREATURE)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(10)
                    .build(BLUE_TIT_KEY)
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds entities...");
        FabricDefaultAttributeRegistry.register(ROBIN, RobinEntity.createRobinAttributes());
        FabricDefaultAttributeRegistry.register(BLUE_TIT, BlueTitEntity.createBlueTitAttributes());
    }
}
