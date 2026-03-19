package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.BlueTitRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BlueTitModel extends EntityModel<BlueTitRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BlueTitModel(ModelPart root) {
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

        // Body: smaller than Robin — 3x4x4 (~0.25 blocks)
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5f, -2.0f, -2.0f, 3.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));

        // Head: proportionally larger — 3x3x3 (same as Robin despite smaller body)
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 18.0f, -0.5f));

        // Beak: small protrusion
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(12, 8)
                        .addBox(-0.5f, -1.5f, -2.5f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Left wing
        partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(14, 0)
                        .addBox(0.0f, -1.0f, -1.5f, 1.0f, 3.0f, 3.0f),
                PartPose.offset(1.5f, 19.5f, 0.0f));

        // Right wing
        partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(14, 0)
                        .mirror()
                        .addBox(-1.0f, -1.0f, -1.5f, 1.0f, 3.0f, 3.0f),
                PartPose.offset(-1.5f, 19.5f, 0.0f));

        // Tail: short
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 20.0f, 2.0f,
                        (float) Math.toRadians(-15.0), 0.0f, 0.0f));

        // Left leg
        partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-0.5f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.75f, 22.0f, 0.0f));

        // Right leg
        partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-0.5f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-0.75f, 22.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(BlueTitRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isHangingUpsideDown) {
            // Upside-down clinging pose
            this.body.xRot = (float) Math.PI; // 180 degrees
            this.head.xRot = (float) Math.PI; // head upside-down too
            // Legs reach up to grip
            this.leftLeg.xRot = (float) Math.PI;
            this.rightLeg.xRot = (float) Math.PI;
            // Wings slightly spread for balance
            this.leftWing.zRot = -0.2f;
            this.rightWing.zRot = 0.2f;
            // Tail hangs down
            this.tail.xRot = (float) Math.PI + (float) Math.toRadians(15.0);
        } else if (renderState.isFlying) {
            // Flying animations
            this.leftWing.zRot = -renderState.flapAngle;
            this.rightWing.zRot = renderState.flapAngle;

            // Tuck legs up
            this.leftLeg.xRot = 0.5f;
            this.rightLeg.xRot = 0.5f;

            // Tilt body forward
            this.body.xRot = -0.2f;
        } else {
            // Ground animations
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.body.xRot = 0.0f;

            // Gentle tail bob
            this.tail.xRot = (float) Math.toRadians(-15.0) + (float) Math.sin(renderState.ageInTicks * 0.15f) * 0.05f;

            // Pecking animation
            if (renderState.isPecking) {
                this.head.xRot = 0.8f;
                this.head.zRot = 0.0f;
            } else if (((int) renderState.ageInTicks % 60) < 15) {
                this.head.xRot = 0.0f;
                this.head.zRot = 0.15f;
            } else {
                this.head.xRot = 0.0f;
                this.head.zRot = 0.0f;
            }

            // Leg walking animation
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
