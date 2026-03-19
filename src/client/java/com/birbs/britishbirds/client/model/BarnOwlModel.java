package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.BarnOwlRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Barn Owl model: heart-shaped facial disc, slender elongated body (not round),
 * notably long legs, broad rounded wings, short square-ended tail, large rounded head.
 * Perched: tall and slim silhouette, upright forward-leaning posture.
 * 64x64 texture. Higher fidelity with more cuboids.
 */
public class BarnOwlModel extends EntityModel<BarnOwlRenderState> {
    private final ModelPart body;
    private final ModelPart lowerBody;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingOuter;
    private final ModelPart rightWingOuter;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BarnOwlModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.lowerBody = root.getChild("lower_body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftWingOuter = this.leftWing.getChild("left_wing_outer");
        this.rightWingOuter = this.rightWing.getChild("right_wing_outer");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body: slender and elongated upper torso 5x5x5 (narrower than before)
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5f, -2.5f, -2.5f, 5.0f, 5.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 15.5f, 0.0f,
                        (float) Math.toRadians(5.0), 0.0f, 0.0f));

        // Lower body: narrower extension 4x3x4 — creates the slim elongated shape
        partDefinition.addOrReplaceChild("lower_body",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0f, -0.5f, -2.0f, 4.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 18.0f, 0.0f));

        // Head: large rounded 5x5x5 with heart-shaped facial disc
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, 13.0f, -0.5f));

        // Heart-shaped facial disc: the defining feature — broader plate on front 6x5x1
        headPart.addOrReplaceChild("facial_disc",
                CubeListBuilder.create()
                        .texOffs(20, 17)
                        .addBox(-3.0f, -4.5f, -3.5f, 6.0f, 5.0f, 1.0f),
                PartPose.ZERO);

        // Beak: small hooked beak in center of facial disc 1x1x1
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(20, 23)
                        .addBox(-0.5f, -2.0f, -4.0f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Left wing: broad rounded 1x6x8 (large relative to body mass)
        PartDefinition leftWingPart = partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(0.0f, -2.5f, -3.5f, 1.0f, 6.0f, 7.0f),
                PartPose.offset(2.5f, 14.5f, 0.0f));

        // Left wing outer: extends the wing broader 1x5x4
        leftWingPart.addOrReplaceChild("left_wing_outer",
                CubeListBuilder.create()
                        .texOffs(16, 27)
                        .addBox(1.0f, -1.5f, -2.5f, 1.0f, 5.0f, 4.0f),
                PartPose.ZERO);

        // Right wing: broad rounded 1x6x7 (mirrored)
        PartDefinition rightWingPart = partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .mirror()
                        .addBox(-1.0f, -2.5f, -3.5f, 1.0f, 6.0f, 7.0f),
                PartPose.offset(-2.5f, 14.5f, 0.0f));

        // Right wing outer
        rightWingPart.addOrReplaceChild("right_wing_outer",
                CubeListBuilder.create()
                        .texOffs(16, 27)
                        .mirror()
                        .addBox(-2.0f, -1.5f, -2.5f, 1.0f, 5.0f, 4.0f),
                PartPose.ZERO);

        // Tail: short square-ended 4x1x3
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .addBox(-2.0f, -0.5f, 0.0f, 4.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 17.5f, 2.5f,
                        (float) Math.toRadians(-10.0), 0.0f, 0.0f));

        // Left leg: notably LONG 1x5x1 (adaptation for striking into grass)
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offset(1.5f, 19.0f, 0.0f));

        // Left talon: 2x1x2
        leftLegPart.addOrReplaceChild("left_talon",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-1.0f, 4.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Right leg: notably LONG 1x5x1
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offset(-1.5f, 19.0f, 0.0f));

        // Right talon
        rightLegPart.addOrReplaceChild("right_talon",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-1.0f, 4.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
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
            this.lowerBody.xRot = -0.1f;
        } else if (renderState.isFlying) {
            // Flying: slow wing flaps, legs dangling back
            this.leftWing.zRot = -renderState.flapAngle;
            this.rightWing.zRot = renderState.flapAngle;
            this.leftLeg.xRot = 0.6f;
            this.rightLeg.xRot = 0.6f;
            this.body.xRot = -0.15f;
            this.lowerBody.xRot = -0.15f;
        } else {
            // Ground: upright perch posture — tall and slim
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.body.xRot = (float) Math.toRadians(5.0);
            this.lowerBody.xRot = 0.0f;
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
