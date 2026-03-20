package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Injects one-shot velocity impulses when the bird is startled,
 * snapping the head toward the threat and jolting the body.
 */
public class StartleResponse implements ProceduralBehaviour {

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (!state.justStartled) return;

        BirdJoint chest      = skeleton.getJoint(BirdSkeleton.CHEST);
        BirdJoint hip        = skeleton.getJoint(BirdSkeleton.HIP);
        BirdJoint lUpperWing = skeleton.getJoint(BirdSkeleton.L_UPPER_WING);
        BirdJoint rUpperWing = skeleton.getJoint(BirdSkeleton.R_UPPER_WING);
        BirdJoint tailFan    = skeleton.getJoint(BirdSkeleton.TAIL_FAN);
        BirdJoint head       = skeleton.getJoint(BirdSkeleton.HEAD);

        if (chest      != null) chest.velX      += 3.0f;
        if (hip        != null) hip.velX        += 2.0f;
        if (lUpperWing != null) lUpperWing.velZ -= 5.0f;
        if (rUpperWing != null) rUpperWing.velZ += 5.0f;
        if (tailFan    != null) tailFan.velX    -= 3.0f;

        // Snap head toward threat
        if (head != null && state.lookTarget != null) {
            double dx = state.lookTarget.x - state.x;
            double dz = state.lookTarget.z - state.z;
            float yawToThreat = (float) Math.atan2(dx, dz);
            head.velY += yawToThreat * 8.0f;
        }
    }
}
