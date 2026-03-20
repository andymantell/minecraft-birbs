package com.birbs.britishbirds.client.animation;

public class BirdJoint {
    public final String name;
    public final BirdJoint parent;

    // Current state
    public float angleX, angleY, angleZ;
    public float velX, velY, velZ;

    // Targets (set by pose resolver + procedural layer)
    public float targetX, targetY, targetZ;

    // Spring parameters
    public float stiffness = 50.0f;
    public float damping = 8.0f;
    public float maxVelocity = 10.0f;

    // Constraints (radians)
    public float minX = -3.14f, maxX = 3.14f;
    public float minY = -3.14f, maxY = 3.14f;
    public float minZ = -3.14f, maxZ = 3.14f;

    public BirdJoint(String name, BirdJoint parent) {
        this.name = name;
        this.parent = parent;
    }

    public void setTarget(float x, float y, float z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public void setSpring(float stiffness, float damping, float maxVelocity) {
        this.stiffness = stiffness;
        this.damping = damping;
        this.maxVelocity = maxVelocity;
    }

    public void setConstraints(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.minZ = minZ; this.maxZ = maxZ;
    }

    public void clampAngles() {
        angleX = Math.clamp(angleX, minX, maxX);
        angleY = Math.clamp(angleY, minY, maxY);
        angleZ = Math.clamp(angleZ, minZ, maxZ);
    }
}
