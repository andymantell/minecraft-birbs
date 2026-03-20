package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

/**
 * Robin tail-bobbing when perched. Adds a rhythmic up-down oscillation
 * to the tail_base and tail_fan xRot, giving the robin its characteristic
 * perched tail-flick.
 */
public class TailBob implements ProceduralBehaviour {

    private final float rate;
    private final float amplitude;

    /**
     * @param rate      phase multiplier (default 0.2f)
     * @param amplitude oscillation amplitude in radians (default 0.08f)
     */
    public TailBob(float rate, float amplitude) {
        this.rate = rate;
        this.amplitude = amplitude;
    }

    public TailBob() {
        this(0.2f, 0.08f);
    }

    @Override
    public void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime) {
        if (state.isFlying) return;

        float bob = (float) Math.sin(state.ageInTicks / 20.0f * rate * 2.0f * (float) Math.PI) * amplitude;

        BirdJoint tailBase = skeleton.getJoint(BirdSkeleton.TAIL_BASE);
        BirdJoint tailFan = skeleton.getJoint(BirdSkeleton.TAIL_FAN);

        if (tailBase != null) tailBase.targetX += bob;
        if (tailFan != null) tailFan.targetX += bob * 0.5f;
    }
}
