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

    public static final SoundEvent BARN_OWL_SCREECH = registerSound("entity.barn_owl.screech");
    public static final SoundEvent BARN_OWL_HISS = registerSound("entity.barn_owl.hiss");
    public static final SoundEvent BARN_OWL_CHIRRUP = registerSound("entity.barn_owl.chirrup");
    public static final SoundEvent BARN_OWL_HURT = registerSound("entity.barn_owl.hurt");
    public static final SoundEvent BARN_OWL_DEATH = registerSound("entity.barn_owl.death");

    public static final SoundEvent PEREGRINE_KAK = registerSound("entity.peregrine_falcon.kak");
    public static final SoundEvent PEREGRINE_CHITTER = registerSound("entity.peregrine_falcon.chitter");
    public static final SoundEvent PEREGRINE_EECHIP = registerSound("entity.peregrine_falcon.eechip");
    public static final SoundEvent PEREGRINE_HURT = registerSound("entity.peregrine_falcon.hurt");
    public static final SoundEvent PEREGRINE_DEATH = registerSound("entity.peregrine_falcon.death");

    public static final SoundEvent MALLARD_QUACK = registerSound("entity.mallard.quack");
    public static final SoundEvent MALLARD_RAEB = registerSound("entity.mallard.raeb");
    public static final SoundEvent MALLARD_WING_WHISTLE = registerSound("entity.mallard.wing_whistle");
    public static final SoundEvent MALLARD_HURT = registerSound("entity.mallard.hurt");
    public static final SoundEvent MALLARD_DEATH = registerSound("entity.mallard.death");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds sounds...");
    }
}
