package com.birbs.britishbirds.client.animation;

public class BirdSkeletonState {
    public static final int JOINT_COUNT = 32;
    public static final int AXIS_COUNT = 3;
    public static final int ARRAY_SIZE = JOINT_COUNT * AXIS_COUNT;

    public final float[] angles = new float[ARRAY_SIZE];
    public final float[] velocities = new float[ARRAY_SIZE];
    public float prevAgeAndPartial = -1.0f; // -1 = uninitialised
    public long lastRenderedTick = 0;       // for stale state cleanup
}
