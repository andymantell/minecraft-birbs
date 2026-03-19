package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.BlueTitModel;
import com.birbs.britishbirds.entity.songbird.BlueTitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BlueTitRenderer extends AbstractBirdRenderer<BlueTitEntity, BlueTitRenderState, BlueTitModel> {
    private static final Identifier BLUE_TIT_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/blue_tit/blue_tit.png");

    public BlueTitRenderer(EntityRendererProvider.Context context) {
        super(context, new BlueTitModel(context.bakeLayer(BirdModelLayers.BLUE_TIT)), 0.25f);
    }

    @Override
    public BlueTitRenderState createRenderState() {
        return new BlueTitRenderState();
    }

    @Override
    protected float flapFrequency() { return 2.0f; }

    @Override
    protected float flapAmplitude() { return 0.8f; }

    @Override
    protected void extractSpeciesState(BlueTitEntity entity, BlueTitRenderState state, float partialTick) {
        state.isPecking = entity.isPecking();
        state.isHangingUpsideDown = entity.isHangingUpsideDown();
    }

    @Override
    public Identifier getTextureLocation(BlueTitRenderState state) {
        return BLUE_TIT_TEXTURE;
    }
}
