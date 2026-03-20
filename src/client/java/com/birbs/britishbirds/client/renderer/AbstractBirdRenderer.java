package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Base renderer for all British bird species.
 * Handles common render state extraction: sex, flight detection, and wing flap angle.
 * Subclasses provide species-specific flap parameters and additional state extraction.
 */
public abstract class AbstractBirdRenderer<E extends AbstractBritishBird, S extends BirdRenderState, M extends EntityModel<S>>
        extends MobRenderer<E, S, M> {

    /** Tracks per-entity flying state from the previous frame for edge detection. */
    private final Map<Integer, Boolean> prevFlyingState = new HashMap<>();

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

        // Skeletal animation fields
        state.entityId = entity.getId();
        state.yawDelta = entity.getYRot() - entity.yRotO;
        state.verticalVelocity = (float) entity.getDeltaMovement().y;
        state.speed = (float) entity.getDeltaMovement().horizontalDistance();
        boolean wasFlying = prevFlyingState.getOrDefault(entity.getId(), false);
        state.justLanded = wasFlying && !state.isFlying && entity.onGround();
        prevFlyingState.put(entity.getId(), state.isFlying);
        state.justStartled = false; // stub — species extractors will set this later
        state.lookTarget = findLookTarget(entity, state);

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

    /**
     * Find the nearest LivingEntity within 8 blocks for head-tracking.
     * Throttled to recompute only every 5 ticks to avoid per-frame entity scans.
     */
    protected Vec3 findLookTarget(E entity, S state) {
        if (entity.tickCount % 5 != 0) {
            return state.lookTarget; // return cached value from previous extraction
        }
        AABB searchBox = entity.getBoundingBox().inflate(8.0);
        var nearby = entity.level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != entity);
        Vec3 entityPos = entity.position();
        double closestDist = Double.MAX_VALUE;
        LivingEntity closest = null;
        for (LivingEntity candidate : nearby) {
            double dist = candidate.position().distanceToSqr(entityPos);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest != null ? closest.position() : null;
    }
}
