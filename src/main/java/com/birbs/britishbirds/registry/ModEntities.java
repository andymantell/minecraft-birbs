package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.entity.raptor.BarnOwlEntity;
import com.birbs.britishbirds.entity.raptor.PeregrineFalconEntity;
import com.birbs.britishbirds.entity.songbird.BlueTitEntity;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import com.birbs.britishbirds.entity.waterfowl.MallardEntity;
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

    public static final ResourceKey<EntityType<?>> BARN_OWL_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "barn_owl"));

    public static final EntityType<BarnOwlEntity> BARN_OWL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "barn_owl"),
            EntityType.Builder.<BarnOwlEntity>of(BarnOwlEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .build(BARN_OWL_KEY)
    );

    public static final ResourceKey<EntityType<?>> PEREGRINE_FALCON_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "peregrine_falcon"));

    public static final EntityType<PeregrineFalconEntity> PEREGRINE_FALCON = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "peregrine_falcon"),
            EntityType.Builder.<PeregrineFalconEntity>of(PeregrineFalconEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.6f)
                    .clientTrackingRange(10)
                    .build(PEREGRINE_FALCON_KEY)
    );

    public static final ResourceKey<EntityType<?>> MALLARD_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "mallard"));

    public static final EntityType<MallardEntity> MALLARD = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "mallard"),
            EntityType.Builder.<MallardEntity>of(MallardEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(10)
                    .build(MALLARD_KEY)
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds entities...");
        FabricDefaultAttributeRegistry.register(ROBIN, RobinEntity.createRobinAttributes());
        FabricDefaultAttributeRegistry.register(BLUE_TIT, BlueTitEntity.createBlueTitAttributes());
        FabricDefaultAttributeRegistry.register(BARN_OWL, BarnOwlEntity.createBarnOwlAttributes());
        FabricDefaultAttributeRegistry.register(PEREGRINE_FALCON, PeregrineFalconEntity.createPeregrineAttributes());
        FabricDefaultAttributeRegistry.register(MALLARD, MallardEntity.createMallardAttributes());
    }
}
