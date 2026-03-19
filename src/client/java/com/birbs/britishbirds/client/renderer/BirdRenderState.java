package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Base render state for all British bird species.
 * Contains fields common to every bird: sex, flight state, and wing flap angle.
 */
public class BirdRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isBaby = false;
    public boolean isFlying = false;
    public float flapAngle = 0.0f;
}
