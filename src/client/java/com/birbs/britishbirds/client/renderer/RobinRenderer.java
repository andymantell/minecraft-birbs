package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class RobinRenderer extends MobRenderer<RobinEntity, RobinRenderState, RobinModel> {
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
    public void extractRenderState(RobinEntity entity, RobinRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isFlying = entity.isFlying() || (!entity.onGround() && !entity.isInWater());
        state.isPecking = entity.isPecking();
        if (state.isFlying) {
            state.flapAngle = (float) Math.sin(state.ageInTicks * 1.4f) * 1.2f;
        } else {
            state.flapAngle = 0.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(RobinRenderState state) {
        return state.isBaby ? ROBIN_BABY_TEXTURE : ROBIN_TEXTURE;
    }
}
