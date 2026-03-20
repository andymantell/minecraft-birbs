package com.birbs.britishbirds.client;

import com.birbs.britishbirds.client.animation.pose.PoseLoader;
import com.birbs.britishbirds.client.model.BarnOwlModel;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.BlueTitModel;
import com.birbs.britishbirds.client.model.MallardModel;
import com.birbs.britishbirds.client.model.PeregrineFalconModel;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.client.renderer.BarnOwlRenderer;
import com.birbs.britishbirds.client.renderer.BlueTitRenderer;
import com.birbs.britishbirds.client.renderer.MallardRenderer;
import com.birbs.britishbirds.client.renderer.PeregrineFalconRenderer;
import com.birbs.britishbirds.client.renderer.RobinRenderer;
import com.birbs.britishbirds.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class BritishBirdsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Load JSON pose data before registering models/renderers
        PoseLoader.loadAll();

        EntityRendererRegistry.register(ModEntities.ROBIN, RobinRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.ROBIN, RobinModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.BLUE_TIT, BlueTitRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.BLUE_TIT, BlueTitModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.BARN_OWL, BarnOwlRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.BARN_OWL, BarnOwlModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.PEREGRINE_FALCON, PeregrineFalconRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.PEREGRINE_FALCON, PeregrineFalconModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.MALLARD, MallardRenderer::new);
        ModelLayerRegistry.registerModelLayer(BirdModelLayers.MALLARD, MallardModel::createBodyLayer);
    }
}
