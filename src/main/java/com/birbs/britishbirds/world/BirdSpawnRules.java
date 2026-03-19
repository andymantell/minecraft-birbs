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
        registerBlueTitSpawns();
        registerBarnOwlSpawns();
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

    private static void registerBlueTitSpawns() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FOREST,
                        Biomes.BIRCH_FOREST,
                        Biomes.FLOWER_FOREST,
                        Biomes.PLAINS,
                        Biomes.MEADOW
                ),
                MobCategory.CREATURE,
                ModEntities.BLUE_TIT,
                12,  // weight (slightly more common than Robin)
                2,   // min group size
                4    // max group size
        );

        net.minecraft.world.entity.SpawnPlacements.register(
                ModEntities.BLUE_TIT,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
    }

    private static void registerBarnOwlSpawns() {
        // Barn Owl: plains and meadow biomes only, NOT forests. Rare, solitary.
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.PLAINS,
                        Biomes.SUNFLOWER_PLAINS,
                        Biomes.MEADOW
                ),
                MobCategory.CREATURE,
                ModEntities.BARN_OWL,
                3,   // weight (rare)
                1,   // min group size (solitary)
                1    // max group size (solitary)
        );

        net.minecraft.world.entity.SpawnPlacements.register(
                ModEntities.BARN_OWL,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
    }
}
