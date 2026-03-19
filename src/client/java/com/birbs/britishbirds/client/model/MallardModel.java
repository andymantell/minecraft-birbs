package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.MallardRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Mallard model: ~chicken sized, heavy rounded body, flat spatulate bill,
 * short legs set far back, webbed feet. 32x32 texture.
 * Drake tail curl only shown when male.
 */
public class MallardModel extends EntityModel<MallardRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart bill;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart tailCurl;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public MallardModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.bill = this.head.getChild("bill");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
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

        // Body: heavy rounded, 6x5x8
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0f, -2.5f, -4.0f, 6.0f, 5.0f, 8.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f));

        // Head: 3x3x3 on a neck
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 14.5f, -3.0f));

        // Bill: flat spatulate, wider than tall, 2x1x2
        headPart.addOrReplaceChild("bill",
                CubeListBuilder.create()
                        .texOffs(12, 13)
                        .addBox(-1.0f, -0.5f, -2.5f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(0.0f, -1.0f, -1.5f));

        // Left wing: 1x4x6
        partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(0.0f, -1.5f, -3.0f, 1.0f, 4.0f, 6.0f),
                PartPose.offset(3.0f, 16.0f, 0.0f));

        // Right wing: 1x4x6 (mirrored)
        partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .mirror()
                        .addBox(-1.0f, -1.5f, -3.0f, 1.0f, 4.0f, 6.0f),
                PartPose.offset(-3.0f, 16.0f, 0.0f));

        // Tail: 4x1x3
        PartDefinition tailPart = partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(20, 0)
                        .addBox(-2.0f, -0.5f, 0.0f, 4.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 16.5f, 4.0f,
                        (float) Math.toRadians(-10.0), 0.0f, 0.0f));

        // Drake tail curl: small curled cuboid, 1x1x2 (only visible for males)
        tailPart.addOrReplaceChild("tail_curl",
                CubeListBuilder.create()
                        .texOffs(20, 4)
                        .addBox(-0.5f, -1.0f, 1.0f, 1.0f, 1.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -0.5f, 1.5f,
                        (float) Math.toRadians(-30.0), 0.0f, 0.0f));

        // Left leg: short, set far back, 1x3x1
        PartDefinition leftLegPart = partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(14, 19)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(1.5f, 21.0f, 2.0f));

        // Left foot: webbed, wider, 2x0.5x2
        leftLegPart.addOrReplaceChild("left_foot",
                CubeListBuilder.create()
                        .texOffs(14, 23)
                        .addBox(-1.0f, 2.5f, -2.0f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Right leg: short, set far back, 1x3x1
        PartDefinition rightLegPart = partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(14, 19)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-1.5f, 21.0f, 2.0f));

        // Right foot: webbed, wider, 2x0.5x2
        rightLegPart.addOrReplaceChild("right_foot",
                CubeListBuilder.create()
                        .texOffs(14, 23)
                        .addBox(-1.0f, 2.5f, -2.0f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(MallardRenderState state) {
        super.setupAnim(state);

        // Drake tail curl: only visible for adult males
        this.tailCurl.visible = state.isMale && !state.isBaby;

        if (state.isDabbling) {
            // Dabbling: body pitches 90 degrees forward, tail points up
            this.body.xRot = (float) Math.toRadians(90.0);
            this.head.xRot = (float) Math.toRadians(90.0);
            this.tail.xRot = (float) Math.toRadians(-80.0);
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.leftLeg.xRot = 0.3f;
            this.rightLeg.xRot = 0.3f;
        } else if (state.isFlying) {
            // Flying: rapid stiff wingbeats, feet trailing behind
            this.leftWing.zRot = -state.flapAngle;
            this.rightWing.zRot = state.flapAngle;
            this.body.xRot = (float) Math.toRadians(-15.0);
            this.head.xRot = 0.0f;
            this.leftLeg.xRot = (float) Math.toRadians(60.0);
            this.rightLeg.xRot = (float) Math.toRadians(60.0);
            this.tail.xRot = (float) Math.toRadians(-5.0);
        } else if (state.isSwimming) {
            // Swimming: body level, legs paddle alternately, gentle bob
            this.body.xRot = 0.0f;
            this.head.xRot = 0.0f;
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.tail.xRot = (float) Math.toRadians(-10.0);

            // Legs paddle alternately below waterline
            float paddleSpeed = 1.5f;
            this.leftLeg.xRot = (float) Math.sin(state.ageInTicks * paddleSpeed) * 0.4f;
            this.rightLeg.xRot = (float) Math.sin(state.ageInTicks * paddleSpeed + Math.PI) * 0.4f;

            // Gentle floating bob
            float bob = (float) Math.sin(state.ageInTicks * 0.1f) * 0.02f;
            this.body.y = 17.0f + bob;
        } else {
            // Ground / idle
            this.body.xRot = 0.0f;
            this.head.xRot = 0.0f;
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.tail.xRot = (float) Math.toRadians(-10.0);
            this.body.y = 17.0f;

            // Walking/waddling animation
            float walkSpeed = state.walkAnimationSpeed;
            float walkPos = state.walkAnimationPos;
            if (walkSpeed > 0.01f) {
                float legSwing = (float) Math.sin(walkPos * 0.6662f) * 1.4f * walkSpeed;
                this.leftLeg.xRot = legSwing;
                this.rightLeg.xRot = -legSwing;

                // Waddle: body rolls side to side
                this.body.zRot = (float) Math.sin(walkPos * 0.6662f) * 0.15f * walkSpeed;
            } else {
                this.leftLeg.xRot = 0.0f;
                this.rightLeg.xRot = 0.0f;
                this.body.zRot = 0.0f;

                // Idle on water/ground: gentle bob
                float bob = (float) Math.sin(state.ageInTicks * 0.08f) * 0.01f;
                this.body.y = 17.0f + bob;
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
