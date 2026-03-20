package com.birbs.britishbirds.client.animation;

import java.util.List;

public class SpringSolver {

    public static void solve(BirdJoint joint, float deltaTime) {
        if (deltaTime <= 0.0f) return;
        if (deltaTime > 0.5f) deltaTime = 0.05f;

        float s = joint.stiffness;
        float d = joint.damping;
        float mv = joint.maxVelocity;

        // X axis
        float accX = s * (joint.targetX - joint.angleX) - d * joint.velX;
        joint.velX = Math.clamp(joint.velX + accX * deltaTime, -mv, mv);
        joint.angleX += joint.velX * deltaTime;

        // Y axis
        float accY = s * (joint.targetY - joint.angleY) - d * joint.velY;
        joint.velY = Math.clamp(joint.velY + accY * deltaTime, -mv, mv);
        joint.angleY += joint.velY * deltaTime;

        // Z axis
        float accZ = s * (joint.targetZ - joint.angleZ) - d * joint.velZ;
        joint.velZ = Math.clamp(joint.velZ + accZ * deltaTime, -mv, mv);
        joint.angleZ += joint.velZ * deltaTime;

        joint.clampAngles();
    }

    public static void solveAll(List<BirdJoint> joints, float deltaTime) {
        for (BirdJoint joint : joints) {
            solve(joint, deltaTime);
        }
    }
}
