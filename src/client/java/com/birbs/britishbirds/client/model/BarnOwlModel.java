package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.BarnOwlRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Barn Owl model: ~0.7 blocks, heart-shaped facial disc, broad wings,
 * long legs, short tail. 32x32 texture.
 */
public class BarnOwlModel extends EntityModel<BarnOwlRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BarnOwlModel(ModelPart root) {
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

        // Body: barrel-shaped, 5x6x6
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5f, -3.0f, -3.0f, 5.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f));

        // Head: 4x4x4 with heart-shaped facial disc as a child
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 14.0f, -1.0f));

        // Heart-shaped facial disc: flattened plate on front of head
        headPart.addOrReplaceChild("facial_disc",
                CubeListBuilder.create()
                        .texOffs(16, 12)
                        .addBox(-2.5f, -3.5f, -3.0f, 5.0f, 4.0f, 1.0f),
                PartPose.ZERO);

        // Beak: small hook on front of head
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(16, 17)
                        .addBox(-0.5f, -1.5f, -3.5f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Left wing: broad, 1x5x7
        partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(0.0f, -2.0f, -3.0f, 1.0f, 5.0f, 7.0f),
                PartPose.offset(2.5f, 16.0f, 0.0f));

        // Right wing: broad, 1x5x7
        partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .mirror()
                        .addBox(-1.0f, -2.0f, -3.0f, 1.0f, 5.0f, 7.0f),
                PartPose.offset(-2.5f, 16.0f, 0.0f));

        // Tail: short, 3x1x2
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 17.0f, 3.0f,
                        (float) Math.toRadians(-10.0), 0.0f, 0.0f));

        // Left leg: longer than songbirds, 1x4x1
        partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(1.5f, 20.0f, 0.0f));

        // Right leg: longer than songbirds, 1x4x1
        partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(-1.5f, 20.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(BarnOwlRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isHovering) {
            // Hovering: rapid wing beats, legs dangling
            this.leftWing.zRot = -(float) Math.sin(renderState.ageInTicks * 2.0f) * 0.8f;
            this.rightWing.zRot = (float) Math.sin(renderState.ageInTicks * 2.0f) * 0.8f;
            this.leftLeg.xRot = 0.3f;
            this.rightLeg.xRot = 0.3f;
            this.body.xRot = -0.1f;
        } else if (renderState.isFlying) {
            // Flying: slow wing flaps, legs dangling back
            this.leftWing.zRot = -renderState.flapAngle;
            this.rightWing.zRot = renderState.flapAngle;
            this.leftLeg.xRot = 0.6f;
            this.rightLeg.xRot = 0.6f;
            this.body.xRot = -0.15f;
        } else {
            // Ground: upright perch posture
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.body.xRot = 0.0f;
            this.leftLeg.xRot = 0.0f;
            this.rightLeg.xRot = 0.0f;

            // Tail slight bob
            this.tail.xRot = (float) Math.toRadians(-10.0)
                    + (float) Math.sin(renderState.ageInTicks * 0.1f) * 0.03f;

            // Owl head rotation: wide turning
            if (((int) renderState.ageInTicks % 80) < 20) {
                this.head.yRot = 0.5f; // Look right
            } else if (((int) renderState.ageInTicks % 80) < 40) {
                this.head.yRot = -0.5f; // Look left
            } else {
                this.head.yRot = 0.0f;
            }
            this.head.xRot = 0.0f;
            this.head.zRot = 0.0f;

            // Leg walking animation
            float walkSpeed = renderState.walkAnimationSpeed;
            float walkPos = renderState.walkAnimationPos;
            if (walkSpeed > 0.01f) {
                float legSwing = (float) Math.sin(walkPos * 0.6662f) * 1.4f * walkSpeed;
                this.leftLeg.xRot = legSwing;
                this.rightLeg.xRot = -legSwing;
            }
        }
    }
}
