package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Shared base poses and cyclic animations used by all bird species.
 * All angle values are in radians. Tuned from preview tool validation.
 *
 * Key conventions after lateral wing geometry change:
 * - Wings extend in +X (left) / -X (right) from the shoulder
 * - zRot controls wing flap (up/down spread)
 * - yRot controls wing fold (Z-fold for perched)
 * - Positive chest xRot = forward pitch (body tilts nose-down)
 * - Negative tail_base xRot = tail lifts to counteract body pitch
 */
public final class BaseBirdPoses {

    private BaseBirdPoses() {}

    // =========================================================================
    // Static Poses
    // =========================================================================

    /** Resting on a branch — tuned in PoseEditor 2026-03-20. */
    public static final PoseData PERCHED = PoseData.builder("perched")
            .joint(BirdSkeleton.CHEST,        0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,   0.42f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,    -0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  -0.1f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,        -0.05f, 0f, 0f)
            // Wings folded — tuned via editor
            .joint(BirdSkeleton.L_UPPER_WING, 1.16f, -0.02f, 1.35f)
            .joint(BirdSkeleton.L_SCAPULARS,  0f, -0.2f, 0f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0.08f, 0.2f)
            .joint(BirdSkeleton.L_SECONDARIES,0f, -0.15f, 0.08f)
            .joint(BirdSkeleton.L_HAND,       0f, 0.05f, 0.36f)
            .joint(BirdSkeleton.L_PRIMARIES,  0.05f, -0.1f, -0.17f)
            .joint(BirdSkeleton.TAIL_BASE,   -0.2f, 0f, 0f)
            .joint(BirdSkeleton.L_THIGH,      0.1f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,       0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,    -0.4f, 0f, 0f)
            .mirror()
            .build();

