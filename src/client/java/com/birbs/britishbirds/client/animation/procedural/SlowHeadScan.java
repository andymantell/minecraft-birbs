package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Owl slow deliberate head rotation. Smoothly scans the head from side to side,
 * characteristic of owls scanning for prey or threats in low light.
 */
public class SlowHeadScan implements ProceduralBehaviour {

    private final float scanRange;
    private final float speed;

    /**
     * @param scanRange maximum rotation in radians (default 0.6f)
     * @param speed     phase multiplier (default 0.03f)
     */
    public SlowHeadScan(float scanRange, float speed) {
        this.scanRange = scanRange;
        this.speed = speed;
    }

    public SlowHeadScan() {
        this(0.6f, 0.03f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (state.isFlying) return;

        float scan = (float) Math.sin(state.ageInTicks / 20.0f * speed * 2.0f * (float) Math.PI) * scanRange;

        BirdJoint head = skeleton.getJoint(BirdSkeleton.HEAD);
        BirdJoint neckUpper = skeleton.getJoint(BirdSkeleton.NECK_UPPER);

        if (head != null) head.targetY += scan;
        if (neckUpper != null) neckUpper.targetY += scan * 0.3f;
    }
}
