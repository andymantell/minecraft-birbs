package com.birbs.britishbirds.world;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Registers spawn rules for all British Birds species.
 */
public class BirdSpawnRules {

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds spawn rules...");
        registerRobinSpawns();
    }

    private static void registerRobinSpawns() {
        // Add robin spawns to appropriate biomes
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FOREST,
                        Biomes.FLOWER_FOREST,
                        Biomes.BIRCH_FOREST,
                        Biomes.OLD_GROWTH_BIRCH_FOREST,
                        Biomes.DARK_FOREST,
                        Biomes.CHERRY_GROVE,
                        Biomes.TAIGA,
                        Biomes.PLAINS,
                        Biomes.MEADOW
                ),
                MobCategory.CREATURE,
                ModEntities.ROBIN,
                10,  // weight
                1,   // min group size
                2    // max group size
        );

        // Register spawn placement rules
        net.minecraft.world.entity.SpawnPlacements.register(
                ModEntities.ROBIN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
    }
}
