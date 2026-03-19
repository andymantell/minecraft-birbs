package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent ROBIN_SONG = registerSound("entity.robin.song");
    public static final SoundEvent ROBIN_CALL = registerSound("entity.robin.call");
    public static final SoundEvent ROBIN_ALARM = registerSound("entity.robin.alarm");
    public static final SoundEvent ROBIN_HURT = registerSound("entity.robin.hurt");
    public static final SoundEvent ROBIN_DEATH = registerSound("entity.robin.death");

    public static final SoundEvent BLUE_TIT_SONG = registerSound("entity.blue_tit.song");
    public static final SoundEvent BLUE_TIT_CALL = registerSound("entity.blue_tit.call");
    public static final SoundEvent BLUE_TIT_ALARM = registerSound("entity.blue_tit.alarm");
    public static final SoundEvent BLUE_TIT_HURT = registerSound("entity.blue_tit.hurt");
    public static final SoundEvent BLUE_TIT_DEATH = registerSound("entity.blue_tit.death");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds sounds...");
    }
}
