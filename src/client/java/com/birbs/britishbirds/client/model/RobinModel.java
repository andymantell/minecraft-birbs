package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.RobinRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RobinModel extends EntityModel<RobinRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public RobinModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body: plump round shape 4x5x5, positioned at center
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, -2.5f, -2.5f, 4.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, 19.0f, 0.0f));

        // Head: 3x3x3 with beak child
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 16.5f, -1.0f));

        // Beak: small protrusion on front of head
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(12, 10)
                        .addBox(-0.5f, -1.5f, -2.5f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Left wing
        partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .addBox(0.0f, -1.5f, -2.0f, 1.0f, 3.0f, 4.0f),
                PartPose.offset(2.0f, 18.5f, 0.0f));

        // Right wing
        partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .mirror()
                        .addBox(-1.0f, -1.5f, -2.0f, 1.0f, 3.0f, 4.0f),
                PartPose.offset(-2.0f, 18.5f, 0.0f));

        // Tail: slightly cocked upward at -15 degrees
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 18.5f, 2.5f,
                        (float) Math.toRadians(-15.0), 0.0f, 0.0f));

        // Left leg
        partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-0.5f, 0.0f, 0.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(1.0f, 21.0f, 0.0f));

        // Right leg
        partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-0.5f, 0.0f, 0.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-1.0f, 21.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(RobinRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isFlying) {
            // Flying animations
            // Wing flapping
            this.leftWing.zRot = -renderState.flapAngle;
            this.rightWing.zRot = renderState.flapAngle;

            // Tuck legs up
            this.leftLeg.xRot = 0.5f;
            this.rightLeg.xRot = 0.5f;

            // Tilt body forward
            this.body.xRot = -0.2f;
        } else {
            // Ground animations
            // Fold wings against body
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;

            // Reset body tilt
            this.body.xRot = 0.0f;

            // Gentle tail bob (sinusoidal)
            this.tail.xRot = (float) Math.toRadians(-15.0) + (float) Math.sin(renderState.ageInTicks * 0.15f) * 0.05f;

            // Occasional head tilt
            if (((int) renderState.ageInTicks % 60) < 15) {
                this.head.zRot = 0.15f;
            } else {
                this.head.zRot = 0.0f;
            }

            // Leg walking animation based on walkAnimationSpeed
            float walkSpeed = renderState.walkAnimationSpeed;
            float walkPos = renderState.walkAnimationPos;
            if (walkSpeed > 0.01f) {
                float legSwing = (float) Math.sin(walkPos * 0.6662f) * 1.4f * walkSpeed;
                this.leftLeg.xRot = legSwing;
                this.rightLeg.xRot = -legSwing;
            } else {
                this.leftLeg.xRot = 0.0f;
                this.rightLeg.xRot = 0.0f;
            }
        }
    }
}
