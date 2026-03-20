package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.animation.SkeletonModelMapper;
import com.birbs.britishbirds.client.animation.pose.BaseBirdPoses;
import com.birbs.britishbirds.client.animation.pose.CyclicAnimation;
import com.birbs.britishbirds.client.animation.pose.PasserinePoses;
import com.birbs.britishbirds.client.animation.pose.PoseResolver;
import com.birbs.britishbirds.client.animation.procedural.Breathing;
import com.birbs.britishbirds.client.animation.procedural.HeadTracking;
import com.birbs.britishbirds.client.animation.procedural.LandingImpact;
import com.birbs.britishbirds.client.animation.procedural.MovementDrag;
import com.birbs.britishbirds.client.animation.procedural.StartleResponse;
import com.birbs.britishbirds.client.animation.procedural.WeightShift;
import com.birbs.britishbirds.client.renderer.RobinRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.Map;

/**
 * Robin model rebuilt on the skeletal animation system.
 * Uses the universal 32-joint bird skeleton with spring-driven animation,
 * pose blending, and procedural behaviours.
 *
 * <p>512x512 texture. ModelPart hierarchy mirrors the BirdSkeleton joint tree
 * so parent rotations propagate to children naturally.
 */
public class RobinModel extends AbstractBirdModel<RobinRenderState> {

    // All 32 skeleton-driven parts
    private final ModelPart chest;
    private final ModelPart shoulderMount;
    private final ModelPart torso;
    private final ModelPart hip;
    private final ModelPart neckLower;
    private final ModelPart neckMid;
    private final ModelPart neckUpper;
    private final ModelPart head;
    private final ModelPart upperBeak;
    private final ModelPart lowerBeak;
    private final ModelPart lUpperWing;
    private final ModelPart lScapulars;
    private final ModelPart lForearm;
    private final ModelPart lSecondaries;
    private final ModelPart lHand;
    private final ModelPart lPrimaries;
    private final ModelPart rUpperWing;
    private final ModelPart rScapulars;
    private final ModelPart rForearm;
    private final ModelPart rSecondaries;
    private final ModelPart rHand;
    private final ModelPart rPrimaries;
    private final ModelPart tailBase;
    private final ModelPart tailFan;
    private final ModelPart lThigh;
    private final ModelPart lShin;
    private final ModelPart lTarsus;
    private final ModelPart lFoot;
    private final ModelPart rThigh;
    private final ModelPart rShin;
    private final ModelPart rTarsus;
    private final ModelPart rFoot;

    // Decorative (not skeleton-driven)
    private final ModelPart breast;
    private final ModelPart crown;

