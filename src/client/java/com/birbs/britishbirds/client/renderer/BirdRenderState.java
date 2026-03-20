package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Base render state for all British bird species.
 * Contains fields common to every bird: sex, flight state, wing flap angle,
 * and skeletal animation parameters.
 */
public class BirdRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isBaby = false;
    public boolean isFlying = false;
    public float flapAngle = 0.0f;

    // Skeletal animation fields
    public float deltaTime = 0.05f;
    public float yawDelta = 0.0f;
    public float verticalVelocity = 0.0f;
    public float speed = 0.0f;
    public boolean justLanded = false;
    public boolean justStartled = false;
    public Vec3 lookTarget = null;
    public int entityId = 0;
}