    /** Head up, body tense — scanning for threats. */
    public static final PoseData ALERT = PoseData.builder("alert")
            .joint(BirdSkeleton.CHEST,       0.0f,  0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.2f,  0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.15f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.1f,  0f, 0f)
            // Wings folded via yRot Z-fold
            .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.3f)
            .joint(BirdSkeleton.L_FOREARM,    0f,  2.2f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, -1.8f, 0f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.1f, 0f, 0f)
            .mirror()
            .build();

    /**
     * Level sustained flight — body pitched forward (positive xRot), wings spread via zRot,
     * neck extends forward, head slightly up to compensate. Tail counteracts body pitch.
     */
    public static final PoseData FLYING_CRUISE = PoseData.builder("flying_cruise")
            .joint(BirdSkeleton.CHEST,       1.0f, 0f, 0f)   // positive = forward pitch
            .joint(BirdSkeleton.TORSO,       0.15f, 0f, 0f)   // torso follows
            .joint(BirdSkeleton.HIP,         0.1f, 0f, 0f)    // hip aligns with body
            .joint(BirdSkeleton.NECK_LOWER,  0.05f, 0f, -0.05f)
            .joint(BirdSkeleton.NECK_MID,    0.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.0f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.5f, 0f, 0f)    // head up to look ahead
            // Wings spread via zRot — slight dihedral (nearly flat, slight downward angle)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.1f)  // zRot: gentle spread
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, 0f)
            // Tail counteracts body pitch
            .joint(BirdSkeleton.TAIL_BASE,  -0.65f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,   -0.15f, 0f, 0f)
            .mirror()
            .build();

    /** Steep launch — body pitched steeply forward, wings spread wide. */
    public static final PoseData FLYING_TAKEOFF = PoseData.builder("flying_takeoff")
            .joint(BirdSkeleton.CHEST,       1.2f, 0f, 0f)   // steeper than cruise
            .joint(BirdSkeleton.TORSO,       0.2f, 0f, 0f)
            .joint(BirdSkeleton.HIP,         0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,  0.05f, 0f, -0.05f)
            .joint(BirdSkeleton.NECK_MID,    0.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.0f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.6f, 0f, 0f)
            // Wings wider spread
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.5f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.8f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,   -0.2f, 0f, 0f)
            .mirror()
            .build();

    /** Flare posture for landing — body pitched back, wings wide via zRot, legs forward, tail as air brake. */
    public static final PoseData FLYING_LAND = PoseData.builder("flying_land")
            .joint(BirdSkeleton.CHEST,      -0.3f, 0f, 0f)    // pitched back for flare
            .joint(BirdSkeleton.NECK_LOWER, -0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.1f, 0f, 0f)
            // Wings wide via zRot
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.8f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.1f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.05f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.8f, 0f, 0f)
            .joint(BirdSkeleton.L_THIGH,    -0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,     -0.5f, 0f, 0f)
            .mirror()
            .build();

    /** Sleeping/resting — settled low, head tucked into shoulder. */
    public static final PoseData TUCKED = PoseData.builder("tucked")
            .joint(BirdSkeleton.CHEST,       0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.1f,  0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,        0.3f,  0f, 0.4f)
            // Wings folded via yRot Z-fold
            .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.3f)
            .joint(BirdSkeleton.L_FOREARM,    0f,  2.2f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, -1.8f, 0f)
            .joint(BirdSkeleton.TAIL_BASE,   0.1f, 0f, 0f)
            .mirror()
            .build();

    // =========================================================================
    // Partial Overlays
    // =========================================================================

    /** Beak open overlay — only affects the lower beak joint. */
    public static final PoseData BEAK_OPEN = PoseData.builder("beak_open")
            .joint(BirdSkeleton.LOWER_BEAK, 0.4f, 0f, 0f)
            .build();

    /** Legs tucked for flight — extreme tuck values for in-flight leg retraction. */
    public static final PoseData LEGS_TUCKED = PoseData.builder("legs_tucked")
            .joint(BirdSkeleton.L_THIGH,  -1.5f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,   -2.5f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,  2.0f, 0f, 0f)
            .joint(BirdSkeleton.L_FOOT,   -0.8f, 0f, 0f)
            .mirror()
            .build();

    // =========================================================================
    // Cyclic Animations (offset poses — values are offsets from zero)
    // =========================================================================

    /**
     * Wingbeat cycle — wings up (A) to wings down (B).
     * Uses zRot (lateral wing geometry) — wings extend in +X from shoulder,
     * so zRot rotates them up and down in the body's frame of reference.
     */
    public static final CyclicAnimation WINGBEAT = new CyclicAnimation(
            "wingbeat",
            PoseData.builder("wings_up")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, -0.4f)  // zRot up
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, -0.15f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, -0.1f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, -0.1f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, -0.08f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, -0.06f)
                    .mirror()
                    .build(),
            PoseData.builder("wings_down")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, 0.4f)   // zRot down
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, 0.1f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, 0.08f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, 0.08f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, 0.06f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, 0.05f)
                    .mirror()
                    .build()
    );

    /** Walk cycle — legs in opposite phase (no mirror, left and right defined explicitly). */
    public static final CyclicAnimation WALK_CYCLE = new CyclicAnimation(
            "walk_cycle",
            PoseData.builder("legs_forward")
                    .joint(BirdSkeleton.L_THIGH,  -0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,    0.4f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS, -0.2f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,   0.3f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS,  0.1f, 0f, 0f)
                    .build(),
            PoseData.builder("legs_back")
                    .joint(BirdSkeleton.L_THIGH,   0.3f, 0f, 0f)
                    .joint(BirdSkeleton.L_SHIN,   -0.1f, 0f, 0f)
                    .joint(BirdSkeleton.L_TARSUS,  0.1f, 0f, 0f)
                    .joint(BirdSkeleton.R_THIGH,  -0.3f, 0f, 0f)
                    .joint(BirdSkeleton.R_SHIN,    0.4f, 0f, 0f)
                    .joint(BirdSkeleton.R_TARSUS, -0.2f, 0f, 0f)
                    .build()
    );
}
