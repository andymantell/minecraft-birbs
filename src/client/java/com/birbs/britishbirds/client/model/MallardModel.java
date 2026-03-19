package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.MallardRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Mallard model: heavy, hefty, robust — large for a dabbling duck.
 * Rounded head with smooth domed profile. Short thick neck.
 * Long body relative to overall size. Broad flat spatulate bill.
 * Short legs set far back on body. Webbed feet. Drake tail curl.
 * 64x64 texture. Higher fidelity with more cuboids.
 */
public class MallardModel extends EntityModel<MallardRenderState> {
    private final ModelPart body;
    private final ModelPart rearBody;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart bill;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final ModelPart tail;
    private final ModelPart tailCurl;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public MallardModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.rearBody = root.getChild("rear_body");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.bill = this.head.getChild("bill");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.tail = root.getChild("tail");
        this.tailCurl = this.tail.getChild("tail_curl");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftFoot = this.leftLeg.getChild("left_foot");
        this.rightFoot = this.rightLeg.getChild("right_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body: heavy rounded front section 7x6x6 — hefty and robust
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.5f, -3.0f, -3.0f, 7.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, 17.0f, -1.0f));

        // Rear body: extends the long body shape 5x5x5
        partDefinition.addOrReplaceChild("rear_body",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-2.5f, -2.5f, -1.0f, 5.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, 17.0f, 3.0f));

        // Neck: short thick 3x3x2
        partDefinition.addOrReplaceChild("neck",
                CubeListBuilder.create()
                        .texOffs(20, 12)
                        .addBox(-1.5f, -2.0f, -1.0f, 3.0f, 3.0f, 2.0f),
                PartPose.offset(0.0f, 15.0f, -3.5f));

        // Head: rounded with smooth domed profile 4x4x4
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 13.0f, -3.5f));

        // Crown dome: rounds out the head shape 3x1x3
        headPart.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(16, 22)
                        .addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // Bill: broad flat spatulate — the classic duck bill 3x1x3 (wider than tall!)
        headPart.addOrReplaceChild("bill",
                CubeListBuilder.create()
                        .texOffs(16, 26)
                        .addBox(-1.5f, -1.5f, -5.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Bill tip: slightly wider and flatter 3x1x1
        headPart.addOrReplaceChild("bill_tip",
                CubeListBuilder.create()
                        .texOffs(28, 26)
                        .addBox(-1.5f, -1.5f, -6.0f, 3.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // Left wing: 1x7x8 — pivots from shoulder (y=0 = top)
        PartDefinition leftWingPart = partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 30)
                        .addBox(0.0f, 0.0f, -4.0f, 1.0f, 7.0f, 8.0f),
                PartPose.offset(3.5f, 13.0f, 0.0f));

        // Left wing tip: speculum area 1x4x4
        leftWingPart.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create()
                        .texOffs(18, 30)
                        .addBox(0.5f, 1.5f, 2.0f, 1.0f, 4.0f, 4.0f),
                PartPose.ZERO);

        // Right wing: 1x7x8 (mirrored) — pivots from shoulder
        PartDefinition rightWingPart = partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 30)
                        .mirror()
                        .addBox(-1.0f, 0.0f, -4.0f, 1.0f, 7.0f, 8.0f),
                PartPose.offset(-3.5f, 13.0f, 0.0f));

        // Right wing tip
        rightWingPart.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create()
                        .texOffs(18, 30)
                        .mirror()
                        .addBox(-1.5f, 1.5f, 2.0f, 1.0f, 4.0f, 4.0f),
                PartPose.ZERO);

        // Tail: 5x1x4
        PartDefinition tailPart = partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(26, 0)
                        .addBox(-2.5f, -0.5f, 0.0f, 5.0f, 1.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 16.5f, 6.5f,
                        (float) Math.toRadians(-10.0), 0.0f, 0.0f));

        // Drake tail curl: small curled feathers 1x1x2 (only visible for males)
        tailPart.addOrReplaceChild("tail_curl",
                CubeListBuilder.create()
                        .texOffs(26, 5)
                        .addBox(-0.5f, -1.0f, 2.0f, 1.0f, 1.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -0.5f, 1.5f,
                        (float) Math.toRadians(-30.0), 0.0f, 0.0f));

        // Left leg: set far back 1x4x1, pivot inside body so leg connects
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(26, 8)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(1.5f, 20.0f, 3.0f));

        // Left foot: webbed, wide 3x1x3
        leftLegPart.addOrReplaceChild("left_foot",
                CubeListBuilder.create()
                        .texOffs(30, 8)
                        .addBox(-1.5f, 3.5f, -2.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // Right leg: set far back 1x4x1, pivot inside body so leg connects
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(26, 8)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(-1.5f, 20.0f, 3.0f));

        // Right foot: webbed, wide 3x1x3
        rightLegPart.addOrReplaceChild("right_foot",
                CubeListBuilder.create()
                        .texOffs(30, 8)
                        .addBox(-1.5f, 3.5f, -2.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(MallardRenderState state) {
        super.setupAnim(state);

        // Drake tail curl: only visible for adult males
        this.tailCurl.visible = state.isMale && !state.isBaby;

        if (state.isDabbling) {
            // Dabbling: body pitches 90 degrees forward, tail points up
            this.body.xRot = (float) Math.toRadians(90.0);
            this.rearBody.xRot = (float) Math.toRadians(90.0);
            this.neck.xRot = (float) Math.toRadians(90.0);
            this.head.xRot = (float) Math.toRadians(90.0);
            this.tail.xRot = (float) Math.toRadians(-80.0);
            BirdAnimations.foldWings(this.leftWing, this.rightWing);
            this.leftLeg.xRot = 0.3f;
            this.rightLeg.xRot = 0.3f;
        } else if (state.isFlying) {
            // Flying: body horizontal, neck extended forward, rapid stiff wingbeats
            this.body.xRot = (float) Math.toRadians(-40.0);
            this.rearBody.xRot = (float) Math.toRadians(-40.0);
            this.neck.xRot = (float) Math.toRadians(20.0);
            this.head.xRot = (float) Math.toRadians(15.0);
            BirdAnimations.animateWingFlap(this.leftWing, this.rightWing, state.flapAngle);
            BirdAnimations.tuckLegs(this.leftLeg, this.rightLeg, (float) Math.toRadians(70.0));
            this.tail.xRot = (float) Math.toRadians(-15.0);
        } else if (state.isSwimming) {
            // Swimming: body level, legs paddle alternately, gentle bob
            this.body.xRot = 0.0f;
            this.rearBody.xRot = 0.0f;
            this.neck.xRot = 0.0f;
            this.head.xRot = 0.0f;
            BirdAnimations.foldWings(this.leftWing, this.rightWing);
            this.tail.xRot = (float) Math.toRadians(-10.0);

            // Legs paddle alternately below waterline
            float paddleSpeed = 1.5f;
            this.leftLeg.xRot = (float) Math.sin(state.ageInTicks * paddleSpeed) * 0.4f;
            this.rightLeg.xRot = (float) Math.sin(state.ageInTicks * paddleSpeed + Math.PI) * 0.4f;

            // Gentle floating bob
            float bob = (float) Math.sin(state.ageInTicks * 0.1f) * 0.02f;
            this.body.y = 17.0f + bob;
            this.rearBody.y = 17.0f + bob;
        } else {
            // Ground / idle
            this.body.xRot = 0.0f;
            this.rearBody.xRot = 0.0f;
            this.neck.xRot = 0.0f;
            this.head.xRot = 0.0f;
            BirdAnimations.foldWings(this.leftWing, this.rightWing);
            this.tail.xRot = (float) Math.toRadians(-10.0);
            this.body.y = 17.0f;
            this.rearBody.y = 17.0f;

            // Walking/waddling animation
            float walkSpeed = state.walkAnimationSpeed;
            float walkPos = state.walkAnimationPos;
            if (walkSpeed > 0.01f) {
                BirdAnimations.animateWalkingLegs(this.leftLeg, this.rightLeg, walkSpeed, walkPos);

                // Waddle: body rolls side to side
                this.body.zRot = (float) Math.sin(walkPos * 0.6662f) * 0.15f * walkSpeed;
                this.rearBody.zRot = this.body.zRot;
            } else {
                this.leftLeg.xRot = 0.0f;
                this.rightLeg.xRot = 0.0f;
                this.body.zRot = 0.0f;
                this.rearBody.zRot = 0.0f;

                // Idle on water/ground: gentle bob
                float bob = (float) Math.sin(state.ageInTicks * 0.08f) * 0.01f;
                this.body.y = 17.0f + bob;
                this.rearBody.y = 17.0f + bob;
            }

            // Head look-around
            if (((int) state.ageInTicks % 100) < 25) {
                this.head.yRot = 0.3f;
            } else if (((int) state.ageInTicks % 100) < 50) {
                this.head.yRot = -0.3f;
            } else {
                this.head.yRot = 0.0f;
            }
        }
    }
}
