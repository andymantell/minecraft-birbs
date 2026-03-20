package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Waterfowl-specific poses and cyclic animations.
 * Covers swimming, dabbling, waddling, and paddling behaviours
 * characteristic of ducks, geese, and swans.
 *
 * <p>Values are loaded from JSON ({@code assets/britishbirds/poses/waterfowl_poses.json})
 * at startup via {@link PoseLoader}. Hardcoded constants serve as fallbacks.
 *
 * Key conventions (lateral wing geometry):
 * - Positive chest xRot = forward pitch
 * - zRot controls wing spread and flap
 * - yRot controls wing fold (Z-fold for swimming/resting)
 * - Negative tail_base xRot counteracts body pitch
 * - Extreme negative leg tuck values for flight
 */
public final class WaterfowlPoses {

    private WaterfowlPoses() {}

    private static final String ARCH = "waterfowl";

    // =========================================================================
    // Static Poses
    // =========================================================================

    /**
     * Swimming: body level and low in water, wings folded via yRot Z-fold, legs hidden below waterline.
     * Relaxed, buoyant posture.
     */
    public static final PoseData SWIM = PoseLoader.getOrDefault(ARCH, "swim",
            PoseData.builder("swim")
                    .joint(BirdSkeleton.CHEST,       0.0f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_LOWER, -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
                    .joint(BirdSkeleton.HEAD,       -0.05f, 0f, 0f)
                    .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.3f)
                    .joint(BirdSkeleton.L_FOREARM,    0f,  2.2f, 0f)
                    .joint(BirdSkeleton.L_HAND,       0f, -1.8f, 0f)
                    .joint(BirdSkeleton.TAIL_BASE,  -0.17f, 0f, 0f)
                    .joint(BirdSkeleton.L_THIGH,     0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,      0.4f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,   -0.2f, 0f, 0f)
                    .mirror()
                    .build());

    /**
     * Dabbling: body pitched steeply forward, tail up, head underwater.
     * The classic duck upending posture for feeding.
     */
    public static final PoseData DABBLE = PoseLoader.getOrDefault(ARCH, "dabble",
            PoseData.builder("dabble")
                    .joint(BirdSkeleton.CHEST,       1.3f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_LOWER,  0.2f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_MID,    0.1f, 0f, 0f)
                    .joint(BirdSkeleton.NECK_UPPER,  0.1f, 0f, 0f)
                    .joint(BirdSkeleton.HEAD,        0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.3f)
                    .joint(BirdSkeleton.L_FOREARM,    0f,  2.2f, 0f)
                    .joint(BirdSkeleton.L_HAND,       0f, -1.8f, 0f)
                    .joint(BirdSkeleton.TAIL_BASE,  -1.2f, 0f, 0f)
                    .joint(BirdSkeleton.TAIL_FAN,   -0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_THIGH,     0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,      0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,   -0.1f, 0f, 0f)
                    .mirror()
                    .build());

    // =========================================================================
    // Cyclic Animations
    // =========================================================================

    /**
     * Waddle cycle: body rolls side-to-side with alternating leg stride.
     * The characteristic duck walk — legs set far back, causing the body to sway.
     */
    public static final CyclicAnimation WADDLE = PoseLoader.getCyclicOrDefault(ARCH, "waddle",
            new CyclicAnimation(
                    "waddle",
                    PoseData.builder("waddle_left")
                            .joint(BirdSkeleton.CHEST, 0f, 0f, -0.12f)
                            .joint(BirdSkeleton.L_THIGH,  -0.3f, 0f, 0f)
                            .joint(BirdSkeleton.L_SHIN,    0.4f, 0f, 0f)
                            .joint(BirdSkeleton.L_TARSUS, -0.2f, 0f, 0f)
                            .joint(BirdSkeleton.R_THIGH,   0.3f, 0f, 0f)
                            .joint(BirdSkeleton.R_SHIN,   -0.1f, 0f, 0f)
                            .joint(BirdSkeleton.R_TARSUS,  0.1f, 0f, 0f)
                            .build(),
                    PoseData.builder("waddle_right")
                            .joint(BirdSkeleton.CHEST, 0f, 0f, 0.12f)
                            .joint(BirdSkeleton.L_THIGH,   0.3f, 0f, 0f)
                            .joint(BirdSkeleton.L_SHIN,   -0.1f, 0f, 0f)
                            .joint(BirdSkeleton.L_TARSUS,  0.1f, 0f, 0f)
                            .joint(BirdSkeleton.R_THIGH,  -0.3f, 0f, 0f)
                            .joint(BirdSkeleton.R_SHIN,    0.4f, 0f, 0f)
                            .joint(BirdSkeleton.R_TARSUS, -0.2f, 0f, 0f)
                            .build()
            ));

    /**
     * Paddle cycle: legs alternate push/recovery below waterline.
     * L_thigh oscillates opposite to R_thigh.
     */
    public static final CyclicAnimation PADDLE = PoseLoader.getCyclicOrDefault(ARCH, "paddle",
            new CyclicAnimation(
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
            ));

    /**
     * Waterfowl wingbeat: fast, stiff strokes.
     */
    public static final CyclicAnimation WATERFOWL_WINGBEAT = PoseLoader.getCyclicOrDefault(ARCH, "waterfowl_wingbeat",
            new CyclicAnimation(
                    "waterfowl_wingbeat",
                    PoseData.builder("waterfowl_wings_up")
                            .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, -0.5f)
                            .joint(BirdSkeleton.L_FOREARM,     0f, 0f, -0.18f)
                            .joint(BirdSkeleton.L_HAND,        0f, 0f, -0.12f)
                            .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, -0.1f)
                            .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, -0.08f)
                            .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, -0.06f)
                            .mirror()
                            .build(),
                    PoseData.builder("waterfowl_wings_down")
                            .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, 0.5f)
                            .joint(BirdSkeleton.L_FOREARM,     0f, 0f, 0.12f)
                            .joint(BirdSkeleton.L_HAND,        0f, 0f, 0.1f)
                            .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, 0.08f)
                            .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, 0.06f)
                            .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, 0.05f)
                            .mirror()
                            .build()
            ));
}
