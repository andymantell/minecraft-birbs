import java.util.*;

/**
 * Preset definitions and pose helper methods.
 * Extracted from PoseEditor during refactor — no logic changes.
 */
class PosePresets {

    // =========================================================================
    // Preset class
    // =========================================================================

    /** Named preset: archetype + pose name + joint values. */
    static class Preset {
        final String name;
        final Map<String, float[]> joints;
        // If non-null, this is a cyclic preset: basePose + offsetA (phase=0) or offsetB (phase=1)
        final Map<String, float[]> basePose;    // base pose values (e.g. flying_cruise + legs_tucked)
        final Map<String, float[]> offsetA;     // cyclic offset A (phase = 0)
        final Map<String, float[]> offsetB;     // cyclic offset B (phase = 1)
        final String cyclicName;                // e.g. "wingbeat"
        final String endpointName;              // e.g. "wings_up"

        Preset(String name, Map<String, float[]> joints) {
            this.name = name;
            this.joints = joints;
            this.basePose = null;
            this.offsetA = null;
            this.offsetB = null;
            this.cyclicName = null;
            this.endpointName = null;
        }

        /** Constructor for cyclic presets. joints = combined (base + this endpoint's offset). */
        Preset(String name, Map<String, float[]> joints,
               Map<String, float[]> basePose, Map<String, float[]> offsetA,
               Map<String, float[]> offsetB, String cyclicName, String endpointName) {
            this.name = name;
            this.joints = joints;
            this.basePose = basePose;
            this.offsetA = offsetA;
            this.offsetB = offsetB;
            this.cyclicName = cyclicName;
            this.endpointName = endpointName;
        }

        boolean isCyclic() { return cyclicName != null; }
    }

    // =========================================================================
    // Pose helpers
    // =========================================================================

