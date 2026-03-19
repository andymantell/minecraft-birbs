package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class BlueTitRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isFlying = false;
    public boolean isPecking = false;
    public float flapAngle = 0.0f;
    public boolean isHangingUpsideDown = false;
}
