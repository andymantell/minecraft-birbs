package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
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

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds entities...");
        FabricDefaultAttributeRegistry.register(ROBIN, RobinEntity.createRobinAttributes());
    }
}