    /** Pose = map from joint name to {xRot, yRot, zRot}. */
    static Map<String, float[]> pose(Object... args) {
        Map<String, float[]> m = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 4) {
            String name = (String) args[i];
            float x = ((Number) args[i+1]).floatValue();
            float y = ((Number) args[i+2]).floatValue();
            float z = ((Number) args[i+3]).floatValue();
            m.put(name, new float[]{x, y, z});
        }
        return m;
    }

    static void mirror(Map<String, float[]> pose) {
        Map<String, float[]> mirrored = new LinkedHashMap<>();
        for (var entry : pose.entrySet()) {
            String name = entry.getKey();
            float[] v = entry.getValue();
            if (name.startsWith("L_")) {
                String rName = "R_" + name.substring(2);
                if (!pose.containsKey(rName)) {
                    // Lateral wing geometry: yRot flips (fold direction), zRot does NOT (flap direction)
                    // xRot stays the same (pitch is symmetric)
                    mirrored.put(rName, new float[]{v[0], -v[1], -v[2]});
                }
            }
        }
        pose.putAll(mirrored);
    }

    static Preset preset(String name, Object... args) {
        Map<String, float[]> joints = pose(args);
        mirror(joints);
        return new Preset(name, joints);
    }

    /** Build a pose offset map from flat args (like pose()), then auto-mirror L_ entries. */
    static Map<String, float[]> offsetPose(Object... args) {
        Map<String, float[]> m = pose(args);
        mirror(m);
        return m;
    }

    /** Merge a base pose with an offset map: result[joint] = base + offset (per-component). */
    static Map<String, float[]> mergePoseOffset(Map<String, float[]> base, Map<String, float[]> offset) {
        Map<String, float[]> result = new LinkedHashMap<>(base);
        for (var entry : offset.entrySet()) {
            String name = entry.getKey();
            float[] off = entry.getValue();
            float[] existing = result.get(name);
            if (existing != null) {
                result.put(name, new float[]{existing[0] + off[0], existing[1] + off[1], existing[2] + off[2]});
            } else {
                result.put(name, new float[]{off[0], off[1], off[2]});
            }
        }
        return result;
    }

    /**
     * Build a pair of cyclic presets (endpoint A and B) from a base pose and two offset maps.
     * The returned list has [endpointA, endpointB].
     */
    static List<Preset> cyclicPresets(String cyclicName, String nameA, String nameB,
                                       Map<String, float[]> basePose,
                                       Map<String, float[]> offsetA,
                                       Map<String, float[]> offsetB) {
        Map<String, float[]> combinedA = mergePoseOffset(basePose, offsetA);
        Map<String, float[]> combinedB = mergePoseOffset(basePose, offsetB);

        String displayNameA = cyclicName + ": " + nameA;
        String displayNameB = cyclicName + ": " + nameB;

        Preset pA = new Preset(displayNameA, combinedA, basePose, offsetA, offsetB, cyclicName, nameA);
        Preset pB = new Preset(displayNameB, combinedB, basePose, offsetA, offsetB, cyclicName, nameB);
        return List.of(pA, pB);
    }

    // =========================================================================
    // Preset name helpers
    // =========================================================================

    static String shortenPresetName(String name) {
        if (name.contains(": ")) {
            String[] parts = name.split(": ", 2);
            String anim = parts[0];
            String ep = parts[1];
            if (ep.equals("wings_up") || ep.equals("up")) return "Wing\u2191";
            if (ep.equals("wings_down") || ep.equals("down")) return "Wing\u2193";
            if (ep.equals("legs_forward")) return "Walk\u2192";
            if (ep.equals("legs_back")) return "Walk\u2190";
            if (ep.equals("crouch")) return "Hop\u2193";
            if (ep.equals("spring")) return "Hop\u2191";
            return anim.substring(0, Math.min(4, anim.length())) + ":" + ep.substring(0, Math.min(3, ep.length()));
        }
        switch (name) {
            case "flying_cruise":  return "Cruise";
            case "flying_takeoff": return "Takeoff";
            case "flying_land":    return "Land";
            case "raptor_perch":   return "R.Perch";
            default:
                return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    /** True if this preset represents the "A" (phase=0) endpoint. */
    static boolean isEndpointA(Preset p) {
        if (!p.isCyclic()) return false;
        Map<String, float[]> expectedA = mergePoseOffset(p.basePose, p.offsetA);
        return mapsApproxEqual(p.joints, expectedA);
    }

    static String getEndpointNameA(String displayName) {
        int idx = displayName.indexOf(": ");
        return idx >= 0 ? displayName.substring(idx + 2) : displayName;
    }

    static boolean mapsApproxEqual(Map<String, float[]> a, Map<String, float[]> b) {
        if (a.size() != b.size()) return false;
        for (var entry : a.entrySet()) {
            float[] bv = b.get(entry.getKey());
            if (bv == null) return false;
            float[] av = entry.getValue();
            if (Math.abs(av[0]-bv[0]) > 0.001f || Math.abs(av[1]-bv[1]) > 0.001f || Math.abs(av[2]-bv[2]) > 0.001f)
                return false;
        }
        return true;
    }

    // =========================================================================
    // Build all presets
    // =========================================================================

    static Map<String, List<Preset>> buildPresets() {
        Map<String, List<Preset>> presets = new LinkedHashMap<>();

        // --- Base poses (shared) ---
        List<Preset> base = new ArrayList<>();
        base.add(preset("perched",  // synced with mod BaseBirdPoses.PERCHED (tuned 2026-03-20)
                "chest", 0.1f, 0f, 0f,
                "neck_lower", 0.42f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", -0.05f, 0f, 0f,
                "L_upper_wing", 1.16f, -0.02f, 1.35f,
                "L_scapulars", 0f, -0.2f, 0f,
                "L_forearm", 0f, 0.08f, 0.2f,
                "L_secondaries", 0f, -0.15f, 0.08f,
                "L_hand", 0f, 0.05f, 0.36f,
                "L_primaries", 0.05f, -0.1f, -0.17f,
                "tail_base", -0.2f, 0f, 0f,
                "L_thigh", 0.1f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.4f, 0f, 0f));
        base.add(preset("alert",
                "chest", 0.0f, 0f, 0f,
                "neck_lower", -0.2f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.15f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.1f, 0f, 0f));
        base.add(preset("flying_cruise",  // synced with mod BaseBirdPoses.FLYING_CRUISE
                "chest", 1.0f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.1f,
                "L_forearm", 0f, 0f, 0f,
                "L_hand", 0f, 0f, 0f,
                "tail_base", -0.65f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f));
        base.add(preset("flying_takeoff",
                "chest", 1.2f, 0f, 0f,
                "torso", 0.2f, 0f, 0f,
                "hip", 0.15f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.6f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.5f,
                "L_forearm", 0f, 0f, 0f,
                "L_hand", 0f, 0f, 0f,
                "tail_base", -0.8f, 0f, 0f,
                "tail_fan", -0.2f, 0f, 0f));
        base.add(preset("flying_land",
                "chest", -0.3f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.8f,
                "L_forearm", 0f, 0f, -0.1f,
                "L_hand", 0f, 0f, -0.05f,
                "tail_base", -0.8f, 0f, 0f,
                "L_thigh", -0.3f, 0f, 0f,
                "L_shin", -0.5f, 0f, 0f));
        base.add(preset("tucked",
                "chest", 0.15f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", 0.3f, 0f, 0.4f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", 0.1f, 0f, 0f));

        // --- Shared cyclic base poses ---
        Map<String, float[]> flightBase = new LinkedHashMap<>();
        flightBase.put("chest",        new float[]{1.0f, 0f, 0f});
        flightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        flightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        flightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        flightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        flightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        flightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        flightBase.put("L_upper_wing", new float[]{0f, 0f, -0.1f});
        flightBase.put("L_forearm",    new float[]{0f, 0f, 0f});
        flightBase.put("L_hand",       new float[]{0f, 0f, 0f});
        flightBase.put("tail_base",    new float[]{-0.65f, 0f, 0f});
        flightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        flightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        flightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        flightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        flightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(flightBase);

        // WINGBEAT offsets
        Map<String, float[]> wingbeatOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.4f,
            "L_forearm",     0f, 0f, -0.15f,
            "L_hand",        0f, 0f, -0.1f,
            "L_scapulars",   0f, 0f, -0.1f,
            "L_secondaries", 0f, 0f, -0.08f,
            "L_primaries",   0f, 0f, -0.06f);
        Map<String, float[]> wingbeatOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.4f,
            "L_forearm",     0f, 0f, 0.1f,
            "L_hand",        0f, 0f, 0.08f,
            "L_scapulars",   0f, 0f, 0.08f,
            "L_secondaries", 0f, 0f, 0.06f,
            "L_primaries",   0f, 0f, 0.05f);

        // WALK_CYCLE offsets
        Map<String, float[]> walkOffA = pose(
            "L_thigh",  -0.3f, 0f, 0f,
            "L_shin",    0.4f, 0f, 0f,
            "L_tarsus", -0.2f, 0f, 0f,
            "R_thigh",   0.3f, 0f, 0f,
            "R_shin",   -0.1f, 0f, 0f,
            "R_tarsus",  0.1f, 0f, 0f);
        Map<String, float[]> walkOffB = pose(
            "L_thigh",   0.3f, 0f, 0f,
            "L_shin",   -0.1f, 0f, 0f,
            "L_tarsus",  0.1f, 0f, 0f,
            "R_thigh",  -0.3f, 0f, 0f,
            "R_shin",    0.4f, 0f, 0f,
            "R_tarsus", -0.2f, 0f, 0f);
        Map<String, float[]> walkBase = new LinkedHashMap<>();

        // HOP offsets
        Map<String, float[]> hopOffA = pose(
            "chest",    0.15f, 0f, 0f,
            "L_thigh",  0.3f,  0f, 0f,
            "L_shin",   0.5f,  0f, 0f,
            "L_tarsus", -0.4f, 0f, 0f,
            "R_thigh",  0.3f,  0f, 0f,
            "R_shin",   0.5f,  0f, 0f,
            "R_tarsus", -0.4f, 0f, 0f);
        Map<String, float[]> hopOffB = pose(
            "chest",    -0.1f,  0f, 0f,
            "L_thigh",  -0.15f, 0f, 0f,
            "L_shin",   -0.1f,  0f, 0f,
            "L_tarsus",  0.1f,  0f, 0f,
            "R_thigh",  -0.15f, 0f, 0f,
            "R_shin",   -0.1f,  0f, 0f,
            "R_tarsus",  0.1f,  0f, 0f);
        Map<String, float[]> hopBase = new LinkedHashMap<>();

        // RAPTOR_WINGBEAT offsets
        Map<String, float[]> raptorWingOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.5f,
            "L_forearm",     0f, 0f, -0.2f,
            "L_hand",        0f, 0f, -0.15f,
            "L_scapulars",   0f, 0f, -0.12f,
            "L_secondaries", 0f, 0f, -0.1f,
            "L_primaries",   0f, 0f, -0.08f);
        Map<String, float[]> raptorWingOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.5f,
            "L_forearm",     0f, 0f, 0.15f,
            "L_hand",        0f, 0f, 0.1f,
            "L_scapulars",   0f, 0f, 0.1f,
            "L_secondaries", 0f, 0f, 0.08f,
            "L_primaries",   0f, 0f, 0.06f);

        // Raptor flight base
        Map<String, float[]> raptorFlightBase = new LinkedHashMap<>();
        raptorFlightBase.put("chest",        new float[]{1.1f, 0f, 0f});
        raptorFlightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        raptorFlightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        raptorFlightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        raptorFlightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        raptorFlightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        raptorFlightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        raptorFlightBase.put("L_upper_wing", new float[]{0f, 0f, -0.4f});
        raptorFlightBase.put("L_forearm",    new float[]{0f, 0f, -0.05f});
        raptorFlightBase.put("L_hand",       new float[]{0f, 0f, -0.03f});
        raptorFlightBase.put("tail_base",    new float[]{-0.7f, 0f, 0f});
        raptorFlightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        raptorFlightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        raptorFlightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        raptorFlightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        raptorFlightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(raptorFlightBase);

        // WATERFOWL_WINGBEAT offsets
        Map<String, float[]> waterfowlWingOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.5f,
            "L_forearm",     0f, 0f, -0.18f,
            "L_hand",        0f, 0f, -0.12f,
            "L_scapulars",   0f, 0f, -0.1f,
            "L_secondaries", 0f, 0f, -0.08f,
            "L_primaries",   0f, 0f, -0.06f);
        Map<String, float[]> waterfowlWingOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.5f,
            "L_forearm",     0f, 0f, 0.12f,
            "L_hand",        0f, 0f, 0.1f,
            "L_scapulars",   0f, 0f, 0.08f,
            "L_secondaries", 0f, 0f, 0.06f,
            "L_primaries",   0f, 0f, 0.05f);

        // Waterfowl flight base
        Map<String, float[]> waterfowlFlightBase = new LinkedHashMap<>();
        waterfowlFlightBase.put("chest",        new float[]{1.0f, 0f, 0f});
        waterfowlFlightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        waterfowlFlightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        waterfowlFlightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        waterfowlFlightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        waterfowlFlightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        waterfowlFlightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        waterfowlFlightBase.put("L_upper_wing", new float[]{0f, 0f, -0.1f});
        waterfowlFlightBase.put("L_forearm",    new float[]{0f, 0f, 0f});
        waterfowlFlightBase.put("L_hand",       new float[]{0f, 0f, 0f});
        waterfowlFlightBase.put("tail_base",    new float[]{-0.65f, 0f, 0f});
        waterfowlFlightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        waterfowlFlightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        waterfowlFlightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        waterfowlFlightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        waterfowlFlightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(waterfowlFlightBase);

        // WADDLE offsets
        Map<String, float[]> waddleOffA = pose(
            "chest",    0f, 0f, -0.12f,
            "L_thigh", -0.3f, 0f, 0f,
            "L_shin",   0.4f, 0f, 0f,
            "L_tarsus",-0.2f, 0f, 0f,
            "R_thigh",  0.3f, 0f, 0f,
            "R_shin",  -0.1f, 0f, 0f,
            "R_tarsus", 0.1f, 0f, 0f);
        Map<String, float[]> waddleOffB = pose(
            "chest",    0f, 0f,  0.12f,
            "L_thigh",  0.3f, 0f, 0f,
            "L_shin",  -0.1f, 0f, 0f,
            "L_tarsus", 0.1f, 0f, 0f,
            "R_thigh", -0.3f, 0f, 0f,
            "R_shin",   0.4f, 0f, 0f,
            "R_tarsus",-0.2f, 0f, 0f);
        Map<String, float[]> waddleBase = new LinkedHashMap<>();

        // --- Passerine-specific ---
        List<Preset> passerine = new ArrayList<>(base);
        passerine.add(preset("forage",
                "chest", 0.3f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", 0.4f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.15f, 0f, 0f,
                "L_thigh", 0.2f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.4f, 0f, 0f));
        passerine.addAll(cyclicPresets("wingbeat", "wings_up", "wings_down",
                flightBase, wingbeatOffA, wingbeatOffB));
        passerine.addAll(cyclicPresets("walk", "legs_forward", "legs_back",
                walkBase, walkOffA, walkOffB));
        passerine.addAll(cyclicPresets("hop", "crouch", "spring",
                hopBase, hopOffA, hopOffB));
        presets.put("Passerine", passerine);

        // --- Raptor-specific ---
        List<Preset> raptor = new ArrayList<>(base);
        raptor.add(preset("soar",
                "chest", 1.1f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.4f,
                "L_forearm", 0f, 0f, -0.05f,
                "L_hand", 0f, 0f, -0.03f,
                "tail_base", -0.7f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f,
                "L_thigh", -1.5f, 0f, 0f,
                "L_shin", -2.5f, 0f, 0f,
                "L_tarsus", 2.0f, 0f, 0f,
                "L_foot", -0.8f, 0f, 0f));
        raptor.add(preset("stoop",
                "chest", 1.0f, 0f, 0f,
                "neck_lower", -0.3f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.15f, 0f, 0f,
                "head", -0.4f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.1f,
                "L_forearm", 0f, 2.5f, 0f,
                "L_hand", 0f, -2.3f, 0f,
                "tail_base", -0.1f, 0f, 0f,
                "tail_fan", 0.1f, 0f, 0f,
                "L_thigh", -1.5f, 0f, 0f,
                "L_shin", -2.5f, 0f, 0f,
                "L_tarsus", 2.0f, 0f, 0f,
                "L_foot", -0.8f, 0f, 0f));
        raptor.add(preset("hover",
                "chest", 0.6f, 0f, 0f,
                "torso", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.3f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.5f,
                "L_forearm", 0f, 0f, -0.05f,
                "L_hand", 0f, 0f, -0.03f,
                "tail_base", -0.5f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.1f, 0f, 0f,
                "L_tarsus", -0.1f, 0f, 0f));
        raptor.add(preset("raptor_perch",
                "chest", 0.35f, 0f, 0f,
                "neck_lower", -0.15f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", -0.15f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", 0.25f, 0f, 0f,
                "L_thigh", 0.15f, 0f, 0f,
                "L_shin", 0.4f, 0f, 0f,
                "L_tarsus", -0.5f, 0f, 0f));
        raptor.add(preset("strike",
                "chest", 0.4f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.2f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.6f,
                "L_forearm", 0f, 0f, -0.1f,
                "L_hand", 0f, 0f, -0.05f,
                "tail_base", -0.6f, 0f, 0f,
                "L_thigh", -0.6f, 0f, 0f,
                "L_shin", -0.3f, 0f, 0f,
                "L_tarsus", 0.2f, 0f, 0f,
                "L_foot", -0.4f, 0f, 0f));
        raptor.addAll(cyclicPresets("raptor_wingbeat", "up", "down",
                raptorFlightBase, raptorWingOffA, raptorWingOffB));
        raptor.addAll(cyclicPresets("walk", "legs_forward", "legs_back",
                walkBase, walkOffA, walkOffB));
        presets.put("Raptor", raptor);

        // --- Waterfowl-specific ---
        List<Preset> waterfowl = new ArrayList<>(base);
        waterfowl.add(preset("swim",
                "chest", 0.0f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.05f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.17f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.4f, 0f, 0f,
                "L_tarsus", -0.2f, 0f, 0f));
        waterfowl.add(preset("dabble",
                "chest", 1.3f, 0f, 0f,
                "neck_lower", 0.2f, 0f, 0f,
                "neck_mid", 0.1f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", 0.2f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -1.2f, 0f, 0f,
                "tail_fan", -0.3f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.1f, 0f, 0f));
        waterfowl.addAll(cyclicPresets("waterfowl_wingbeat", "up", "down",
                waterfowlFlightBase, waterfowlWingOffA, waterfowlWingOffB));
        waterfowl.addAll(cyclicPresets("walk", "legs_forward", "legs_back",
                walkBase, walkOffA, walkOffB));
        waterfowl.addAll(cyclicPresets("waddle", "waddle_left", "waddle_right",
                waddleBase, waddleOffA, waddleOffB));
        presets.put("Waterfowl", waterfowl);

        return presets;
    }
}
