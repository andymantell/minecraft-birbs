package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Adds lateral weight-shift while walking and aperiodic idle sway
 * when standing still, so the bird never looks frozen.
 */
public class WeightShift implements ProceduralBehaviour {

    private final float swayAmplitude;

    /**
     * @param swayAmplitude idle sway amplitude (default 0.002f)
     */
    public WeightShift(float swayAmplitude) {
        this.swayAmplitude = swayAmplitude;
    }

    public WeightShift() {
        this(0.002f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        BirdJoint chest = skeleton.getJoint(BirdSkeleton.CHEST);
        BirdJoint hip   = skeleton.getJoint(BirdSkeleton.HIP);

        if (state.walkAnimationSpeed > 0.01f) {
            // Walking: lateral lean synced to walk cycle
            float leanZ = (float) Math.sin(state.walkAnimationPos * 0.6662f)
                    * state.walkAnimationSpeed * 0.08f;
            if (chest != null) chest.targetZ += leanZ;
            if (hip   != null) hip.targetZ   += leanZ * 0.5f;
        } else {
            // Idle: aperiodic sway using two incommensurate frequencies
            float sway = (float) (Math.sin(state.ageInTicks * 0.023f)
                    * Math.sin(state.ageInTicks * 0.037f)) * swayAmplitude;
            if (chest != null) {
                chest.targetZ += sway;
                chest.targetX += sway * 0.5f;
            }
        }
    }
}
