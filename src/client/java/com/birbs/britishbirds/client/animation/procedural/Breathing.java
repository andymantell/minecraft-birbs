package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Subtle breathing animation: chest expansion/contraction with
 * corresponding scapular movement.
 */
public class Breathing implements ProceduralBehaviour {

    private final float breathRate;

    /**
     * @param breathRate phase multiplier; 0.15f for small passerines, 0.05f for large birds
     */
    public Breathing(float breathRate) {
        this.breathRate = breathRate;
    }

    public Breathing() {
        this(0.15f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        float breathPhase = state.ageInTicks * breathRate;
        float expansion = (float) Math.sin(breathPhase) * 0.015f;

        BirdJoint chest = skeleton.getJoint(BirdSkeleton.CHEST);
        BirdJoint torso = skeleton.getJoint(BirdSkeleton.TORSO);
        BirdJoint lScap = skeleton.getJoint(BirdSkeleton.L_SCAPULARS);
        BirdJoint rScap = skeleton.getJoint(BirdSkeleton.R_SCAPULARS);

        if (chest != null) chest.targetX += expansion;
        if (torso != null) torso.targetX -= expansion * 0.5f;
        if (lScap != null) lScap.targetZ -= expansion * 0.3f;
        if (rScap != null) rScap.targetZ += expansion * 0.3f;
    }
}
