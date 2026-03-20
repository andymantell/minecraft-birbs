package com.birbs.britishbirds.client.animation.pose;

import org.joml.Vector3f;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PoseData {

    public record SpringOverride(float stiffness, float damping) {}

    private final String name;
    private final Map<String, Vector3f> jointAngles;
    private final Map<String, SpringOverride> springOverrides;

    private PoseData(String name, Map<String, Vector3f> jointAngles, Map<String, SpringOverride> springOverrides) {
        this.name = name;
        this.jointAngles = jointAngles;
        this.springOverrides = springOverrides;
    }

    public String getName() {
        return name;
    }

    public Vector3f getAngle(String jointName) {
        return jointAngles.get(jointName);
    }

    public SpringOverride getSpringOverride(String jointName) {
        return springOverrides.get(jointName);
    }

    public boolean hasJoint(String jointName) {
        return jointAngles.containsKey(jointName);
    }

    public Map<String, Vector3f> getJointAngles() {
        return jointAngles;
    }

    public Map<String, SpringOverride> getSpringOverrides() {
        return springOverrides;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {

        private final String name;
        private final Map<String, Vector3f> jointAngles = new HashMap<>();
        private final Map<String, SpringOverride> springOverrides = new HashMap<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder joint(String jointName, float x, float y, float z) {
            jointAngles.put(jointName, new Vector3f(x, y, z));
            return this;
        }

        public Builder spring(String jointName, float stiffness, float damping) {
            springOverrides.put(jointName, new SpringOverride(stiffness, damping));
            return this;
        }

        /**
         * Copies all L_-prefixed entries to R_ equivalents, flipping the sign on Y (yaw) and Z (roll).
         * Pitch (X) does not mirror.
         */
        public Builder mirror() {
            Map<String, Vector3f> toAdd = new HashMap<>();
            for (Map.Entry<String, Vector3f> entry : jointAngles.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("L_")) {
                    String mirroredKey = "R_" + key.substring(2);
                    Vector3f v = entry.getValue();
                    // yRot flips (fold direction mirrors), zRot does NOT (flap direction same for both wings)
                    toAdd.put(mirroredKey, new Vector3f(v.x, -v.y, v.z));
                }
            }
            jointAngles.putAll(toAdd);

            Map<String, SpringOverride> springsToAdd = new HashMap<>();
            for (Map.Entry<String, SpringOverride> entry : springOverrides.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("L_")) {
                    String mirroredKey = "R_" + key.substring(2);
                    springsToAdd.put(mirroredKey, entry.getValue());
                }
            }
            springOverrides.putAll(springsToAdd);

            return this;
        }

        public PoseData build() {
            return new PoseData(
                    name,
                    Collections.unmodifiableMap(new HashMap<>(jointAngles)),
                    Collections.unmodifiableMap(new HashMap<>(springOverrides))
            );
        }
    }
}
