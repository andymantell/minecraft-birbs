package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class BirdModelLayers {
    public static final ModelLayerLocation ROBIN = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"), "main");

    public static final ModelLayerLocation BLUE_TIT = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "blue_tit"), "main");

    public static final ModelLayerLocation BARN_OWL = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "barn_owl"), "main");

    public static final ModelLayerLocation PEREGRINE_FALCON = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "peregrine_falcon"), "main");

    public static final ModelLayerLocation MALLARD = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "mallard"), "main");
}