    public RobinModel(ModelPart root) {
        super(root);

        // Navigate the ModelPart hierarchy to store references
        this.chest = root.getChild("chest");

        // Spine chain off chest
        this.shoulderMount = this.chest.getChild("shoulder_mount");
        this.torso = this.chest.getChild("torso");
        this.hip = this.chest.getChild("hip");

        // Neck chain off chest
        this.neckLower = this.chest.getChild("neck_lower");
        this.neckMid = this.neckLower.getChild("neck_mid");
        this.neckUpper = this.neckMid.getChild("neck_upper");
        this.head = this.neckUpper.getChild("head");
        this.upperBeak = this.head.getChild("upper_beak");
        this.lowerBeak = this.head.getChild("lower_beak");

        // Left wing chain off shoulder_mount
        this.lUpperWing = this.shoulderMount.getChild("L_upper_wing");
        this.lScapulars = this.lUpperWing.getChild("L_scapulars");
        this.lForearm = this.lUpperWing.getChild("L_forearm");
        this.lSecondaries = this.lForearm.getChild("L_secondaries");
        this.lHand = this.lForearm.getChild("L_hand");
        this.lPrimaries = this.lHand.getChild("L_primaries");

        // Right wing chain off shoulder_mount
        this.rUpperWing = this.shoulderMount.getChild("R_upper_wing");
        this.rScapulars = this.rUpperWing.getChild("R_scapulars");
        this.rForearm = this.rUpperWing.getChild("R_forearm");
        this.rSecondaries = this.rForearm.getChild("R_secondaries");
        this.rHand = this.rForearm.getChild("R_hand");
        this.rPrimaries = this.rHand.getChild("R_primaries");

        // Tail chain off chest
        this.tailBase = this.chest.getChild("tail_base");
        this.tailFan = this.tailBase.getChild("tail_fan");

        // Left leg chain off hip
        this.lThigh = this.hip.getChild("L_thigh");
        this.lShin = this.lThigh.getChild("L_shin");
        this.lTarsus = this.lShin.getChild("L_tarsus");
        this.lFoot = this.lTarsus.getChild("L_foot");

        // Right leg chain off hip
        this.rThigh = this.hip.getChild("R_thigh");
        this.rShin = this.rThigh.getChild("R_shin");
        this.rTarsus = this.rShin.getChild("R_tarsus");
        this.rFoot = this.rTarsus.getChild("R_foot");

        // Decorative parts
        this.breast = this.chest.getChild("breast");
        this.crown = this.head.getChild("crown");

        // LAST: initialise skeleton binding
        initSkeleton(root);
    }

    // =========================================================================
    // Static mesh definition
    // =========================================================================

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Compute UV offsets from the standard passerine dimensions
        BirdUVLayout layout = BirdUVLayout.computeLayout(BirdUVLayout.getDefaultPasserineDimensions());

