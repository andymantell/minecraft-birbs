package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Shared base poses and cyclic animations used by all bird species.
 * All angle values are in radians. These are approximate starting points
 * and will be tuned in-game later.
 */
public final class BaseBirdPoses {

    private BaseBirdPoses() {}

    // =========================================================================
    // Static Poses
    // =========================================================================

    /** Resting on a branch — relaxed S-curve neck, wings folded tight, legs gripping. */
    public static final PoseData PERCHED = PoseData.builder("perched")
            .joint(BirdSkeleton.CHEST,       0.1f,  0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.1f,  0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.1f,  0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.05f, 0f, 0f)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.2f, 0f, 0f)
            .joint(BirdSkeleton.L_THIGH,     0.1f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.4f, 0f, 0f)
            .mirror()
            .build();

    /** Head up, body tense — scanning for threats. */
    public static final PoseData ALERT = PoseData.builder("alert")
            .joint(BirdSkeleton.CHEST,       0.0f,  0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.2f,  0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.15f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.1f,  0f, 0f)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.1f, 0f, 0f)
            .mirror()
            .build();

    /** Level sustained flight — body nearly horizontal, wings spread, neck compensating up. */
    public static final PoseData FLYING_CRUISE = PoseData.builder("flying_cruise")
            .joint(BirdSkeleton.CHEST,      -1.2f, 0f, 0f)   // strong forward pitch
            .joint(BirdSkeleton.TORSO,      -0.2f, 0f, 0f)   // torso follows
            .joint(BirdSkeleton.HIP,        -0.15f, 0f, 0f)  // hip aligns with body
            .joint(BirdSkeleton.NECK_LOWER,  0.4f, 0f, 0f)   // neck compensates up
            .joint(BirdSkeleton.NECK_MID,    0.3f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.2f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,        0.25f, 0f, 0f)   // head looks forward
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.3f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.2f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.1f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.3f, 0f, 0f)   // tail extends behind
            .mirror()
            .build();

    /** Steep launch — body pitched steeply, wings spread wide, powerful upward thrust. */
    public static final PoseData FLYING_TAKEOFF = PoseData.builder("flying_takeoff")
            .joint(BirdSkeleton.CHEST,      -1.4f, 0f, 0f)   // steeper than cruise
            .joint(BirdSkeleton.TORSO,      -0.3f, 0f, 0f)
            .joint(BirdSkeleton.HIP,        -0.2f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,  0.5f, 0f, 0f)   // neck cranes up
            .joint(BirdSkeleton.NECK_MID,    0.35f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.25f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,        0.25f, 0f, 0f)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.5f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.2f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.1f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.4f, 0f, 0f)   // tail fans for lift
            .mirror()
            .build();

    /** Flare posture for landing — body pitched back, wings wide, legs forward, tail as air brake. */
    public static final PoseData FLYING_LAND = PoseData.builder("flying_land")
            .joint(BirdSkeleton.CHEST,       0.3f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.1f, 0f, 0f)
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.8f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.3f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.15f)
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
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f,  0.05f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -2.0f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f,  1.8f)
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

    /** Legs tucked for flight — thighs, shins, tarsi, and feet pulled up. */
    public static final PoseData LEGS_TUCKED = PoseData.builder("legs_tucked")
            .joint(BirdSkeleton.L_THIGH,   0.5f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,    0.8f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS, -0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_FOOT,    0.2f, 0f, 0f)
            .mirror()
            .build();

    // =========================================================================
    // Cyclic Animations (offset poses — values are offsets from zero)
    // =========================================================================

    /** Wingbeat cycle — wings up (A) to wings down (B). */
    public static final CyclicAnimation WINGBEAT = new CyclicAnimation(
            "wingbeat",
            PoseData.builder("wings_up")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, -0.8f)
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, -0.3f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, -0.2f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, -0.2f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, -0.15f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, -0.1f)
                    .mirror()
                    .build(),
            PoseData.builder("wings_down")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, 0.6f)
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, 0.2f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, 0.15f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, 0.15f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, 0.1f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, 0.08f)
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
