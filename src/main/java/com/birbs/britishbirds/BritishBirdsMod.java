package com.birbs.britishbirds;

import com.birbs.britishbirds.registry.ModEntities;
import com.birbs.britishbirds.registry.ModItems;
import com.birbs.britishbirds.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BritishBirdsMod implements ModInitializer {
    public static final String MOD_ID = "britishbirds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("British Birds mod initializing...");
        ModSounds.initialize();
        ModEntities.initialize();
        ModItems.initialize();
    }
}
