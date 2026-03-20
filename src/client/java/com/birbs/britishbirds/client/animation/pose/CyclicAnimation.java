package com.birbs.britishbirds.client.animation.pose;

import org.joml.Vector3f;

public final class CyclicAnimation {

    private final String name;
    private final PoseData offsetA;
    private final PoseData offsetB;

    public CyclicAnimation(String name, PoseData offsetA, PoseData offsetB) {
        this.name = name;
        this.offsetA = offsetA;
        this.offsetB = offsetB;
    }

    public String getName() {
        return name;
    }

    public PoseData getOffsetA() {
        return offsetA;
    }

    public PoseData getOffsetB() {
        return offsetB;
    }

    /**
     * Returns the linearly interpolated offset for the given joint at the given phase (0.0–1.0).
     * If the joint is present in only one sub-pose, the missing side is treated as zero.
     * Returns null if the joint is absent from both sub-poses.
     */
    public Vector3f getBlendedOffset(String jointName, float phase) {
        Vector3f a = offsetA.getAngle(jointName);
        Vector3f b = offsetB.getAngle(jointName);

        if (a == null && b == null) {
            return null;
        }

        Vector3f va = (a != null) ? a : new Vector3f(0f, 0f, 0f);
        Vector3f vb = (b != null) ? b : new Vector3f(0f, 0f, 0f);

        float clampedPhase = Math.clamp(phase, 0.0f, 1.0f);
        return new Vector3f(
                va.x + (vb.x - va.x) * clampedPhase,
                va.y + (vb.y - va.y) * clampedPhase,
                va.z + (vb.z - va.z) * clampedPhase
        );
    }
}
