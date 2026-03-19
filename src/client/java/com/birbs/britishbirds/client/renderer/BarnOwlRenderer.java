package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BarnOwlModel;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.entity.raptor.BarnOwlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class BarnOwlRenderer extends MobRenderer<BarnOwlEntity, BarnOwlRenderState, BarnOwlModel> {
    private static final Identifier BARN_OWL_MALE_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/barn_owl/barn_owl_male.png");
    private static final Identifier BARN_OWL_FEMALE_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/barn_owl/barn_owl_female.png");

    public BarnOwlRenderer(EntityRendererProvider.Context context) {
        super(context, new BarnOwlModel(context.bakeLayer(BirdModelLayers.BARN_OWL)), 0.4f);
    }

    @Override
    public BarnOwlRenderState createRenderState() {
        return new BarnOwlRenderState();
    }

    @Override
    public void extractRenderState(BarnOwlEntity entity, BarnOwlRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isFlying = entity.isFlying() || (!entity.onGround() && !entity.isInWater());
        state.isHovering = entity.isHovering();
        if (state.isFlying) {
            // Very slow, deep wing sweeps — silent flight
            state.flapAngle = (float) Math.sin(state.ageInTicks * 0.35f) * 1.4f;
        } else {
            state.flapAngle = 0.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(BarnOwlRenderState state) {
        return state.isMale ? BARN_OWL_MALE_TEXTURE : BARN_OWL_FEMALE_TEXTURE;
    }
}
