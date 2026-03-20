package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Waterfowl-specific poses and cyclic animations.
 * Covers swimming, dabbling, waddling, and paddling behaviours
 * characteristic of ducks, geese, and swans.
 */
public final class WaterfowlPoses {

    private WaterfowlPoses() {}

    // =========================================================================
    // Static Poses
    // =========================================================================

    /**
     * Swimming: body level and low in water, wings folded, legs hidden below waterline.
     * Relaxed, buoyant posture.
     */
    public static final PoseData SWIM = PoseData.builder("swim")
            .joint(BirdSkeleton.CHEST,       0.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.05f, 0f, 0f)
            // Wings folded
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.17f, 0f, 0f)
            // Legs extended slightly below for paddling
            .joint(BirdSkeleton.L_THIGH,     0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.4f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.2f, 0f, 0f)
            .mirror()
            .build();

    /**
     * Dabbling: body pitched steeply forward, tail up, head underwater.
     * The classic duck upending posture for feeding.
     */
    public static final PoseData DABBLE = PoseData.builder("dabble")
            // Body pitched steeply down — chest approaching 90 degrees
            .joint(BirdSkeleton.CHEST,       1.3f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,  0.2f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,    0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.1f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,        0.2f, 0f, 0f)
            // Wings folded
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
            // Tail up
            .joint(BirdSkeleton.TAIL_BASE,  -1.2f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,   -0.3f, 0f, 0f)
            // Legs paddle gently
            .joint(BirdSkeleton.L_THIGH,     0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.1f, 0f, 0f)
            .mirror()
            .build();

    // =========================================================================
    // Cyclic Animations
    // =========================================================================

    /**
     * Waddle cycle: body rolls side-to-side with alternating leg stride.
     * The characteristic duck walk — legs set far back, causing the body to sway.
     */
    public static final CyclicAnimation WADDLE = new CyclicAnimation(
            "waddle",
            PoseData.builder("waddle_left")
                    // Body rolls right (left leg forward, right leg back)
                    .joint(BirdSkeleton.CHEST, 0f, 0f, -0.12f)
                    .joint(BirdSkeleton.L_THIGH,  -0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,    0.4f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS, -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,   0.3f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS,  0.1f, 0f, 0f)
                    .build(),
            PoseData.builder("waddle_right")
                    // Body rolls left (right leg forward, left leg back)
                    .joint(BirdSkeleton.CHEST, 0f, 0f, 0.12f)
                    .joint(BirdSkeleton.L_THIGH,   0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,  0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,  -0.3f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,    0.4f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS, -0.2f, 0f, 0f)
                    .build()
    );

    /**
     * Paddle cycle: legs alternate push/recovery below waterline.
     * L_thigh oscillates opposite to R_thigh.
     */
    public static final CyclicAnimation PADDLE = new CyclicAnimation(
            "paddle",
            PoseData.builder("paddle_left_push")
                    .joint(BirdSkeleton.L_THIGH,   0.4f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,    0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS, -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_FOOT,    0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,  -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS,  0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_FOOT,   -0.05f, 0f, 0f)
                    .build(),
            PoseData.builder("paddle_right_push")
                    .joint(BirdSkeleton.L_THIGH,  -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,  0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_FOOT,   -0.05f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,   0.4f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,    0.3f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS, -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.R_FOOT,    0.1f, 0f, 0f)
                    .build()
    );

    /**
     * Waterfowl wingbeat: fast, stiff strokes — rapid wing cycling typical
     * of heavy-bodied ducks in direct flight.
     */
    /**
     * Waterfowl wingbeat: fast, stiff strokes.
     * Uses xRot — wings are children of chest, so when body pitches
     * forward for flight, xRot correctly flaps up/down.
     */
    public static final CyclicAnimation WATERFOWL_WINGBEAT = new CyclicAnimation(
            "waterfowl_wingbeat",
            PoseData.builder("waterfowl_wings_up")
                    .joint(BirdSkeleton.L_UPPER_WING,  -1.0f, 0f, 0f)
                    .joint(BirdSkeleton.L_FOREARM,     -0.35f, 0f, 0f)
                    .joint(BirdSkeleton.L_HAND,        -0.25f, 0f, 0f)
                    .joint(BirdSkeleton.L_SCAPULARS,   -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_SECONDARIES, -0.15f, 0f, 0f)
                    .joint(BirdSkeleton.L_PRIMARIES,   -0.1f, 0f, 0f)
                    .mirror()
                    .build(),
            PoseData.builder("waterfowl_wings_down")
                    .joint(BirdSkeleton.L_UPPER_WING,   0.8f, 0f, 0f)
                    .joint(BirdSkeleton.L_FOREARM,      0.25f, 0f, 0f)
                    .joint(BirdSkeleton.L_HAND,         0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_SCAPULARS,    0.15f, 0f, 0f)
                    .joint(BirdSkeleton.L_SECONDARIES,  0.12f, 0f, 0f)
                    .joint(BirdSkeleton.L_PRIMARIES,    0.1f, 0f, 0f)
                    .mirror()
                    .build()
    );
}
