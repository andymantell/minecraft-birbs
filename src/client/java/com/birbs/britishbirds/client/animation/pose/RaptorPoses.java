package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

/**
 * Raptor-specific poses and cyclic animations.
 * Covers soaring, stooping (high-speed dive), and slow deep wingbeats
 * characteristic of birds of prey.
 *
 * Key conventions (lateral wing geometry):
 * - Positive chest xRot = forward pitch
 * - zRot controls wing spread and flap
 * - yRot controls wing fold (Z-fold for perched)
 * - Negative tail_base xRot counteracts body pitch
 * - Extreme negative leg tuck values for flight
 */
public final class RaptorPoses {

    private RaptorPoses() {}

    // =========================================================================
    // Static Poses
    // =========================================================================

    /**
     * Soaring: wings fully spread and flat, body level, slight upward dihedral.
     * Tail fanned as a rudder. Legs tucked.
     */
    public static final PoseData SOAR = PoseData.builder("soar")
            .joint(BirdSkeleton.CHEST,       1.1f, 0f, 0f)   // positive forward pitch
            .joint(BirdSkeleton.TORSO,       0.15f, 0f, 0f)
            .joint(BirdSkeleton.HIP,         0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,  0.05f, 0f, -0.05f)
            .joint(BirdSkeleton.NECK_MID,    0.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.0f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.5f, 0f, 0f)   // head up to scan
            // Wings spread wide via zRot
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.4f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.05f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.03f)
            // Tail counteracts body pitch
            .joint(BirdSkeleton.TAIL_BASE,  -0.7f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,   -0.15f, 0f, 0f)
            // Legs tucked
            .joint(BirdSkeleton.L_THIGH,    -1.5f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,     -2.5f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,    2.0f, 0f, 0f)
            .joint(BirdSkeleton.L_FOOT,     -0.8f, 0f, 0f)
            .mirror()
            .build();

    /**
     * Stoop: wings locked tight against body, body angled steeply downward.
     * The peregrine's signature dive — teardrop shape at 200+ mph.
     * Very high spring stiffness prevents wing flutter during the dive.
     */
    public static final PoseData STOOP = PoseData.builder("stoop")
            // Body pitched steeply down (positive = nose down)
            .joint(BirdSkeleton.CHEST,       1.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.3f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.15f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.4f, 0f, 0f)
            // Wings locked tight via yRot Z-fold + tight zRot
            .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.1f)
            .joint(BirdSkeleton.L_FOREARM,    0f,  2.5f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, -2.3f, 0f)
            // Tail folded tight
            .joint(BirdSkeleton.TAIL_BASE,  -0.1f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,    0.1f, 0f, 0f)
            // Legs tucked tight
            .joint(BirdSkeleton.L_THIGH,    -1.5f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,     -2.5f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,    2.0f, 0f, 0f)
            .joint(BirdSkeleton.L_FOOT,     -0.8f, 0f, 0f)
            .mirror()
            // Very stiff springs on wing bones — no flutter during stoop
            .spring(BirdSkeleton.L_UPPER_WING, 200f, 25f)
            .spring(BirdSkeleton.L_FOREARM,    200f, 25f)
            .spring(BirdSkeleton.L_HAND,       200f, 25f)
            .spring(BirdSkeleton.L_SCAPULARS,  200f, 25f)
            .spring(BirdSkeleton.L_SECONDARIES,200f, 25f)
            .spring(BirdSkeleton.L_PRIMARIES,  200f, 25f)
            .mirror()
            .build();

    /**
     * Bolt-upright perch: characteristic raptor stance when perched on a ledge or branch.
     * Body more vertical than passerines, head high, wings folded tight via yRot Z-fold.
     */
    public static final PoseData RAPTOR_PERCH = PoseData.builder("raptor_perch")
            .joint(BirdSkeleton.CHEST,       0.35f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.15f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.1f,  0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.1f,  0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.15f, 0f, 0f)
            // Wings folded tight via yRot Z-fold
            .joint(BirdSkeleton.L_UPPER_WING, 0f, -1.5f, 0.3f)
            .joint(BirdSkeleton.L_FOREARM,    0f,  2.2f, 0f)
            .joint(BirdSkeleton.L_HAND,       0f, -1.8f, 0f)
            .joint(BirdSkeleton.TAIL_BASE,   0.25f, 0f, 0f)
            .joint(BirdSkeleton.L_THIGH,     0.15f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.4f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.5f, 0f, 0f)
            .mirror()
            .build();

    /**
     * Hover: body pitched forward, wings beating rapidly, legs dangling.
     * Used by barn owl during quartering flight over fields.
     * Slower, softer springs on trailing feathers for loose trailing effect.
     */
    public static final PoseData HOVER = PoseData.builder("hover")
            .joint(BirdSkeleton.CHEST,       0.6f, 0f, 0f)   // positive forward pitch
            .joint(BirdSkeleton.TORSO,       0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER,  0.05f, 0f, -0.05f)
            .joint(BirdSkeleton.NECK_MID,    0.0f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER,  0.0f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.3f,  0f, 0f)  // head tilted to scan ground
            // Wings spread wide via zRot for hovering
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.5f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.05f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.03f)
            // Tail counteracts body pitch
            .joint(BirdSkeleton.TAIL_BASE,  -0.5f, 0f, 0f)
            .joint(BirdSkeleton.TAIL_FAN,   -0.15f, 0f, 0f)
            // Legs dangling down for strike readiness
            .joint(BirdSkeleton.L_THIGH,     0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,      0.1f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,   -0.1f, 0f, 0f)
            .mirror()
            // Loose trailing feathers during hover
            .spring(BirdSkeleton.L_SCAPULARS,   10f, 1.5f)
            .spring(BirdSkeleton.L_SECONDARIES, 10f, 1.5f)
            .spring(BirdSkeleton.L_PRIMARIES,    8f, 1.0f)
            .mirror()
            .build();

    /**
     * Strike: legs extended forward for prey grab, wings swept back,
     * body pitching down. Used at the moment of prey contact.
     */
    public static final PoseData STRIKE = PoseData.builder("strike")
            .joint(BirdSkeleton.CHEST,       0.4f, 0f, 0f)
            .joint(BirdSkeleton.NECK_LOWER, -0.1f, 0f, 0f)
            .joint(BirdSkeleton.NECK_MID,   -0.05f, 0f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, -0.05f, 0f, 0f)
            .joint(BirdSkeleton.HEAD,       -0.2f, 0f, 0f)  // looking down at prey
            // Wings swept back via zRot to brake
            .joint(BirdSkeleton.L_UPPER_WING, 0f, 0f, -0.6f)
            .joint(BirdSkeleton.L_FOREARM,    0f, 0f, -0.1f)
            .joint(BirdSkeleton.L_HAND,       0f, 0f, -0.05f)
            .joint(BirdSkeleton.TAIL_BASE,  -0.6f, 0f, 0f)   // tail as brake
            // Legs extended forward — talons out
            .joint(BirdSkeleton.L_THIGH,    -0.6f, 0f, 0f)
            .joint(BirdSkeleton.L_SHIN,     -0.3f, 0f, 0f)
            .joint(BirdSkeleton.L_TARSUS,    0.2f, 0f, 0f)
            .joint(BirdSkeleton.L_FOOT,     -0.4f, 0f, 0f)   // talons spread
            .mirror()
            .build();

    // =========================================================================
    // Partial Overlays
    // =========================================================================

    /** Alert head scan — sharp raptor head turn to one side. */
    public static final PoseData HEAD_SCAN = PoseData.builder("head_scan")
            .joint(BirdSkeleton.HEAD, 0f, 0.4f, 0f)
            .build();

    /** Owl head turn left — wider rotation than standard raptor (owls rotate ~270 degrees). */
    public static final PoseData OWL_HEAD_LEFT = PoseData.builder("owl_head_left")
            .joint(BirdSkeleton.HEAD, 0f, -0.5f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, 0f, -0.15f, 0f)
            .build();

    /** Owl head turn right. */
    public static final PoseData OWL_HEAD_RIGHT = PoseData.builder("owl_head_right")
            .joint(BirdSkeleton.HEAD, 0f, 0.5f, 0f)
            .joint(BirdSkeleton.NECK_UPPER, 0f, 0.15f, 0f)
            .build();

    // =========================================================================
    // Cyclic Animations
    // =========================================================================

    /**
     * Raptor wingbeat: slow, deep, powerful strokes.
     * Uses zRot (lateral wing geometry) — wings extend in +X from shoulder,
     * so zRot rotates them up and down correctly.
     */
    public static final CyclicAnimation RAPTOR_WINGBEAT = new CyclicAnimation(
            "raptor_wingbeat",
            PoseData.builder("raptor_wings_up")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, -0.5f)  // zRot up
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, -0.2f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, -0.15f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, -0.12f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, -0.1f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, -0.08f)
                    .mirror()
                    .build(),
            PoseData.builder("raptor_wings_down")
                    .joint(BirdSkeleton.L_UPPER_WING,  0f, 0f, 0.5f)   // zRot down
                    .joint(BirdSkeleton.L_FOREARM,     0f, 0f, 0.15f)
                    .joint(BirdSkeleton.L_HAND,        0f, 0f, 0.1f)
                    .joint(BirdSkeleton.L_SCAPULARS,   0f, 0f, 0.1f)
                    .joint(BirdSkeleton.L_SECONDARIES, 0f, 0f, 0.08f)
                    .joint(BirdSkeleton.L_PRIMARIES,   0f, 0f, 0.06f)
                    .mirror()
                    .build()
    );
}
