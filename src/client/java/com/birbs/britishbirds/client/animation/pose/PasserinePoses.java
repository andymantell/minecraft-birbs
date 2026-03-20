package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Passerine-specific poses and cyclic animations.
 * Extends the base poses with behaviours characteristic of small perching birds:
 * hopping, ground foraging, pecking, and the diagnostic head-tilt.
 */
public final class PasserinePoses {

    private PasserinePoses() {}

    // =========================================================================
    // Cyclic Animations
    // =========================================================================

    /**
     * Hop cycle: crouch (A) to spring (B).
     * Legs bend and body drops in A, legs extend and body rises in B.
     */
    public static final CyclicAnimation HOP = new CyclicAnimation(
            "hop",
            PoseData.builder("hop_crouch")
                    .joint(BirdSkeleton.CHEST,    0.15f, 0f, 0f)
                    .joint(BirdSkeleton.L_THIGH,  0.4f,  0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,   0.6f,  0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS, -0.5f, 0f, 0f)
                    .mirror()
                    .build(),
            PoseData.builder("hop_spring")
                    .joint(BirdSkeleton.CHEST,    -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_THIGH,  -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,  0.1f, 0f, 0f)
                    .mirror()
                    .build()
    );

    // =========================================================================
    // Static Poses
    // =========================================================================

    /** Ground foraging: body tilted forward, head angled down. */
    public static final PoseData FORAGE = PoseData.builder("forage")
            .joint(BirdSkeleton.CHEST,       0.3f,  0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.1f,  0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.1f,  0f, 0f)
            .joint(BirdSkeleton.HEAD,        0.4f,  0f, 0f)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.15f, 0f, 0f)
            .joint(BirdSkeleton.L_THIGH,     0.2f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.4f, 0f, 0f)
            .mirror()
            .build();

    // =========================================================================
    // Cyclic Animations (continued)
    // =========================================================================

    /**
     * Peck cycle: head down + beak open (A) to head up + beak closed (B).
     * Affects head, neck_upper, and lower_beak.
     */
    public static final CyclicAnimation PECK = new CyclicAnimation(
            "peck",
            PoseData.builder("peck_down")
                    .joint(BirdSkeleton.NECK_UPPER, 0.3f,  0f, 0f)
                    .joint(BirdSkeleton.HEAD,       0.4f,  0f, 0f)
                    .joint(BirdSkeleton.LOWER_BEAK, 0.3f,  0f, 0f)
                    .build(),
            PoseData.builder("peck_up")
                    .joint(BirdSkeleton.NECK_UPPER, -0.15f, 0f, 0f)
                    .joint(BirdSkeleton.HEAD,       -0.2f,  0f, 0f)
                    .joint(BirdSkeleton.LOWER_BEAK,  0.0f,  0f, 0f)
                    .build()
    );

    // =========================================================================
    // Partial Overlays
    // =========================================================================

    /** Characteristic passerine head-tilt: side-cock to observe the ground. */
    public static final PoseData HEAD_TILT = PoseData.builder("head_tilt")
            .joint(BirdSkeleton.HEAD, 0f, 0.3f, 0.15f)
            .build();
}
