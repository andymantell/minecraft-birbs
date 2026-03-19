package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

/**
 * Base renderer for all British bird species.
 * Handles common render state extraction: sex, flight detection, and wing flap angle.
 * Subclasses provide species-specific flap parameters and additional state extraction.
 */
public abstract class AbstractBirdRenderer<E extends AbstractBritishBird, S extends BirdRenderState, M extends EntityModel<S>>
        extends MobRenderer<E, S, M> {

    protected AbstractBirdRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void extractRenderState(E entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isBaby = entity.isBaby();
        state.isFlying = detectFlying(entity);

        if (state.isFlying && shouldComputeFlap(state)) {
            state.flapAngle = (float) Math.sin(state.ageInTicks * flapFrequency()) * flapAmplitude();
        } else {
            state.flapAngle = 0.0f;
        }

        extractSpeciesState(entity, state, partialTick);
    }

    /** Detect if the bird is airborne. Works for both flying birds and water birds. */
    protected boolean detectFlying(E entity) {
        return entity.isFlying() || (!entity.onGround() && !entity.isInWater());
    }

    /** Flap frequency multiplier (higher = faster wingbeats). */
    protected abstract float flapFrequency();

    /** Flap amplitude (higher = wider wing sweep). */
    protected abstract float flapAmplitude();

    /** Override to suppress flap computation in certain states (e.g. stooping). */
    protected boolean shouldComputeFlap(S state) {
        return true;
    }

    /** Extract any species-specific render state fields. Called after common extraction. */
    protected void extractSpeciesState(E entity, S state, float partialTick) {
        // Default: nothing extra. Override in subclasses.
    }
}
