package com.birbs.britishbirds.client.model;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Shared animation helpers used across all bird model classes.
 * Eliminates duplication of common animation patterns like walking legs,
 * wing flapping, body flight pitch, and tail bobbing.
 */
public final class BirdAnimations {

    private BirdAnimations() {}

    /**
     * Standard walking leg swing animation. Identical formula used by all bird species.
     */
    public static void animateWalkingLegs(ModelPart leftLeg, ModelPart rightLeg,
                                           float walkSpeed, float walkPos) {
        if (walkSpeed > 0.01f) {
            float legSwing = (float) Math.sin(walkPos * 0.6662f) * 1.4f * walkSpeed;
            leftLeg.xRot = legSwing;
            rightLeg.xRot = -legSwing;
        } else {
            leftLeg.xRot = 0.0f;
            rightLeg.xRot = 0.0f;
        }
    }

    /**
     * Standard symmetric wing flap: left wing negative, right wing positive.
     * @param scale multiplier on flapAngle (1.0 for normal, 0.5 for shallow like peregrine)
     */
    public static void animateWingFlap(ModelPart leftWing, ModelPart rightWing,
                                        float flapAngle, float scale) {
        leftWing.zRot = -flapAngle * scale;
        rightWing.zRot = flapAngle * scale;
    }

    /** Wing flap with default 1.0 scale. */
    public static void animateWingFlap(ModelPart leftWing, ModelPart rightWing, float flapAngle) {
        animateWingFlap(leftWing, rightWing, flapAngle, 1.0f);
    }

    /** Fold wings against body (grounded posture). */
    public static void foldWings(ModelPart leftWing, ModelPart rightWing) {
        leftWing.zRot = 0.0f;
        rightWing.zRot = 0.0f;
    }

    /** Tuck legs back for flight. */
    public static void tuckLegs(ModelPart leftLeg, ModelPart rightLeg, float angle) {
        leftLeg.xRot = angle;
        rightLeg.xRot = angle;
    }

    /**
     * Gentle sinusoidal tail bob for idle/perched birds.
     * @param baseAngle the tail's rest angle in radians
     * @param speed oscillation speed
     * @param amplitude oscillation amplitude
     */
    public static void animateTailBob(ModelPart tail, float baseAngle,
                                       float ageInTicks, float speed, float amplitude) {
        tail.xRot = baseAngle + (float) Math.sin(ageInTicks * speed) * amplitude;
    }
}
