package com.birbs.britishbirds.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import java.util.HashMap;
import java.util.Map;

public class SkeletonModelMapper {
    private final Map<String, ModelPart> bindings;

    private SkeletonModelMapper(Map<String, ModelPart> bindings) {
        this.bindings = Map.copyOf(bindings);
    }

    public void apply(BirdSkeleton skeleton) {
        for (var entry : bindings.entrySet()) {
            BirdJoint joint = skeleton.getJoint(entry.getKey());
            if (joint != null) {
                ModelPart part = entry.getValue();
                part.xRot = joint.angleX;
                part.yRot = joint.angleY;
                part.zRot = joint.angleZ;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, ModelPart> bindings = new HashMap<>();

        public Builder bind(String jointName, ModelPart part) {
            bindings.put(jointName, part);
            return this;
        }

        public SkeletonModelMapper build() {
            return new SkeletonModelMapper(bindings);
        }
    }
}
