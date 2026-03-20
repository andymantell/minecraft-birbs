package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Blue tit rapid head-cocking side to side. Quick alternating head tilts
 * characteristic of curious, active small birds examining their surroundings.
 */
public class HeadCock implements ProceduralBehaviour {

    private final float interval;
    private final float tiltAngle;

    /**
     * @param interval  cycle length in ticks (default 40)
     * @param tiltAngle maximum tilt angle in radians (default 0.25f)
     */
    public HeadCock(float interval, float tiltAngle) {
        this.interval = interval;
        this.tiltAngle = tiltAngle;
    }

    public HeadCock() {
        this(40f, 0.25f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (state.isFlying) return;

        int cycle = (int) (state.ageInTicks) % (int) interval;
        BirdJoint head = skeleton.getJoint(BirdSkeleton.HEAD);
        if (head == null) return;

        if (cycle < interval / 4) {
            head.targetZ += tiltAngle;           // tilt right
        } else if (cycle < interval / 2) {
            head.targetZ -= tiltAngle;           // tilt left
        }
        // else: head stays straight (targetZ unchanged)
    }
}
