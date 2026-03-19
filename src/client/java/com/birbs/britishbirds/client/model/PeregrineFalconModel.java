package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.PeregrineFalconRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Peregrine Falcon model: medium-large, compact torpedo shape.
 * Broad chest, narrow tail, long pointed wings. 32x32 texture.
 */
public class PeregrineFalconModel extends EntityModel<PeregrineFalconRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public PeregrineFalconModel(ModelPart root) {
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

        // Body: torpedo-shaped, broad chest narrowing to tail, 5x5x7
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5f, -2.5f, -3.5f, 5.0f, 5.0f, 7.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f));

        // Head: compact, 3x3x3 with dark helmet/moustachial stripe
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 14.5f, -2.5f));

        // Beak: hooked raptor beak
        headPart.addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(12, 12)
                        .addBox(-0.5f, -1.5f, -3.0f, 1.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // Left wing: long and pointed, 1x4x8
        partDefinition.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(0.0f, -1.0f, -3.0f, 1.0f, 4.0f, 8.0f),
                PartPose.offset(2.5f, 16.0f, 0.0f));

        // Right wing: long and pointed, 1x4x8
        partDefinition.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .mirror()
                        .addBox(-1.0f, -1.0f, -3.0f, 1.0f, 4.0f, 8.0f),
                PartPose.offset(-2.5f, 16.0f, 0.0f));

        // Tail: narrow, 3x1x3
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 17.0f, 3.5f,
                        (float) Math.toRadians(-5.0), 0.0f, 0.0f));

        // Left leg: 1x3x1
        partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(1.5f, 21.0f, 0.0f));

        // Right leg: 1x3x1
        partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-1.5f, 21.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(PeregrineFalconRenderState renderState) {
        super.setupAnim(renderState);

        if (renderState.isStooping) {
            // Stooping: wings completely folded against body, body angled downward, teardrop shape
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            // Fold wings tight against body
            this.leftWing.xRot = (float) Math.toRadians(80.0);
            this.rightWing.xRot = (float) Math.toRadians(80.0);
            // Scale wings very small to appear folded (hide them via rotation)
            this.leftWing.zRot = -0.05f;
            this.rightWing.zRot = 0.05f;
            // Body angled steeply downward
            this.body.xRot = (float) Math.toRadians(60.0);
            this.head.xRot = (float) Math.toRadians(-30.0); // Head looks forward, compensating body tilt
            // Tail folded tight
            this.tail.xRot = (float) Math.toRadians(50.0);
            // Legs tucked
            this.leftLeg.xRot = (float) Math.toRadians(60.0);
            this.rightLeg.xRot = (float) Math.toRadians(60.0);
        } else if (renderState.isFlying) {
            // Active flight: stiff shallow wingbeats
            this.leftWing.zRot = -renderState.flapAngle * 0.6f;
            this.rightWing.zRot = renderState.flapAngle * 0.6f;
            this.leftWing.xRot = 0.0f;
            this.rightWing.xRot = 0.0f;
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            this.body.xRot = -0.1f;
            this.head.xRot = 0.0f;
            this.tail.xRot = (float) Math.toRadians(-5.0);
            // Legs tucked back
            this.leftLeg.xRot = 0.7f;
            this.rightLeg.xRot = 0.7f;

            // Soaring detection: if flapAngle is very small, wings spread wide and flat
            if (Math.abs(renderState.flapAngle) < 0.1f) {
                // Soaring: wings spread wide, tail fanned
                this.leftWing.zRot = -1.2f;
                this.rightWing.zRot = 1.2f;
                this.tail.xRot = (float) Math.toRadians(-15.0);
            }
        } else {
            // Perched: upright bolt posture
            this.leftWing.zRot = 0.0f;
            this.rightWing.zRot = 0.0f;
            this.leftWing.xRot = 0.0f;
            this.rightWing.xRot = 0.0f;
            this.leftWing.yRot = 0.0f;
            this.rightWing.yRot = 0.0f;
            this.body.xRot = (float) Math.toRadians(15.0); // Slightly upright posture
            this.head.xRot = 0.0f;
            this.head.yRot = 0.0f;
            this.tail.xRot = (float) Math.toRadians(10.0);
            this.leftLeg.xRot = 0.0f;
            this.rightLeg.xRot = 0.0f;

            // Alert head turns
            if (((int) renderState.ageInTicks % 120) < 30) {
                this.head.yRot = 0.4f;
            } else if (((int) renderState.ageInTicks % 120) < 60) {
                this.head.yRot = -0.4f;
            } else {
                this.head.yRot = 0.0f;
            }

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
