package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.PeregrineFalconRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Peregrine Falcon model: COMPLETE REMODEL.
 * Compact, powerful, broad-chested muscular build.
 * Torpedo-shaped / streamlined — body TAPERS from broad shoulders to narrow tail.
 * V-shaped contour — broad at shoulders, narrowing to tail tip.
 * Long, pointed, narrow wings — sickle-shaped in active flight.
 * Head appears relatively small compared to broad chest.
 * Perched: bolt-upright and alert posture.
 * 64x64 texture.
 */
public class PeregrineFalconModel extends EntityModel<PeregrineFalconRenderState> {
    private final ModelPart chest;
    private final ModelPart rearBody;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingOuter;
    private final ModelPart rightWingOuter;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public PeregrineFalconModel(ModelPart root) {
        super(root);
        this.chest = root.getChild("chest");
        this.rearBody = root.getChild("rear_body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftWingOuter = this.leftWing.getChild("left_wing_outer");
        this.rightWingOuter = this.rightWing.getChild("right_wing_outer");
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // CHEST: broad, powerful, muscular — the widest part of the V-contour
        // 6x5x5 — wide side-to-side, narrow front-to-back
        partDefinition.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0f, -2.5f, -2.5f, 6.0f, 5.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 16.0f, -1.0f,
                        (float) Math.toRadians(10.0), 0.0f, 0.0f));

