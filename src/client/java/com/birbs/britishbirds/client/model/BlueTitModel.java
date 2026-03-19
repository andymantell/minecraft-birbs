package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.BlueTitRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Blue Tit model: small, round, compact — almost spherical when fluffed.
 * Proportionally very large head for body size ("big-headed").
 * Very small short fine conical bill. Short legs for clinging.
 * 64x64 texture. Higher fidelity with more cuboids.
 */
public class BlueTitModel extends EntityModel<BlueTitRenderState> {
    private final ModelPart body;
    private final ModelPart belly;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BlueTitModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.belly = root.getChild("belly");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body: small compact sphere 4x4x4 — nearly spherical
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));

        // Belly: rounded underbelly bulge 3x2x3 for spherical look
        partDefinition.addOrReplaceChild("belly",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.5f, 0.5f, -1.5f, 3.0f, 2.0f, 3.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));

        // Head: proportionally VERY large — 4x4x4 (same size as body!)
        // This is the defining "big-headed" look of the blue tit
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 18.0f, -0.5f));

        // Crown: raised cap area 3x1x3 for the blue cap
        headPart.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(16, 13)
                        .addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // Beak: very small, short, fine conical bill 1x1x1
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(16, 17)
                        .addBox(-0.5f, -2.0f, -3.0f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Cheek patches: slight bulge on sides of head for the white cheeks 1x2x2
        headPart.addOrReplaceChild("left_cheek",
                CubeListBuilder.create()
                        .texOffs(20, 17)
                        .addBox(1.5f, -3.0f, -1.5f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);

        headPart.addOrReplaceChild("right_cheek",
                CubeListBuilder.create()
                        .texOffs(20, 17)
                        .mirror()
                        .addBox(-2.5f, -3.0f, -1.5f, 1.0f, 2.0f, 2.0f),
                PartPose.ZERO);

        // Left wing: 1x4x4 — pivots from shoulder (y=0 = top)
        PartDefinition leftWingPart = partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(0.0f, 0.0f, -2.0f, 1.0f, 4.0f, 4.0f),
                PartPose.offset(2.0f, 18.0f, 0.0f));

        // Left wing tip 1x3x2
        leftWingPart.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create()
                        .texOffs(26, 0)
                        .addBox(0.5f, 0.5f, 0.5f, 1.0f, 3.0f, 2.0f),
                PartPose.ZERO);

        // Right wing: 1x4x4 (mirrored) — pivots from shoulder
        PartDefinition rightWingPart = partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .mirror()
                        .addBox(-1.0f, 0.0f, -2.0f, 1.0f, 4.0f, 4.0f),
                PartPose.offset(-2.0f, 18.0f, 0.0f));

        // Right wing tip
        rightWingPart.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create()
                        .texOffs(26, 0)
                        .mirror()
                        .addBox(-1.5f, 0.5f, 0.5f, 1.0f, 3.0f, 2.0f),
                PartPose.ZERO);

        // Tail: moderately short, slightly notched 3x1x3
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 21)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 20.0f, 2.0f,
                        (float) Math.toRadians(-15.0), 0.0f, 0.0f));

        // Left leg: short 1x2x1
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.75f, 22.0f, 0.0f));

        // Left foot: gripping toes 2x0.5x1
        leftLegPart.addOrReplaceChild("left_foot",
                CubeListBuilder.create()
                        .texOffs(4, 25)
                        .addBox(-0.5f, 1.5f, -1.0f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Right leg: short 1x2x1
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-0.75f, 22.0f, 0.0f));

        // Right foot
        rightLegPart.addOrReplaceChild("right_foot",
                CubeListBuilder.create()
                        .texOffs(4, 25)
                        .addBox(-0.5f, 1.5f, -1.0f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(BlueTitRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isHangingUpsideDown) {
            // Upside-down clinging pose
            this.body.xRot = (float) Math.PI; // 180 degrees
            this.belly.xRot = (float) Math.PI;
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
            // Flying: body well forward, rapid whirring wingbeats
            this.body.xRot = -0.6f;
            this.belly.xRot = -0.6f;
            this.head.xRot = 0.35f;
            this.head.zRot = 0.0f;

            BirdAnimations.animateWingFlap(this.leftWing, this.rightWing, renderState.flapAngle);

            BirdAnimations.tuckLegs(this.leftLeg, this.rightLeg, 0.5f);

            // Tail extends back
            this.tail.xRot = -0.4f;
        } else {
            // Ground animations
            BirdAnimations.foldWings(this.leftWing, this.rightWing);
            this.body.xRot = 0.0f;
            this.belly.xRot = 0.0f;

            BirdAnimations.animateTailBob(this.tail, (float) Math.toRadians(-15.0),
                    renderState.ageInTicks, 0.15f, 0.05f);

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

            BirdAnimations.animateWalkingLegs(this.leftLeg, this.rightLeg,
                    renderState.walkAnimationSpeed, renderState.walkAnimationPos);
        }
    }
}
