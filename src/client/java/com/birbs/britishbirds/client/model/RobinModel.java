package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.RobinRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Robin model: round plump "puffball" body, large rounded head blending into body,
 * short fine pointed beak, short tail slightly cocked upward, long slender legs.
 * 64x64 texture. Higher fidelity with more cuboids for smoother shapes.
 */
public class RobinModel extends EntityModel<RobinRenderState> {
    private final ModelPart body;
    private final ModelPart breast;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public RobinModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.breast = root.getChild("breast");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body: plump round upper body 5x5x5 — the main round "puffball"
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5f, -2.5f, -2.5f, 5.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, 19.0f, 0.0f));

        // Breast: rounded protrusion on the front-bottom, gives the plump look
        // 4x3x3, overlapping slightly with body at front
        partDefinition.addOrReplaceChild("breast",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0f, -1.0f, -3.5f, 4.0f, 3.0f, 2.0f),
                PartPose.offset(0.0f, 19.5f, 0.0f));

        // Head: 4x4x4 large rounded head that blends into body (no visible neck)
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 16.5f, -1.0f));

        // Beak: small fine pointed insectivore beak 1x1x2
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(16, 15)
                        .addBox(-0.5f, -2.0f, -4.0f, 1.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Crown: slightly raised area on top of head for rounder profile 3x1x3
        headPart.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(22, 15)
                        .addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // Left wing: short rounded, 1x4x5 (21cm wingspan on 13cm body)
        PartDefinition leftWingPart = partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .addBox(0.0f, -2.0f, -2.5f, 1.0f, 4.0f, 5.0f),
                PartPose.offset(2.5f, 18.5f, 0.0f));

        // Left wing tip: rounded end 1x3x3
        leftWingPart.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(0.5f, -1.0f, 1.0f, 1.0f, 3.0f, 3.0f),
                PartPose.ZERO);

        // Right wing: short rounded, 1x4x5 (mirrored)
        PartDefinition rightWingPart = partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .mirror()
                        .addBox(-1.0f, -2.0f, -2.5f, 1.0f, 4.0f, 5.0f),
                PartPose.offset(-2.5f, 18.5f, 0.0f));

        // Right wing tip
        rightWingPart.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .mirror()
                        .addBox(-1.5f, -1.0f, 1.0f, 1.0f, 3.0f, 3.0f),
                PartPose.ZERO);

        // Tail: short, slightly cocked upward at -15 degrees, 2x1x3
        PartDefinition tailPart = partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 23)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 18.5f, 2.5f,
                        (float) Math.toRadians(-15.0), 0.0f, 0.0f));

        // Tail tip: narrower end 2x1x2
        tailPart.addOrReplaceChild("tail_tip",
                CubeListBuilder.create()
                        .texOffs(12, 23)
                        .addBox(-1.0f, -0.5f, 2.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Left leg: long and slender 1x4x1 (longer than you'd expect for plump body)
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(1.0f, 20.0f, 0.0f));

        // Left foot: 2x0.5x2
        leftLegPart.addOrReplaceChild("left_foot",
                CubeListBuilder.create()
                        .texOffs(4, 27)
                        .addBox(-1.0f, 3.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Right leg: long and slender 1x4x1
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(-1.0f, 20.0f, 0.0f));

        // Right foot: 2x0.5x2
        rightLegPart.addOrReplaceChild("right_foot",
                CubeListBuilder.create()
                        .texOffs(4, 27)
                        .addBox(-1.0f, 3.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(RobinRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isFlying) {
            // Flying: body tilts well forward, rapid fluttering
            this.body.xRot = -0.6f;
            this.breast.xRot = -0.6f;
            this.head.xRot = 0.35f;
            this.head.zRot = 0.0f;

            // Wing flapping
            BirdAnimations.animateWingFlap(this.leftWing, this.rightWing, renderState.flapAngle);

            BirdAnimations.tuckLegs(this.leftLeg, this.rightLeg, 0.5f);

            // Tail extends back
            this.tail.xRot = -0.4f;
        } else {
            // Ground animations
            BirdAnimations.foldWings(this.leftWing, this.rightWing);

            // Reset body tilt
            this.body.xRot = 0.0f;
            this.breast.xRot = 0.0f;

            BirdAnimations.animateTailBob(this.tail, (float) Math.toRadians(-15.0),
                    renderState.ageInTicks, 0.15f, 0.05f);

            // Pecking animation: head dips down
            if (renderState.isPecking) {
                this.head.xRot = 0.8f; // Tilt head downward for pecking
                this.head.zRot = 0.0f;
            } else if (((int) renderState.ageInTicks % 60) < 15) {
                // Occasional head tilt
                this.head.xRot = 0.0f;
                this.head.zRot = 0.15f;
            } else {
                this.head.xRot = 0.0f;
                this.head.zRot = 0.0f;
            }

            BirdAnimations.animateWalkingLegs(this.leftLeg, this.rightLeg,
                    renderState.walkAnimationSpeed, renderState.walkAnimationPos);
        }
    }
}
