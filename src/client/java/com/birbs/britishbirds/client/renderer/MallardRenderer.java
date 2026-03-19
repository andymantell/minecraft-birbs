package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.MallardModel;
import com.birbs.britishbirds.entity.waterfowl.MallardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class MallardRenderer extends MobRenderer<MallardEntity, MallardRenderState, MallardModel> {
    private static final Identifier MALLARD_MALE_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/mallard/mallard_male.png");
    private static final Identifier MALLARD_FEMALE_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/mallard/mallard_female.png");
    private static final Identifier MALLARD_DUCKLING_TEXTURE =
            Identifier.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "textures/entity/mallard/mallard_duckling.png");

    public MallardRenderer(EntityRendererProvider.Context context) {
        super(context, new MallardModel(context.bakeLayer(BirdModelLayers.MALLARD)), 0.4f);
    }

    @Override
    public MallardRenderState createRenderState() {
        return new MallardRenderState();
    }

    @Override
    public void extractRenderState(MallardEntity entity, MallardRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isFlying = entity.isFlying();
        state.isSwimming = entity.isSwimming();
        state.isDabbling = entity.isDabbling();
        state.isWaddling = entity.isWaddling();
        state.isBaby = entity.isBaby();
        if (state.isFlying) {
            // Rapid stiff wingbeats
            state.flapAngle = (float) Math.sin(state.ageInTicks * 1.5f) * 0.9f;
        } else {
            state.flapAngle = 0.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(MallardRenderState state) {
        if (state.isBaby) {
            return MALLARD_DUCKLING_TEXTURE;
        }
        return state.isMale ? MALLARD_MALE_TEXTURE : MALLARD_FEMALE_TEXTURE;
    }
}
