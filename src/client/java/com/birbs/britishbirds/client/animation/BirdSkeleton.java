package com.birbs.britishbirds.client.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BirdSkeleton {

    // Joint name constants
    public static final String CHEST          = "chest";
    public static final String NECK_LOWER     = "neck_lower";
    public static final String NECK_MID       = "neck_mid";
    public static final String NECK_UPPER     = "neck_upper";
    public static final String HEAD           = "head";
    public static final String UPPER_BEAK     = "upper_beak";
    public static final String LOWER_BEAK     = "lower_beak";
    public static final String SHOULDER_MOUNT = "shoulder_mount";
    public static final String L_UPPER_WING   = "L_upper_wing";
    public static final String L_SCAPULARS    = "L_scapulars";
    public static final String L_FOREARM      = "L_forearm";
    public static final String L_SECONDARIES  = "L_secondaries";
    public static final String L_HAND         = "L_hand";
    public static final String L_PRIMARIES    = "L_primaries";
    public static final String R_UPPER_WING   = "R_upper_wing";
    public static final String R_SCAPULARS    = "R_scapulars";
    public static final String R_FOREARM      = "R_forearm";
    public static final String R_SECONDARIES  = "R_secondaries";
    public static final String R_HAND         = "R_hand";
    public static final String R_PRIMARIES    = "R_primaries";
    public static final String TORSO          = "torso";
    public static final String HIP            = "hip";
    public static final String L_THIGH        = "L_thigh";
    public static final String L_SHIN         = "L_shin";
    public static final String L_TARSUS       = "L_tarsus";
    public static final String L_FOOT         = "L_foot";
    public static final String R_THIGH        = "R_thigh";
    public static final String R_SHIN         = "R_shin";
    public static final String R_TARSUS       = "R_tarsus";
    public static final String R_FOOT         = "R_foot";
    public static final String TAIL_BASE      = "tail_base";
    public static final String TAIL_FAN       = "tail_fan";

    private final Map<String, BirdJoint> joints;
    private final List<BirdJoint> jointList;
    private final Map<String, Integer> jointIndex;

    private BirdSkeleton(Map<String, BirdJoint> joints, List<BirdJoint> jointList, Map<String, Integer> jointIndex) {
        this.joints = joints;
        this.jointList = jointList;
        this.jointIndex = jointIndex;
    }

    /**
     * Creates the universal 32-joint bird skeleton with default spring tuning.
     */
    public static BirdSkeleton createUniversal() {
        // Use LinkedHashMap to preserve insertion order for stable indexing
        Map<String, BirdJoint> joints = new LinkedHashMap<>();
        List<BirdJoint> jointList = new ArrayList<>();
        Map<String, Integer> jointIndex = new LinkedHashMap<>();

        // Helper to add joints in order
        // chest is the root (no parent)
        BirdJoint chest = add(joints, jointList, jointIndex, CHEST, null);

        // Neck chain off chest
        BirdJoint neckLower = add(joints, jointList, jointIndex, NECK_LOWER, chest);
        BirdJoint neckMid   = add(joints, jointList, jointIndex, NECK_MID,   neckLower);
        BirdJoint neckUpper = add(joints, jointList, jointIndex, NECK_UPPER, neckMid);
        BirdJoint head      = add(joints, jointList, jointIndex, HEAD,       neckUpper);
        add(joints, jointList, jointIndex, UPPER_BEAK, head);
        add(joints, jointList, jointIndex, LOWER_BEAK, head);

        // Shoulder mount off chest
        BirdJoint shoulderMount = add(joints, jointList, jointIndex, SHOULDER_MOUNT, chest);

        // Left wing chain
        BirdJoint lUpperWing = add(joints, jointList, jointIndex, L_UPPER_WING, shoulderMount);
        add(joints, jointList, jointIndex, L_SCAPULARS,   lUpperWing);
        BirdJoint lForearm   = add(joints, jointList, jointIndex, L_FOREARM,    lUpperWing);
        add(joints, jointList, jointIndex, L_SECONDARIES, lForearm);
        BirdJoint lHand      = add(joints, jointList, jointIndex, L_HAND,       lForearm);
        add(joints, jointList, jointIndex, L_PRIMARIES,   lHand);

        // Right wing chain
        BirdJoint rUpperWing = add(joints, jointList, jointIndex, R_UPPER_WING, shoulderMount);
        add(joints, jointList, jointIndex, R_SCAPULARS,   rUpperWing);
        BirdJoint rForearm   = add(joints, jointList, jointIndex, R_FOREARM,    rUpperWing);
        add(joints, jointList, jointIndex, R_SECONDARIES, rForearm);
        BirdJoint rHand      = add(joints, jointList, jointIndex, R_HAND,       rForearm);
        add(joints, jointList, jointIndex, R_PRIMARIES,   rHand);

        // Torso and hip off chest
        add(joints, jointList, jointIndex, TORSO, chest);
        BirdJoint hip = add(joints, jointList, jointIndex, HIP, chest);

        // Left leg chain
        BirdJoint lThigh  = add(joints, jointList, jointIndex, L_THIGH,  hip);
        BirdJoint lShin   = add(joints, jointList, jointIndex, L_SHIN,   lThigh);
        BirdJoint lTarsus = add(joints, jointList, jointIndex, L_TARSUS, lShin);
        add(joints, jointList, jointIndex, L_FOOT, lTarsus);

        // Right leg chain
        BirdJoint rThigh  = add(joints, jointList, jointIndex, R_THIGH,  hip);
        BirdJoint rShin   = add(joints, jointList, jointIndex, R_SHIN,   rThigh);
        BirdJoint rTarsus = add(joints, jointList, jointIndex, R_TARSUS, rShin);
        add(joints, jointList, jointIndex, R_FOOT, rTarsus);

        // Tail off chest
        BirdJoint tailBase = add(joints, jointList, jointIndex, TAIL_BASE, chest);
        add(joints, jointList, jointIndex, TAIL_FAN, tailBase);

        BirdSkeleton skeleton = new BirdSkeleton(
                Collections.unmodifiableMap(joints),
                Collections.unmodifiableList(jointList),
                Collections.unmodifiableMap(jointIndex));

        // Apply default spring tuning
        applyDefaultTuning(skeleton);

        return skeleton;
    }

    private static BirdJoint add(Map<String, BirdJoint> joints, List<BirdJoint> list,
                                  Map<String, Integer> index, String name, BirdJoint parent) {
        BirdJoint joint = new BirdJoint(name, parent);
        int idx = list.size();
        joints.put(name, joint);
        list.add(joint);
        index.put(name, idx);
        return joint;
    }

    private static void applyDefaultTuning(BirdSkeleton s) {
        // Neck joints
        for (String n : new String[]{NECK_LOWER, NECK_MID, NECK_UPPER}) {
            s.getJoint(n).setSpring(80f, 12f, 15f);
        }
        // Head / beak
        for (String n : new String[]{HEAD, UPPER_BEAK, LOWER_BEAK}) {
            s.getJoint(n).setSpring(70f, 10f, 12f);
        }
        // Spine
        for (String n : new String[]{CHEST, SHOULDER_MOUNT, TORSO, HIP}) {
            s.getJoint(n).setSpring(50f, 8f, 8f);
        }
        // Wing bones
        for (String n : new String[]{L_UPPER_WING, L_FOREARM, L_HAND, R_UPPER_WING, R_FOREARM, R_HAND}) {
            s.getJoint(n).setSpring(40f, 5f, 10f);
        }
        // Trailing feathers
        for (String n : new String[]{L_SCAPULARS, L_SECONDARIES, L_PRIMARIES, R_SCAPULARS, R_SECONDARIES, R_PRIMARIES}) {
            s.getJoint(n).setSpring(15f, 2f, 8f);
        }
        // Leg bones
        for (String n : new String[]{L_THIGH, L_SHIN, L_TARSUS, R_THIGH, R_SHIN, R_TARSUS}) {
            s.getJoint(n).setSpring(60f, 9f, 10f);
        }
        // Feet
        for (String n : new String[]{L_FOOT, R_FOOT}) {
            s.getJoint(n).setSpring(45f, 7f, 8f);
        }
        // Tail
        for (String n : new String[]{TAIL_BASE, TAIL_FAN}) {
            s.getJoint(n).setSpring(20f, 3f, 6f);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the joint with the given name, or null if not found. */
    public BirdJoint getJoint(String name) {
        return joints.get(name);
    }

    /** Returns an unmodifiable list of all joints in insertion (hierarchy) order. */
    public List<BirdJoint> getAllJoints() {
        return jointList;
    }

    /**
     * Returns the flat-array index for the given joint name.
     * The angle/velocity arrays in {@link BirdSkeletonState} are indexed as:
     * {@code index * 3 + 0} = X, {@code index * 3 + 1} = Y, {@code index * 3 + 2} = Z.
     */
    public int getJointIndex(String name) {
        Integer idx = jointIndex.get(name);
        if (idx == null) throw new IllegalArgumentException("Unknown joint: " + name);
        return idx;
    }

    /**
     * Copies angle and velocity data from the given state into joint fields.
     */
    public void loadState(BirdSkeletonState state) {
        for (int i = 0; i < jointList.size(); i++) {
            BirdJoint j = jointList.get(i);
            int base = i * BirdSkeletonState.AXIS_COUNT;
            j.angleX = state.angles[base];
            j.angleY = state.angles[base + 1];
            j.angleZ = state.angles[base + 2];
            j.velX   = state.velocities[base];
            j.velY   = state.velocities[base + 1];
            j.velZ   = state.velocities[base + 2];
        }
    }

    /**
     * Copies angle and velocity data from joint fields back into the given state.
     */
    public void saveState(BirdSkeletonState state) {
        for (int i = 0; i < jointList.size(); i++) {
            BirdJoint j = jointList.get(i);
            int base = i * BirdSkeletonState.AXIS_COUNT;
            state.angles[base]         = j.angleX;
            state.angles[base + 1]     = j.angleY;
            state.angles[base + 2]     = j.angleZ;
            state.velocities[base]     = j.velX;
            state.velocities[base + 1] = j.velY;
            state.velocities[base + 2] = j.velZ;
        }
    }
}
