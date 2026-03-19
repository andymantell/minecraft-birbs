package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BarnOwlModel;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.entity.raptor.BarnOwlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BarnOwlRenderer extends AbstractBirdRenderer<BarnOwlEntity, BarnOwlRenderState, BarnOwlModel> {
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
    protected float flapFrequency() { return 0.35f; }

    @Override
    protected float flapAmplitude() { return 1.4f; }

    @Override
    protected void extractSpeciesState(BarnOwlEntity entity, BarnOwlRenderState state, float partialTick) {
        state.isHovering = entity.isHovering();
    }

    @Override
    public Identifier getTextureLocation(BarnOwlRenderState state) {
        return state.isMale ? BARN_OWL_MALE_TEXTURE : BARN_OWL_FEMALE_TEXTURE;
    }
}
