package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

public interface ProceduralBehaviour {
    void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime);
}
