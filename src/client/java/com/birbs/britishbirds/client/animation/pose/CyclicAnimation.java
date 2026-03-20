package com.birbs.britishbirds.client.animation.pose;

import org.joml.Vector3f;

public final class CyclicAnimation {

    /** Reusable scratch vector for getBlendedOffset — safe because render thread is single-threaded. */
    private static final Vector3f SCRATCH = new Vector3f();

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
     *
     * <p><b>Important:</b> The returned Vector3f is a shared scratch object. The caller must
     * read its values before the next call to this method (PoseResolver.resolve() does this —
     * it copies x/y/z into local floats immediately).
     */
    public Vector3f getBlendedOffset(String jointName, float phase) {
        Vector3f a = offsetA.getAngle(jointName);
        Vector3f b = offsetB.getAngle(jointName);

        if (a == null && b == null) {
            return null;
        }

        float ax = a != null ? a.x : 0, ay = a != null ? a.y : 0, az = a != null ? a.z : 0;
        float bx = b != null ? b.x : 0, by = b != null ? b.y : 0, bz = b != null ? b.z : 0;
        float t = Math.clamp(phase, 0.0f, 1.0f);
        return SCRATCH.set(
                ax + (bx - ax) * t,
                ay + (by - ay) * t,
                az + (bz - az) * t
        );
    }
}
