package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Applies inertial drag to tail and wing feathers based on the
 * bird's turning rate, vertical velocity and forward speed.
 */
public class MovementDrag implements ProceduralBehaviour {

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        BirdJoint tailBase     = skeleton.getJoint(BirdSkeleton.TAIL_BASE);
        BirdJoint tailFan      = skeleton.getJoint(BirdSkeleton.TAIL_FAN);
        BirdJoint lPrimaries   = skeleton.getJoint(BirdSkeleton.L_PRIMARIES);
        BirdJoint rPrimaries   = skeleton.getJoint(BirdSkeleton.R_PRIMARIES);
        BirdJoint lSecondaries = skeleton.getJoint(BirdSkeleton.L_SECONDARIES);
        BirdJoint rSecondaries = skeleton.getJoint(BirdSkeleton.R_SECONDARIES);

        // Yaw drag on tail
        if (tailBase != null) {
            tailBase.targetY -= state.yawDelta * 0.4f;
            tailBase.targetX -= state.verticalVelocity * 0.8f;
        }
        if (tailFan != null) {
            tailFan.targetY -= state.yawDelta * 0.6f;
        }

        // Speed drag on wing feathers
        if (lPrimaries   != null) lPrimaries.targetZ   -= state.speed * 0.3f;
        if (rPrimaries   != null) rPrimaries.targetZ   += state.speed * 0.3f;
        if (lSecondaries != null) lSecondaries.targetZ -= state.speed * 0.2f;
        if (rSecondaries != null) rSecondaries.targetZ += state.speed * 0.2f;
    }
}