        // --- CHEST (root) --- 3,3,4 — deeper
        int[] uv = layout.getOffset(BirdSkeleton.CHEST);
        PartDefinition chestPart = partDefinition.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.5f, -2.0f, 3.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 19.0f, 0.0f));

        // --- Decorative: breast (child of chest) --- 3,2,2
        chestPart.addOrReplaceChild("breast",
                CubeListBuilder.create()
                        .texOffs(0, 200)
                        .addBox(-1.5f, -0.5f, -3.0f, 3.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, 0.5f, 0.0f));

        // --- SHOULDER_MOUNT (child of chest) --- 2,2,2
        uv = layout.getOffset(BirdSkeleton.SHOULDER_MOUNT);
        PartDefinition shoulderPart = chestPart.addOrReplaceChild("shoulder_mount",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // --- TORSO (child of chest) --- 3,3,4 — deeper
        uv = layout.getOffset(BirdSkeleton.TORSO);
        chestPart.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.5f, -2.0f, 3.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 2.0f));

        // --- HIP (child of chest) --- 2,2,3 — deeper
        uv = layout.getOffset(BirdSkeleton.HIP);
        PartDefinition hipPart = chestPart.addOrReplaceChild("hip",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -1.0f, -1.5f, 2.0f, 2.0f, 3.0f),
                PartPose.offset(0.0f, 1.0f, 1.5f));

        // --- NECK_LOWER (child of chest) --- 2,1,1
        uv = layout.getOffset(BirdSkeleton.NECK_LOWER);
        PartDefinition neckLowerPart = chestPart.addOrReplaceChild("neck_lower",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -1.0f, -0.5f, 2.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, -1.0f, -0.5f));

        // --- NECK_MID (child of neck_lower) --- 2,1,1
        uv = layout.getOffset(BirdSkeleton.NECK_MID);
        PartDefinition neckMidPart = neckLowerPart.addOrReplaceChild("neck_mid",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -1.0f, -0.5f, 2.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // --- NECK_UPPER (child of neck_mid) --- 2,1,1
        uv = layout.getOffset(BirdSkeleton.NECK_UPPER);
        PartDefinition neckUpperPart = neckMidPart.addOrReplaceChild("neck_upper",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -1.0f, -0.5f, 2.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // --- HEAD (child of neck_upper) --- 4,4,4
        uv = layout.getOffset(BirdSkeleton.HEAD);
        PartDefinition headPart = neckUpperPart.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // --- UPPER_BEAK (child of head) --- 1,1,2
        uv = layout.getOffset(BirdSkeleton.UPPER_BEAK);
        headPart.addOrReplaceChild("upper_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, -2.0f, -4.0f, 1.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- LOWER_BEAK (child of head) --- 1,1,2
        uv = layout.getOffset(BirdSkeleton.LOWER_BEAK);
        headPart.addOrReplaceChild("lower_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, -1.0f, -4.0f, 1.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- Decorative: crown (child of head) --- 3,1,3
        headPart.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 210)
                        .addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- LEFT WING CHAIN (lateral orientation: width extends in +X) ---

        // L_UPPER_WING (child of shoulder_mount) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.L_UPPER_WING);
        PartDefinition lUpperWingPart = shoulderPart.addOrReplaceChild("L_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -2.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(1.0f, -0.5f, 0.0f));

        // L_SCAPULARS (child of L_upper_wing) --- 3,1,3
        uv = layout.getOffset(BirdSkeleton.L_SCAPULARS);
        lUpperWingPart.addOrReplaceChild("L_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -1.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // L_FOREARM (child of L_upper_wing) --- 3,1,3 offset at (+4, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_FOREARM);
        PartDefinition lForearmPart = lUpperWingPart.addOrReplaceChild("L_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(4.0f, 0.0f, 0.0f));

        // L_SECONDARIES (child of L_forearm) --- 3,1,3
        uv = layout.getOffset(BirdSkeleton.L_SECONDARIES);
        lForearmPart.addOrReplaceChild("L_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -1.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // L_HAND (child of L_forearm) --- 3,1,2 offset at (+3, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_HAND);
        PartDefinition lHandPart = lForearmPart.addOrReplaceChild("L_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -1.0f, 3.0f, 1.0f, 2.0f),
                PartPose.offset(3.0f, 0.0f, 0.0f));

        // L_PRIMARIES (child of L_hand) --- 3,1,2
        uv = layout.getOffset(BirdSkeleton.L_PRIMARIES);
        lHandPart.addOrReplaceChild("L_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -0.5f, 3.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- RIGHT WING CHAIN (mirrored: width extends in -X) ---

        // R_UPPER_WING (child of shoulder_mount) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.R_UPPER_WING);
        PartDefinition rUpperWingPart = shoulderPart.addOrReplaceChild("R_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.0f, -0.5f, -2.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(-1.0f, -0.5f, 0.0f));

        // R_SCAPULARS (child of R_upper_wing) --- 3,1,3
        uv = layout.getOffset(BirdSkeleton.R_SCAPULARS);
        rUpperWingPart.addOrReplaceChild("R_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-3.5f, -0.5f, -1.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // R_FOREARM (child of R_upper_wing) --- 3,1,3 offset at (-4, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_FOREARM);
        PartDefinition rForearmPart = rUpperWingPart.addOrReplaceChild("R_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-3.0f, -0.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(-4.0f, 0.0f, 0.0f));

        // R_SECONDARIES (child of R_forearm) --- 3,1,3
        uv = layout.getOffset(BirdSkeleton.R_SECONDARIES);
        rForearmPart.addOrReplaceChild("R_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-3.5f, -0.5f, -1.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // R_HAND (child of R_forearm) --- 3,1,2 offset at (-3, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_HAND);
        PartDefinition rHandPart = rForearmPart.addOrReplaceChild("R_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-3.0f, -0.5f, -1.0f, 3.0f, 1.0f, 2.0f),
                PartPose.offset(-3.0f, 0.0f, 0.0f));

        // R_PRIMARIES (child of R_hand) --- 3,1,2
        uv = layout.getOffset(BirdSkeleton.R_PRIMARIES);
        rHandPart.addOrReplaceChild("R_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-3.5f, -0.5f, -0.5f, 3.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- TAIL CHAIN ---

        // TAIL_BASE (child of chest) --- 2,1,2 — offset further back
        uv = layout.getOffset(BirdSkeleton.TAIL_BASE);
        PartDefinition tailBasePart = chestPart.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(0.0f, 0.0f, 2.0f));

        // TAIL_FAN (child of tail_base) --- 2,1,4 — deeper
        uv = layout.getOffset(BirdSkeleton.TAIL_FAN);
        tailBasePart.addOrReplaceChild("tail_fan",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 2.0f));

        // --- LEFT LEG CHAIN (shorter) ---

        // L_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.L_THIGH);
        PartDefinition lThighPart = hipPart.addOrReplaceChild("L_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(0.75f, 1.0f, 0.0f));

        // L_SHIN (child of L_thigh) --- 1,2,1
        uv = layout.getOffset(BirdSkeleton.L_SHIN);
        PartDefinition lShinPart = lThighPart.addOrReplaceChild("L_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // L_TARSUS (child of L_shin) --- 1,1.5,1
        uv = layout.getOffset(BirdSkeleton.L_TARSUS);
        PartDefinition lTarsusPart = lShinPart.addOrReplaceChild("L_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.5f, 1.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // L_FOOT (child of L_tarsus) --- 2,0.5,2
        uv = layout.getOffset(BirdSkeleton.L_FOOT);
        lTarsusPart.addOrReplaceChild("L_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, 0.0f, -1.5f, 2.0f, 0.5f, 2.0f),
                PartPose.offset(0.0f, 1.5f, 0.0f));

        // --- RIGHT LEG CHAIN (shorter) ---

        // R_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.R_THIGH);
        PartDefinition rThighPart = hipPart.addOrReplaceChild("R_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(-0.75f, 1.0f, 0.0f));

        // R_SHIN (child of R_thigh) --- 1,2,1
        uv = layout.getOffset(BirdSkeleton.R_SHIN);
        PartDefinition rShinPart = rThighPart.addOrReplaceChild("R_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // R_TARSUS (child of R_shin) --- 1,1.5,1
        uv = layout.getOffset(BirdSkeleton.R_TARSUS);
        PartDefinition rTarsusPart = rShinPart.addOrReplaceChild("R_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.5f, 1.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // R_FOOT (child of R_tarsus) --- 2,0.5,2
        uv = layout.getOffset(BirdSkeleton.R_FOOT);
        rTarsusPart.addOrReplaceChild("R_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, 0.0f, -1.5f, 2.0f, 0.5f, 2.0f),
                PartPose.offset(0.0f, 1.5f, 0.0f));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }

    // =========================================================================
    // Skeleton binding
    // =========================================================================

    @Override
    protected void buildMapper(ModelPart root) {
        this.mapper = SkeletonModelMapper.builder()
                .bind(BirdSkeleton.CHEST,          chest)
                .bind(BirdSkeleton.SHOULDER_MOUNT, shoulderMount)
                .bind(BirdSkeleton.TORSO,          torso)
                .bind(BirdSkeleton.HIP,            hip)
                .bind(BirdSkeleton.NECK_LOWER,     neckLower)
                .bind(BirdSkeleton.NECK_MID,       neckMid)
                .bind(BirdSkeleton.NECK_UPPER,     neckUpper)
                .bind(BirdSkeleton.HEAD,           head)
                .bind(BirdSkeleton.UPPER_BEAK,     upperBeak)
                .bind(BirdSkeleton.LOWER_BEAK,     lowerBeak)
                .bind(BirdSkeleton.L_UPPER_WING,   lUpperWing)
                .bind(BirdSkeleton.L_SCAPULARS,    lScapulars)
                .bind(BirdSkeleton.L_FOREARM,      lForearm)
                .bind(BirdSkeleton.L_SECONDARIES,  lSecondaries)
                .bind(BirdSkeleton.L_HAND,         lHand)
                .bind(BirdSkeleton.L_PRIMARIES,    lPrimaries)
                .bind(BirdSkeleton.R_UPPER_WING,   rUpperWing)
                .bind(BirdSkeleton.R_SCAPULARS,    rScapulars)
                .bind(BirdSkeleton.R_FOREARM,      rForearm)
                .bind(BirdSkeleton.R_SECONDARIES,  rSecondaries)
                .bind(BirdSkeleton.R_HAND,         rHand)
                .bind(BirdSkeleton.R_PRIMARIES,    rPrimaries)
                .bind(BirdSkeleton.TAIL_BASE,      tailBase)
                .bind(BirdSkeleton.TAIL_FAN,       tailFan)
                .bind(BirdSkeleton.L_THIGH,        lThigh)
                .bind(BirdSkeleton.L_SHIN,         lShin)
                .bind(BirdSkeleton.L_TARSUS,       lTarsus)
                .bind(BirdSkeleton.L_FOOT,         lFoot)
                .bind(BirdSkeleton.R_THIGH,        rThigh)
                .bind(BirdSkeleton.R_SHIN,         rShin)
                .bind(BirdSkeleton.R_TARSUS,       rTarsus)
                .bind(BirdSkeleton.R_FOOT,         rFoot)
                .build();
    }

    // =========================================================================
    // Procedural behaviours
    // =========================================================================

    @Override
    protected void configureBehaviours() {
        behaviours.add(new Breathing(0.15f));       // small bird, fast breathing
        behaviours.add(new HeadTracking(1.5f, 0.8f));
        behaviours.add(new WeightShift(0.002f));
        behaviours.add(new LandingImpact());
        behaviours.add(new MovementDrag());
        behaviours.add(new StartleResponse());
    }

    // =========================================================================
    // Animation
    // =========================================================================

    @Override
    public void setupAnim(RobinRenderState state) {
        selectPoses(state);
        super.setupAnim(state); // runs full skeleton pipeline
    }

    private void selectPoses(RobinRenderState state) {
        PoseResolver resolver = getResolver(state);

        if (state.isFlying) {
            resolver.setBasePose(BaseBirdPoses.FLYING_CRUISE, 3.0f);
            // Convert flapAngle (-amplitude..+amplitude) to a 0..1 phase
            float flapPhase = state.flapAngle * 0.5f + 0.5f;
            resolver.setActiveCyclic(BaseBirdPoses.WINGBEAT, flapPhase);
            resolver.addOverlay(BaseBirdPoses.LEGS_TUCKED, 1.0f);
        } else if (state.isPecking) {
            resolver.setBasePose(PasserinePoses.FORAGE, 4.0f);
            // Peck phase: use ageInTicks as a simple oscillator
            float peckPhase = (float) (Math.sin(state.ageInTicks * 0.3f) * 0.5f + 0.5f);
            resolver.setActiveCyclic(PasserinePoses.PECK, peckPhase);
            resolver.removeOverlay("legs_tucked");
        } else {
            resolver.setBasePose(BaseBirdPoses.PERCHED, 2.0f);
            resolver.removeOverlay("legs_tucked");

            if (state.walkAnimationSpeed > 0.01f) {
                float walkPhase = (float) (Math.sin(state.walkAnimationPos * 0.6662f) * 0.5f + 0.5f);
                resolver.setActiveCyclic(BaseBirdPoses.WALK_CYCLE, walkPhase);
            } else {
                resolver.clearCyclic();

                // Occasional head tilt when idle
                if (((int) state.ageInTicks % 60) < 15) {
                    resolver.addOverlay(PasserinePoses.HEAD_TILT, 1.0f);
                } else {
                    resolver.removeOverlay("head_tilt");
                }
            }
        }
    }
}
