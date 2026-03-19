package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class BarnOwlRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isFlying = false;
    public boolean isHovering = false;
    public float flapAngle = 0.0f;
}
