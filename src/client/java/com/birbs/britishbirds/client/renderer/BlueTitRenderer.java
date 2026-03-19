package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.BlueTitModel;
import com.birbs.britishbirds.entity.songbird.BlueTitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class BlueTitRenderer extends MobRenderer<BlueTitEntity, BlueTitRenderState, BlueTitModel> {
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
    public void extractRenderState(BlueTitEntity entity, BlueTitRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isFlying = entity.isFlying() || (!entity.onGround() && !entity.isInWater());
        state.isPecking = entity.isPecking();
        state.isHangingUpsideDown = entity.isHangingUpsideDown();
        if (state.isFlying) {
            // Very rapid whirring wingbeats — fastest of all species
            state.flapAngle = (float) Math.sin(state.ageInTicks * 2.0f) * 0.8f;
        } else {
            state.flapAngle = 0.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(BlueTitRenderState state) {
        return BLUE_TIT_TEXTURE;
    }
}
