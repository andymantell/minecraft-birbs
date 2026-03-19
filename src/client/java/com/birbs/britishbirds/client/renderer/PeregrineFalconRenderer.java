package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.PeregrineFalconModel;
import com.birbs.britishbirds.entity.raptor.PeregrineFalconEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class PeregrineFalconRenderer extends MobRenderer<PeregrineFalconEntity, PeregrineFalconRenderState, PeregrineFalconModel> {
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
    public void extractRenderState(PeregrineFalconEntity entity, PeregrineFalconRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isFlying = entity.isFlying() || (!entity.onGround() && !entity.isInWater());
        state.isStooping = entity.isStooping();
        if (state.isFlying && !state.isStooping) {
            // Stiff, snappy shallow wingbeats
            state.flapAngle = (float) Math.sin(state.ageInTicks * 1.0f) * 0.7f;
        } else {
            state.flapAngle = 0.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(PeregrineFalconRenderState state) {
        // Use adult texture for both sexes (same plumage); juvenile could be used for babies
        return PEREGRINE_ADULT_TEXTURE;
    }
}
