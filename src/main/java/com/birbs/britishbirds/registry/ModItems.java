package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
    public static final Item ROBIN_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin_spawn_egg"),
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.ROBIN))
    );

    public static final Item BLUE_TIT_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "blue_tit_spawn_egg"),
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.BLUE_TIT))
    );

    public static final Item BARN_OWL_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "barn_owl_spawn_egg"),
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.BARN_OWL))
    );

    public static final Item PEREGRINE_FALCON_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "peregrine_falcon_spawn_egg"),
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.PEREGRINE_FALCON))
    );

    public static final Item MALLARD_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "mallard_spawn_egg"),
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.MALLARD))
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds items...");
    }
}
