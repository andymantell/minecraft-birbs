package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {

    public static final Item ROBIN_SPAWN_EGG = registerSpawnEgg("robin_spawn_egg", ModEntities.ROBIN);
    public static final Item BLUE_TIT_SPAWN_EGG = registerSpawnEgg("blue_tit_spawn_egg", ModEntities.BLUE_TIT);
    public static final Item BARN_OWL_SPAWN_EGG = registerSpawnEgg("barn_owl_spawn_egg", ModEntities.BARN_OWL);
    public static final Item PEREGRINE_FALCON_SPAWN_EGG = registerSpawnEgg("peregrine_falcon_spawn_egg", ModEntities.PEREGRINE_FALCON);
    public static final Item MALLARD_SPAWN_EGG = registerSpawnEgg("mallard_spawn_egg", ModEntities.MALLARD);

    private static Item registerSpawnEgg(String name, EntityType<?> entityType) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, name));
        Item.Properties props = new Item.Properties()
                .setId(key)
                .useItemDescriptionPrefix()
                .spawnEgg(entityType);
        return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(props));
    }

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds items...");
    }
}
