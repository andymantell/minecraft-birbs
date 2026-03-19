package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MallardRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isFlying = false;
    public boolean isSwimming = false;
    public boolean isDabbling = false;
    public boolean isWaddling = false;
    public boolean isBaby = false;
    public float flapAngle = 0.0f;
}