        // REAR BODY: narrows significantly — creates the V-taper / torpedo shape
        // 4x4x5 — narrower than chest, extends behind
        partDefinition.addOrReplaceChild("rear_body",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0f, -2.0f, -1.0f, 4.0f, 4.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 16.5f, 2.0f,
                        (float) Math.toRadians(5.0), 0.0f, 0.0f));

        // HEAD: relatively SMALL compared to broad chest — 3x3x3
        // This is key to getting rid of the "duck" look
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 13.5f, -2.5f));

        // Hooked beak: short powerful with tomial tooth 1x1x2
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(12, 19)
                        .addBox(-0.5f, -1.5f, -3.5f, 1.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Beak hook: the tomial tooth curve 1x1x1 angled down
        headPart.addOrReplaceChild("beak_hook",
                CubeListBuilder.create()
                        .texOffs(18, 19)
                        .addBox(-0.5f, -0.5f, -3.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -1.0f, 0.0f,
                        (float) Math.toRadians(15.0), 0.0f, 0.0f));

        // Dark helmet/malar stripe: slight widening on sides of head 1x2x2 each side
        headPart.addOrReplaceChild("left_malar",
                CubeListBuilder.create()
                        .texOffs(22, 19)
                        .addBox(1.0f, -2.5f, -1.5f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);

        headPart.addOrReplaceChild("right_malar",
                CubeListBuilder.create()
                        .texOffs(22, 19)
                        .mirror()
                        .addBox(-2.0f, -2.5f, -1.5f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);

        // LEFT WING: LONG, NARROW, POINTED — sickle-shaped (100cm wingspan on 42cm body)
        // Inner wing: 1x6x7
        PartDefinition leftWingPart = partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(0.0f, -2.5f, -3.5f, 1.0f, 6.0f, 7.0f),
                PartPose.offset(3.0f, 15.0f, 0.0f));

        // Left wing outer: extends the wing long and narrow — pointed tip 1x5x5
        leftWingPart.addOrReplaceChild("left_wing_outer",
                CubeListBuilder.create()
                        .texOffs(16, 25)
                        .addBox(1.0f, -1.5f, -2.0f, 1.0f, 5.0f, 5.0f),
                PartPose.ZERO);

        // RIGHT WING: LONG, NARROW, POINTED (mirrored)
        PartDefinition rightWingPart = partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .mirror()
                        .addBox(-1.0f, -2.5f, -3.5f, 1.0f, 6.0f, 7.0f),
                PartPose.offset(-3.0f, 15.0f, 0.0f));

        // Right wing outer
        rightWingPart.addOrReplaceChild("right_wing_outer",
                CubeListBuilder.create()
                        .texOffs(16, 25)
                        .mirror()
                        .addBox(-2.0f, -1.5f, -2.0f, 1.0f, 5.0f, 5.0f),
                PartPose.ZERO);

        // TAIL: medium-long, narrow — continues the taper 3x1x4
        PartDefinition tailPart = partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 16.5f, 5.5f,
                        (float) Math.toRadians(-5.0), 0.0f, 0.0f));

        // Tail tip: even narrower end 2x1x3
        tailPart.addOrReplaceChild("tail_tip",
                CubeListBuilder.create()
                        .texOffs(22, 5)
                        .addBox(-1.0f, -0.5f, 3.5f, 2.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // LEFT LEG: 1x5x1, pivot inside body so leg visually connects
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offset(1.5f, 19.0f, 0.0f));

        // Left talon: large powerful feet 2x1x2 (yellow in texture)
        leftLegPart.addOrReplaceChild("left_talon",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-1.0f, 4.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // RIGHT LEG: 1x5x1, pivot inside body so leg visually connects
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f),
                PartPose.offset(-1.5f, 19.0f, 0.0f));

        // Right talon
        rightLegPart.addOrReplaceChild("right_talon",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-1.0f, 4.5f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(PeregrineFalconRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isStooping) {
            // Stooping: wings completely folded against body, body angled downward, teardrop shape
            this.leftWing.zRot = -0.05f;
            this.rightWing.zRot = 0.05f;
            this.leftWing.xRot = (float) Math.toRadians(80.0);
            this.rightWing.xRot = (float) Math.toRadians(80.0);
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            // Body angled steeply downward
            this.chest.xRot = (float) Math.toRadians(60.0);
            this.rearBody.xRot = (float) Math.toRadians(55.0);
            this.head.xRot = (float) Math.toRadians(-30.0);
            // Tail folded tight
            this.tail.xRot = (float) Math.toRadians(50.0);
            // Legs tucked
            this.leftLeg.xRot = (float) Math.toRadians(60.0);
            this.rightLeg.xRot = (float) Math.toRadians(60.0);
        } else if (renderState.isFlying) {
            // Active flight: body streamlined horizontal, stiff shallow wingbeats
            this.chest.xRot = -0.6f;
            this.rearBody.xRot = -0.6f;
            this.head.xRot = 0.35f;
            BirdAnimations.animateWingFlap(this.leftWing, this.rightWing, renderState.flapAngle, 0.5f);
            this.leftWing.xRot = 0.0f;
            this.rightWing.xRot = 0.0f;
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            this.tail.xRot = -0.3f;
            BirdAnimations.tuckLegs(this.leftLeg, this.rightLeg, 0.8f);

            // Soaring detection: if flapAngle is very small, wings spread wide and flat
            if (Math.abs(renderState.flapAngle) < 0.1f) {
                // Soaring: wings spread very wide, tail fanned
                this.leftWing.zRot = -1.4f;
                this.rightWing.zRot = 1.4f;
                this.tail.xRot = (float) Math.toRadians(-15.0);
            }
        } else {
            // Perched: BOLT-UPRIGHT and alert — very different from duck posture
            BirdAnimations.foldWings(this.leftWing, this.rightWing);
            this.leftWing.xRot = 0.0f;
            this.rightWing.xRot = 0.0f;
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            // Upright posture — body tilted more vertical
            this.chest.xRot = (float) Math.toRadians(20.0);
            this.rearBody.xRot = (float) Math.toRadians(15.0);
            this.head.xRot = (float) Math.toRadians(-10.0); // compensate, look forward
            this.head.yRot = 0.0f;
            this.tail.xRot = (float) Math.toRadians(15.0);
            this.leftLeg.xRot = 0.0f;
            this.rightLeg.xRot = 0.0f;

            // Alert head turns — sharp, quick movements
            if (((int) renderState.ageInTicks % 120) < 30) {
                this.head.yRot = 0.4f;
            } else if (((int) renderState.ageInTicks % 120) < 60) {
                this.head.yRot = -0.4f;
            } else {
                this.head.yRot = 0.0f;
            }

            BirdAnimations.animateWalkingLegs(this.leftLeg, this.rightLeg,
                    renderState.walkAnimationSpeed, renderState.walkAnimationPos);
        }
    }
}
