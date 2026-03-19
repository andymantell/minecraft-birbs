package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.PeregrineFalconModel;
import com.birbs.britishbirds.entity.raptor.PeregrineFalconEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class PeregrineFalconRenderer extends AbstractBirdRenderer<PeregrineFalconEntity, PeregrineFalconRenderState, PeregrineFalconModel> {
    private static final Identifier PEREGRINE_ADULT_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/peregrine_falcon/peregrine_adult.png");
    private static final Identifier PEREGRINE_JUVENILE_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/peregrine_falcon/peregrine_juvenile.png");

    public PeregrineFalconRenderer(EntityRendererProvider.Context context) {
        super(context, new PeregrineFalconModel(context.bakeLayer(BirdModelLayers.PEREGRINE_FALCON)), 0.4f);
    }

    @Override
    public PeregrineFalconRenderState createRenderState() {
        return new PeregrineFalconRenderState();
    }

    @Override
    protected float flapFrequency() { return 1.0f; }

    @Override
    protected float flapAmplitude() { return 0.7f; }

    @Override
    protected boolean shouldComputeFlap(PeregrineFalconRenderState state) {
        return !state.isStooping;
    }

    @Override
    protected void extractSpeciesState(PeregrineFalconEntity entity, PeregrineFalconRenderState state, float partialTick) {
        state.isStooping = entity.isStooping();
    }

    @Override
    public Identifier getTextureLocation(PeregrineFalconRenderState state) {
        return state.isBaby ? PEREGRINE_JUVENILE_TEXTURE : PEREGRINE_ADULT_TEXTURE;
    }
}
