package com.birbs.britishbirds.client.animation.pose;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads pose data from JSON files bundled in the mod's resources.
 *
 * <p>JSON files live at {@code assets/britishbirds/poses/<archetype>_poses.json}
 * and are read from the classpath at client initialisation time. Each file
 * contains static poses, cyclic animations, and overlay poses for one archetype.
 *
 * <p>If a JSON file is missing or malformed, the loader logs a warning and
 * returns null, allowing callers to fall back to hardcoded defaults.
 */
public final class PoseLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("britishbirds");

    private static final String RESOURCE_PATH = "/assets/britishbirds/poses/";

    private static Map<String, Map<String, PoseData>> staticPoses = new HashMap<>();
    private static Map<String, Map<String, CyclicAnimation>> cyclicAnimations = new HashMap<>();
    private static Map<String, Map<String, PoseData>> overlayPoses = new HashMap<>();

    private static volatile boolean loaded = false;

    private PoseLoader() {}

    /**
     * Loads all three archetype JSON files. Call once during client init.
     */
    public static void loadAll() {
        loadArchetype("passerine");
        loadArchetype("raptor");
        loadArchetype("waterfowl");
        // Freeze maps to prevent accidental mutation after loading
        staticPoses = Collections.unmodifiableMap(staticPoses);
        cyclicAnimations = Collections.unmodifiableMap(cyclicAnimations);
        overlayPoses = Collections.unmodifiableMap(overlayPoses);
        loaded = true;
        LOGGER.info("[BritishBirds] Loaded pose data from JSON for {} archetypes", staticPoses.size());
    }

    /**
     * Returns true if JSON poses have been loaded.
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Gets a static or overlay pose from JSON, or returns the fallback if not available.
     */
    public static PoseData getOrDefault(String archetype, String poseName, PoseData fallback) {
        if (!loaded) return fallback;

        // Check static poses first
        Map<String, PoseData> statics = staticPoses.get(archetype);
        if (statics != null) {
            PoseData pose = statics.get(poseName);
            if (pose != null) return pose;
        }

        // Check overlay poses
        Map<String, PoseData> overlays = overlayPoses.get(archetype);
        if (overlays != null) {
            PoseData pose = overlays.get(poseName);
            if (pose != null) return pose;
        }

        return fallback;
    }

    /**
     * Gets a cyclic animation from JSON, or returns the fallback if not available.
     */
    public static CyclicAnimation getCyclicOrDefault(String archetype, String cyclicName, CyclicAnimation fallback) {
        if (!loaded) return fallback;

        Map<String, CyclicAnimation> cyclics = cyclicAnimations.get(archetype);
        if (cyclics != null) {
            CyclicAnimation anim = cyclics.get(cyclicName);
            if (anim != null) return anim;
        }

        return fallback;
    }

    // =========================================================================
    // Internal loading
    // =========================================================================

    private static void loadArchetype(String archetype) {
        String path = RESOURCE_PATH + archetype + "_poses.json";
        try (InputStream is = PoseLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                LOGGER.warn("[BritishBirds] Pose JSON not found: {}", path);
                return;
            }

            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();

            // Static poses
            if (root.has("static_poses")) {
                Map<String, PoseData> poses = new HashMap<>();
                JsonObject staticObj = root.getAsJsonObject("static_poses");
                for (Map.Entry<String, JsonElement> entry : staticObj.entrySet()) {
                    PoseData pose = parsePoseData(entry.getKey(), entry.getValue().getAsJsonObject());
                    if (pose != null) poses.put(entry.getKey(), pose);
                }
                staticPoses.put(archetype, poses);
            }

            // Cyclic animations
            if (root.has("cyclic_animations")) {
                Map<String, CyclicAnimation> cyclics = new HashMap<>();
                JsonObject cyclicObj = root.getAsJsonObject("cyclic_animations");
                for (Map.Entry<String, JsonElement> entry : cyclicObj.entrySet()) {
                    CyclicAnimation anim = parseCyclicAnimation(entry.getKey(),
                            entry.getValue().getAsJsonObject());
                    if (anim != null) cyclics.put(entry.getKey(), anim);
                }
                cyclicAnimations.put(archetype, cyclics);
            }

            // Overlay poses
            if (root.has("overlay_poses")) {
                Map<String, PoseData> overlays = new HashMap<>();
                JsonObject overlayObj = root.getAsJsonObject("overlay_poses");
                for (Map.Entry<String, JsonElement> entry : overlayObj.entrySet()) {
                    PoseData pose = parseOverlayPoseData(entry.getKey(),
                            entry.getValue().getAsJsonObject());
                    if (pose != null) overlays.put(entry.getKey(), pose);
                }
                overlayPoses.put(archetype, overlays);
            }

            LOGGER.debug("[BritishBirds] Loaded poses for archetype: {}", archetype);

        } catch (Exception e) {
            LOGGER.error("[BritishBirds] Failed to load pose JSON for {}: {}", archetype, e.getMessage());
        }
    }

    /**
     * Parses a static pose object with joints and spring_overrides.
     * Joints with L_ prefixes are auto-mirrored to R_ equivalents.
     */
    private static PoseData parsePoseData(String name, JsonObject obj) {
        try {
            PoseData.Builder builder = PoseData.builder(name);

            if (obj.has("joints")) {
                parseJoints(builder, obj.getAsJsonObject("joints"));
            }

            if (obj.has("spring_overrides")) {
                parseSpringOverrides(builder, obj.getAsJsonObject("spring_overrides"));
            }

            // Auto-mirror L_ entries
            builder.mirror();

            return builder.build();
        } catch (Exception e) {
            LOGGER.warn("[BritishBirds] Failed to parse pose '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Parses an overlay pose object. Overlays may or may not need mirroring
     * (legs_tucked does, beak_open does not). We mirror unconditionally since
     * mirror() is a no-op if there are no L_ prefixed joints.
     */
    private static PoseData parseOverlayPoseData(String name, JsonObject obj) {
        try {
            PoseData.Builder builder = PoseData.builder(name);

            if (obj.has("joints")) {
                parseJoints(builder, obj.getAsJsonObject("joints"));
            }

            builder.mirror();

            return builder.build();
        } catch (Exception e) {
            LOGGER.warn("[BritishBirds] Failed to parse overlay '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Parses a cyclic animation with offset_a and offset_b sub-poses.
     * Each offset's L_ joints are auto-mirrored.
     */
    private static CyclicAnimation parseCyclicAnimation(String name, JsonObject obj) {
        try {
            JsonObject offsetAObj = obj.getAsJsonObject("offset_a");
            JsonObject offsetBObj = obj.getAsJsonObject("offset_b");

            PoseData offsetA = parseCyclicOffset(name + "_a", offsetAObj);
            PoseData offsetB = parseCyclicOffset(name + "_b", offsetBObj);

            if (offsetA == null || offsetB == null) return null;

            return new CyclicAnimation(name, offsetA, offsetB);
        } catch (Exception e) {
            LOGGER.warn("[BritishBirds] Failed to parse cyclic '{}': {}", name, e.getMessage());
            return null;
        }
    }

    private static PoseData parseCyclicOffset(String name, JsonObject obj) {
        PoseData.Builder builder = PoseData.builder(name);
        if (obj.has("joints")) {
            parseJoints(builder, obj.getAsJsonObject("joints"));
        }
        builder.mirror();
        return builder.build();
    }

    private static void parseJoints(PoseData.Builder builder, JsonObject joints) {
        for (Map.Entry<String, JsonElement> entry : joints.entrySet()) {
            var arr = entry.getValue().getAsJsonArray();
            float x = arr.get(0).getAsFloat();
            float y = arr.get(1).getAsFloat();
            float z = arr.get(2).getAsFloat();
            builder.joint(entry.getKey(), x, y, z);
        }
    }

    private static void parseSpringOverrides(PoseData.Builder builder, JsonObject springs) {
        for (Map.Entry<String, JsonElement> entry : springs.entrySet()) {
            var arr = entry.getValue().getAsJsonArray();
            float stiffness = arr.get(0).getAsFloat();
            float damping = arr.get(1).getAsFloat();
            builder.spring(entry.getKey(), stiffness, damping);
        }
    }
}
