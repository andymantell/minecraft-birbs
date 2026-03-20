package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.animation.SkeletonModelMapper;
import com.birbs.britishbirds.client.animation.pose.BaseBirdPoses;
import com.birbs.britishbirds.client.animation.pose.CyclicAnimation;
import com.birbs.britishbirds.client.animation.pose.PoseResolver;
import com.birbs.britishbirds.client.animation.pose.WaterfowlPoses;
import com.birbs.britishbirds.client.animation.procedural.Breathing;
import com.birbs.britishbirds.client.animation.procedural.HeadTracking;
import com.birbs.britishbirds.client.animation.procedural.LandingImpact;
import com.birbs.britishbirds.client.animation.procedural.MovementDrag;
import com.birbs.britishbirds.client.animation.procedural.StartleResponse;
import com.birbs.britishbirds.client.animation.procedural.WeightShift;
import com.birbs.britishbirds.client.renderer.MallardRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mallard model rebuilt on the skeletal animation system.
 * Uses the universal 32-joint bird skeleton with spring-driven animation,
 * pose blending, and procedural behaviours.
 *
 * <p>Heavy, rounded body with thick neck, broad flat bill, and webbed feet.
 * Drake tail curl decorative part toggled by sex. 512x512 texture.
 * ModelPart hierarchy mirrors the BirdSkeleton joint tree.
 */
public class MallardModel extends AbstractBirdModel<MallardRenderState> {

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
    private final ModelPart crown;
    private final ModelPart tailCurl;

    public MallardModel(ModelPart root) {
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
        this.crown = this.head.getChild("crown");
        this.tailCurl = this.tailFan.getChild("tail_curl");

        // LAST: initialise skeleton binding
        initSkeleton(root);
    }

    // =========================================================================
    // Species-specific cuboid dimensions
    // =========================================================================

    /**
     * Returns cuboid dimensions for a Mallard: heavy rounded body, thick neck,
     * broad flat bill, webbed feet.
     */
    private static Map<String, int[]> getMallardDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();

        // Heavy, rounded body — deeper
        dims.put(BirdSkeleton.CHEST,          new int[]{5, 5, 6});
        dims.put(BirdSkeleton.SHOULDER_MOUNT, new int[]{3, 3, 3});
        // Long body extends back, deeper
        dims.put(BirdSkeleton.TORSO,          new int[]{5, 5, 6});
        dims.put(BirdSkeleton.HIP,            new int[]{3, 3, 4});

        // Thick neck
        dims.put(BirdSkeleton.NECK_LOWER,     new int[]{3, 2, 2});
        dims.put(BirdSkeleton.NECK_MID,       new int[]{3, 2, 2});
        dims.put(BirdSkeleton.NECK_UPPER,     new int[]{3, 2, 2});
        // Rounded head with smooth dome
        dims.put(BirdSkeleton.HEAD,           new int[]{4, 4, 4});
        // Broad flat spatulate bill
        dims.put(BirdSkeleton.UPPER_BEAK,     new int[]{3, 1, 3});
        dims.put(BirdSkeleton.LOWER_BEAK,     new int[]{3, 1, 3});

        // Standard duck wings — lateral orientation (width x 1 x depth)
        dims.put(BirdSkeleton.L_UPPER_WING,   new int[]{5, 1, 6});
        dims.put(BirdSkeleton.L_SCAPULARS,    new int[]{4, 1, 4});
        dims.put(BirdSkeleton.L_FOREARM,      new int[]{4, 1, 5});
        dims.put(BirdSkeleton.L_SECONDARIES,  new int[]{4, 1, 4});
        dims.put(BirdSkeleton.L_HAND,         new int[]{4, 1, 3});
        dims.put(BirdSkeleton.L_PRIMARIES,    new int[]{4, 1, 2});

        dims.put(BirdSkeleton.R_UPPER_WING,   new int[]{5, 1, 6});
        dims.put(BirdSkeleton.R_SCAPULARS,    new int[]{4, 1, 4});
        dims.put(BirdSkeleton.R_FOREARM,      new int[]{4, 1, 5});
        dims.put(BirdSkeleton.R_SECONDARIES,  new int[]{4, 1, 4});
        dims.put(BirdSkeleton.R_HAND,         new int[]{4, 1, 3});
        dims.put(BirdSkeleton.R_PRIMARIES,    new int[]{4, 1, 2});

