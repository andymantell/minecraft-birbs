package com.birbs.britishbirds.client;

import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.BlueTitModel;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.client.renderer.BlueTitRenderer;
import com.birbs.britishbirds.client.renderer.RobinRenderer;
import com.birbs.britishbirds.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class BritishBirdsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ROBIN, RobinRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.ROBIN, RobinModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.BLUE_TIT, BlueTitRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.BLUE_TIT, BlueTitModel::createBodyLayer);
    }
}
