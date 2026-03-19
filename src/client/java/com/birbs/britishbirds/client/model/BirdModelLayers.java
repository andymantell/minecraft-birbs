package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class BirdModelLayers {
    public static final ModelLayerLocation ROBIN = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"), "main");

    public static final ModelLayerLocation BLUE_TIT = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "blue_tit"), "main");
}