        // Broad tail — deeper
        dims.put(BirdSkeleton.TAIL_BASE,      new int[]{4, 1, 4});
        dims.put(BirdSkeleton.TAIL_FAN,       new int[]{3, 1, 4});

        // Shorter legs set far back, webbed feet
        dims.put(BirdSkeleton.L_THIGH,        new int[]{1, 1, 1});
        dims.put(BirdSkeleton.L_SHIN,         new int[]{1, 2, 1});
        dims.put(BirdSkeleton.L_TARSUS,       new int[]{1, 2, 1});
        dims.put(BirdSkeleton.L_FOOT,         new int[]{3, 1, 3});

        dims.put(BirdSkeleton.R_THIGH,        new int[]{1, 1, 1});
        dims.put(BirdSkeleton.R_SHIN,         new int[]{1, 2, 1});
        dims.put(BirdSkeleton.R_TARSUS,       new int[]{1, 2, 1});
        dims.put(BirdSkeleton.R_FOOT,         new int[]{3, 1, 3});

        return dims;
    }

    // =========================================================================
    // Static mesh definition
    // =========================================================================

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        BirdUVLayout layout = BirdUVLayout.computeLayout(getMallardDimensions());

        // --- CHEST (root) --- 5,5,6 — heavy, rounded, deeper
        int[] uv = layout.getOffset(BirdSkeleton.CHEST);
        PartDefinition chestPart = partDefinition.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.5f, -2.5f, -3.0f, 5.0f, 5.0f, 6.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f));

        // --- SHOULDER_MOUNT (child of chest) --- 3,3,3
        uv = layout.getOffset(BirdSkeleton.SHOULDER_MOUNT);
        PartDefinition shoulderPart = chestPart.addOrReplaceChild("shoulder_mount",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.5f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- TORSO (child of chest) --- 5,5,6 — deeper
        uv = layout.getOffset(BirdSkeleton.TORSO);
        chestPart.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.5f, -2.5f, -3.0f, 5.0f, 5.0f, 6.0f),
                PartPose.offset(0.0f, 0.0f, 3.0f));

        // --- HIP (child of chest) --- 3,3,4 — deeper
        uv = layout.getOffset(BirdSkeleton.HIP);
        PartDefinition hipPart = chestPart.addOrReplaceChild("hip",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.5f, -2.0f, 3.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 1.0f, 2.5f));

        // --- NECK_LOWER (child of chest) --- 3,2,2 — thick duck neck
        uv = layout.getOffset(BirdSkeleton.NECK_LOWER);
        PartDefinition neckLowerPart = chestPart.addOrReplaceChild("neck_lower",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -2.0f, -1.0f, 3.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, -2.0f, -1.0f));

        // --- NECK_MID (child of neck_lower) --- 3,2,2
        uv = layout.getOffset(BirdSkeleton.NECK_MID);
        PartDefinition neckMidPart = neckLowerPart.addOrReplaceChild("neck_mid",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -2.0f, -1.0f, 3.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- NECK_UPPER (child of neck_mid) --- 3,2,2
        uv = layout.getOffset(BirdSkeleton.NECK_UPPER);
        PartDefinition neckUpperPart = neckMidPart.addOrReplaceChild("neck_upper",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -2.0f, -1.0f, 3.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- HEAD (child of neck_upper) --- 4,4,4 — rounded with smooth dome
        uv = layout.getOffset(BirdSkeleton.HEAD);
        PartDefinition headPart = neckUpperPart.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- UPPER_BEAK (child of head) --- 3,1,3 — broad flat spatulate bill
        uv = layout.getOffset(BirdSkeleton.UPPER_BEAK);
        headPart.addOrReplaceChild("upper_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -2.0f, -5.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- LOWER_BEAK (child of head) --- 3,1,3
        uv = layout.getOffset(BirdSkeleton.LOWER_BEAK);
        headPart.addOrReplaceChild("lower_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.0f, -5.0f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- Decorative: crown dome (child of head) --- 3,1,3
        headPart.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 200)
                        .addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- LEFT WING CHAIN (lateral orientation) ---

        // L_UPPER_WING (child of shoulder_mount) --- 5,1,6
        uv = layout.getOffset(BirdSkeleton.L_UPPER_WING);
        PartDefinition lUpperWingPart = shoulderPart.addOrReplaceChild("L_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -3.0f, 5.0f, 1.0f, 6.0f),
                PartPose.offset(1.5f, -0.5f, 0.0f));

        // L_SCAPULARS (child of L_upper_wing) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.L_SCAPULARS);
        lUpperWingPart.addOrReplaceChild("L_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -1.5f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);

        // L_FOREARM (child of L_upper_wing) --- 4,1,5 offset at (+5, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_FOREARM);
        PartDefinition lForearmPart = lUpperWingPart.addOrReplaceChild("L_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -2.5f, 4.0f, 1.0f, 5.0f),
                PartPose.offset(5.0f, 0.0f, 0.0f));

        // L_SECONDARIES (child of L_forearm) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.L_SECONDARIES);
        lForearmPart.addOrReplaceChild("L_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -1.5f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);

        // L_HAND (child of L_forearm) --- 4,1,3 offset at (+4, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_HAND);
        PartDefinition lHandPart = lForearmPart.addOrReplaceChild("L_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -1.5f, 4.0f, 1.0f, 3.0f),
                PartPose.offset(4.0f, 0.0f, 0.0f));

        // L_PRIMARIES (child of L_hand) --- 4,1,2
        uv = layout.getOffset(BirdSkeleton.L_PRIMARIES);
        lHandPart.addOrReplaceChild("L_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -0.5f, 4.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- RIGHT WING CHAIN (mirrored lateral) ---

        // R_UPPER_WING (child of shoulder_mount) --- 5,1,6
        uv = layout.getOffset(BirdSkeleton.R_UPPER_WING);
        PartDefinition rUpperWingPart = shoulderPart.addOrReplaceChild("R_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.0f, -0.5f, -3.0f, 5.0f, 1.0f, 6.0f),
                PartPose.offset(-1.5f, -0.5f, 0.0f));

        // R_SCAPULARS (child of R_upper_wing) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.R_SCAPULARS);
        rUpperWingPart.addOrReplaceChild("R_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.5f, -0.5f, -1.5f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);

        // R_FOREARM (child of R_upper_wing) --- 4,1,5 offset at (-5, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_FOREARM);
        PartDefinition rForearmPart = rUpperWingPart.addOrReplaceChild("R_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.0f, -0.5f, -2.5f, 4.0f, 1.0f, 5.0f),
                PartPose.offset(-5.0f, 0.0f, 0.0f));

        // R_SECONDARIES (child of R_forearm) --- 4,1,4
        uv = layout.getOffset(BirdSkeleton.R_SECONDARIES);
        rForearmPart.addOrReplaceChild("R_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.5f, -0.5f, -1.5f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);

        // R_HAND (child of R_forearm) --- 4,1,3 offset at (-4, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_HAND);
        PartDefinition rHandPart = rForearmPart.addOrReplaceChild("R_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.0f, -0.5f, -1.5f, 4.0f, 1.0f, 3.0f),
                PartPose.offset(-4.0f, 0.0f, 0.0f));

        // R_PRIMARIES (child of R_hand) --- 4,1,2
        uv = layout.getOffset(BirdSkeleton.R_PRIMARIES);
        rHandPart.addOrReplaceChild("R_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-4.5f, -0.5f, -0.5f, 4.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        // --- TAIL CHAIN ---

        // TAIL_BASE (child of chest) --- 4,1,4 — deeper, offset further back
        uv = layout.getOffset(BirdSkeleton.TAIL_BASE);
        PartDefinition tailBasePart = chestPart.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -0.5f, 0.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 3.0f));

        // TAIL_FAN (child of tail_base) --- 3,1,4 — deeper
        uv = layout.getOffset(BirdSkeleton.TAIL_FAN);
        PartDefinition tailFanPart = tailBasePart.addOrReplaceChild("tail_fan",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 4.0f));

        // --- Decorative: drake tail curl (child of tail_fan) --- 1,1,2
        tailFanPart.addOrReplaceChild("tail_curl",
                CubeListBuilder.create()
                        .texOffs(0, 210)
                        .addBox(-0.5f, -1.0f, 1.0f, 1.0f, 1.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -0.5f, 1.0f,
                        (float) Math.toRadians(-30.0), 0.0f, 0.0f));

        // --- LEFT LEG CHAIN --- shorter legs set far back, webbed feet

        // L_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.L_THIGH);
        PartDefinition lThighPart = hipPart.addOrReplaceChild("L_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(1.0f, 1.5f, 0.5f));

        // L_SHIN (child of L_thigh) --- 1,2,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.L_SHIN);
        PartDefinition lShinPart = lThighPart.addOrReplaceChild("L_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // L_TARSUS (child of L_shin) --- 1,2,1
        uv = layout.getOffset(BirdSkeleton.L_TARSUS);
        PartDefinition lTarsusPart = lShinPart.addOrReplaceChild("L_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // L_FOOT (child of L_tarsus) --- 3,1,3 — webbed feet, wide
        uv = layout.getOffset(BirdSkeleton.L_FOOT);
        lTarsusPart.addOrReplaceChild("L_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, 0.0f, -2.5f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // --- RIGHT LEG CHAIN ---

        // R_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.R_THIGH);
        PartDefinition rThighPart = hipPart.addOrReplaceChild("R_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(-1.0f, 1.5f, 0.5f));

        // R_SHIN (child of R_thigh) --- 1,2,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.R_SHIN);
        PartDefinition rShinPart = rThighPart.addOrReplaceChild("R_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // R_TARSUS (child of R_shin) --- 1,2,1
        uv = layout.getOffset(BirdSkeleton.R_TARSUS);
        PartDefinition rTarsusPart = rShinPart.addOrReplaceChild("R_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // R_FOOT (child of R_tarsus) --- 3,1,3 — webbed
        uv = layout.getOffset(BirdSkeleton.R_FOOT);
        rTarsusPart.addOrReplaceChild("R_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, 0.0f, -2.5f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

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
        behaviours.add(new Breathing(0.1f));         // medium bird, moderate breathing
        behaviours.add(new HeadTracking(1.2f, 0.6f));
        behaviours.add(new WeightShift(0.002f));
        behaviours.add(new LandingImpact());
        behaviours.add(new MovementDrag());
        behaviours.add(new StartleResponse());
    }

    // =========================================================================
    // Animation
    // =========================================================================

    @Override
    public void setupAnim(MallardRenderState state) {
        // Drake tail curl: only visible for adult males
        this.tailCurl.visible = state.isMale && !state.isBaby;

        selectPoses(state);
        super.setupAnim(state); // runs full skeleton pipeline
    }

    private void selectPoses(MallardRenderState state) {
        PoseResolver resolver = getResolver(state);

        if (state.isDabbling) {
            // Dabbling: body pitched steeply, tail up, head underwater
            resolver.setBasePose(WaterfowlPoses.DABBLE, 3.0f);
            resolver.clearCyclic();
            resolver.removeOverlay("legs_tucked");
        } else if (state.isFlying) {
            // Direct flight: body horizontal, rapid stiff wingbeats
            resolver.setBasePose(BaseBirdPoses.FLYING_CRUISE, 3.0f);
            float flapPhase = state.flapAngle * 0.5f + 0.5f;
            resolver.setActiveCyclic(WaterfowlPoses.WATERFOWL_WINGBEAT, flapPhase);
            resolver.addOverlay(BaseBirdPoses.LEGS_TUCKED, 1.0f);
        } else if (state.isSwimming) {
            // Swimming: body level, legs paddle alternately
            resolver.setBasePose(WaterfowlPoses.SWIM, 2.0f);
            // Paddle phase from age ticks
            float paddlePhase = (float) (Math.sin(state.ageInTicks * 1.5f) * 0.5f + 0.5f);
            resolver.setActiveCyclic(WaterfowlPoses.PADDLE, paddlePhase);
            resolver.removeOverlay("legs_tucked");
        } else {
            // Ground / idle
            resolver.setBasePose(BaseBirdPoses.PERCHED, 2.0f);
            resolver.removeOverlay("legs_tucked");

            if (state.walkAnimationSpeed > 0.01f || state.isWaddling) {
                // Waddling: characteristic duck side-to-side roll
                float walkPhase = (float) (Math.sin(state.walkAnimationPos * 0.6662f) * 0.5f + 0.5f);
                resolver.setActiveCyclic(WaterfowlPoses.WADDLE, walkPhase);
            } else {
                resolver.clearCyclic();

                // Head look-around when idle
                int tick = (int) state.ageInTicks % 100;
                if (tick < 25) {
                    resolver.addOverlay(BaseBirdPoses.BEAK_OPEN, 0.0f); // placeholder — just head turn
                } else {
                    resolver.removeOverlay("beak_open");
                }
            }
        }
    }
}
