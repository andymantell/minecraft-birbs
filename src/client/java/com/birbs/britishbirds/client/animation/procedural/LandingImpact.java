package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Injects one-shot velocity impulses when the bird has just landed,
 * creating a satisfying impact ripple through the skeleton.
 */
public class LandingImpact implements ProceduralBehaviour {

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (!state.justLanded) return;

        BirdJoint chest     = skeleton.getJoint(BirdSkeleton.CHEST);
        BirdJoint hip       = skeleton.getJoint(BirdSkeleton.HIP);
        BirdJoint tailBase  = skeleton.getJoint(BirdSkeleton.TAIL_BASE);
        BirdJoint tailFan   = skeleton.getJoint(BirdSkeleton.TAIL_FAN);
        BirdJoint lShin     = skeleton.getJoint(BirdSkeleton.L_SHIN);
        BirdJoint rShin     = skeleton.getJoint(BirdSkeleton.R_SHIN);
        BirdJoint lUpperWing = skeleton.getJoint(BirdSkeleton.L_UPPER_WING);
        BirdJoint rUpperWing = skeleton.getJoint(BirdSkeleton.R_UPPER_WING);

        if (chest     != null) chest.velX     += 2.0f;
        if (hip       != null) hip.velX       += 1.5f;
        if (tailBase  != null) tailBase.velX  -= 3.0f;
        if (tailFan   != null) tailFan.velX   -= 2.0f;
        if (lShin     != null) lShin.velX     += 2.5f;
        if (rShin     != null) rShin.velX     += 2.5f;
        if (lUpperWing != null) lUpperWing.velZ -= 4.0f;
        if (rUpperWing != null) rUpperWing.velZ += 4.0f;
    }
}
