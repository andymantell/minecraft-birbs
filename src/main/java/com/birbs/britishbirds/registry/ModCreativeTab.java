package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {
    public static final CreativeModeTab BRITISH_BIRDS_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "british_birds"),
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.britishbirds.british_birds"))
                    .icon(() -> new ItemStack(ModItems.ROBIN_SPAWN_EGG))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ROBIN_SPAWN_EGG);
                        output.accept(ModItems.BLUE_TIT_SPAWN_EGG);
                        output.accept(ModItems.BARN_OWL_SPAWN_EGG);
                        output.accept(ModItems.PEREGRINE_FALCON_SPAWN_EGG);
                        output.accept(ModItems.MALLARD_SPAWN_EGG);
                    })
                    .build()
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds creative tab...");
    }
}
