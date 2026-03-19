package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RobinRenderer extends AbstractBirdRenderer<RobinEntity, RobinRenderState, RobinModel> {
    private static final Identifier ROBIN_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/robin/robin.png");
    private static final Identifier ROBIN_BABY_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/robin/robin_baby.png");

    public RobinRenderer(EntityRendererProvider.Context context) {
        super(context, new RobinModel(context.bakeLayer(BirdModelLayers.ROBIN)), 0.3f);
    }

    @Override
    public RobinRenderState createRenderState() {
        return new RobinRenderState();
    }

    @Override
    protected float flapFrequency() { return 1.8f; }

    @Override
    protected float flapAmplitude() { return 0.9f; }

    @Override
    protected void extractSpeciesState(RobinEntity entity, RobinRenderState state, float partialTick) {
        state.isPecking = entity.isPecking();
    }

    @Override
    public Identifier getTextureLocation(RobinRenderState state) {
        return state.isBaby ? ROBIN_BABY_TEXTURE : ROBIN_TEXTURE;
    }
}
