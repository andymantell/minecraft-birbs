package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Distributes head-tracking rotation across the neck chain so
 * the bird looks toward a world-space target naturally.
 */
public class HeadTracking implements ProceduralBehaviour {

    private final float maxYaw;
    private final float maxPitch;

    /**
     * @param maxYaw   maximum horizontal rotation in radians (default 1.5)
     * @param maxPitch maximum vertical rotation in radians (default 0.8)
     */
    public HeadTracking(float maxYaw, float maxPitch) {
        this.maxYaw = maxYaw;
        this.maxPitch = maxPitch;
    }

    public HeadTracking() {
        this(1.5f, 0.8f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (state.lookTarget == null) return;

        // Compute direction from bird to target in world space
        double dx = state.lookTarget.x - state.x;
        double dy = state.lookTarget.y - state.y;
        double dz = state.lookTarget.z - state.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yawToTarget = (float) Math.atan2(dx, dz);
        float pitchToTarget = (float) -Math.atan2(dy, horizontalDist);

        // Clamp
        yawToTarget = Math.clamp(yawToTarget, -maxYaw, maxYaw);
        pitchToTarget = Math.clamp(pitchToTarget, -maxPitch, maxPitch);

        // Distribute across neck chain
        BirdJoint neckLower = skeleton.getJoint(BirdSkeleton.NECK_LOWER);
        BirdJoint neckMid   = skeleton.getJoint(BirdSkeleton.NECK_MID);
        BirdJoint neckUpper = skeleton.getJoint(BirdSkeleton.NECK_UPPER);
        BirdJoint head      = skeleton.getJoint(BirdSkeleton.HEAD);

        if (neckLower != null) neckLower.targetY += yawToTarget * 0.2f;
        if (neckMid   != null) neckMid.targetY   += yawToTarget * 0.3f;
        if (neckUpper != null) {
            neckUpper.targetY += yawToTarget * 0.25f;
            neckUpper.targetX += pitchToTarget * 0.4f;
        }
        if (head != null) {
            head.targetY += yawToTarget * 0.25f;
            head.targetX += pitchToTarget * 0.6f;
        }
    }
}
