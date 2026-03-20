package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.animation.pose.BaseBirdPoses;
import com.birbs.britishbirds.client.animation.pose.PoseData;
import com.birbs.britishbirds.client.animation.pose.PoseResolver;
import com.birbs.britishbirds.client.animation.pose.RaptorPoses;
import com.birbs.britishbirds.client.animation.procedural.Breathing;
import com.birbs.britishbirds.client.animation.procedural.HeadTracking;
import com.birbs.britishbirds.client.animation.procedural.LandingImpact;
import com.birbs.britishbirds.client.animation.procedural.MovementDrag;
import com.birbs.britishbirds.client.animation.procedural.SlowHeadScan;
import com.birbs.britishbirds.client.animation.procedural.StartleResponse;
import com.birbs.britishbirds.client.animation.procedural.WeightShift;
import com.birbs.britishbirds.client.renderer.BarnOwlRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Barn Owl model rebuilt on the skeletal animation system.
 * Uses the universal 32-joint bird skeleton with spring-driven animation,
 * pose blending, and procedural behaviours.
 *
 * <p>Barn Owl is MUCH larger than passerines — slender elongated body, very broad
 * rounded wings, long legs for striking, and the defining heart-shaped facial disc.
 * Decorative part: facialDisc (child of head).
 * 512x512 texture. ModelPart hierarchy mirrors the BirdSkeleton joint tree.
 */
public class BarnOwlModel extends AbstractBirdModel<BarnOwlRenderState> {

    // =========================================================================
    // Owl loose feathers overlay — softer springs for silent, fluid flight
    // =========================================================================

    /** Loose trailing feathers for owl's characteristically silent, fluid flight. */
    private static final PoseData OWL_LOOSE_FEATHERS = PoseData.builder("owl_loose_feathers")
            .spring(BirdSkeleton.L_SCAPULARS, 8f, 1.0f)
            .spring(BirdSkeleton.L_SECONDARIES, 8f, 1.0f)
            .spring(BirdSkeleton.L_PRIMARIES, 6f, 0.8f)
            .spring(BirdSkeleton.R_SCAPULARS, 8f, 1.0f)
            .spring(BirdSkeleton.R_SECONDARIES, 8f, 1.0f)
            .spring(BirdSkeleton.R_PRIMARIES, 6f, 0.8f)
            .build();

    // Decorative (not skeleton-driven)
    private final ModelPart facialDisc;

    public BarnOwlModel(ModelPart root) {
        super(root);
        initCommonParts(root);

        // Decorative parts
        this.facialDisc = this.head.getChild("facial_disc");

        // LAST: initialise skeleton binding
        initSkeleton(root);
    }

    // =========================================================================
    // Static mesh definition
    // =========================================================================

    /**
     * Returns cuboid dimensions for a barn owl — much larger than passerines,
     * with very broad wings and long legs.
     */
    public static Map<String, int[]> getBarnOwlDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();

        // Large body — slender and elongated, deeper
        dims.put(BirdSkeleton.CHEST,          new int[]{5, 5, 6});    // deeper chest
        dims.put(BirdSkeleton.SHOULDER_MOUNT, new int[]{3, 3, 3});
        dims.put(BirdSkeleton.TORSO,          new int[]{4, 4, 5});    // deeper torso
        dims.put(BirdSkeleton.HIP,            new int[]{4, 3, 5});    // deeper hip
        dims.put(BirdSkeleton.NECK_LOWER,     new int[]{2, 2, 1});
        dims.put(BirdSkeleton.NECK_MID,       new int[]{2, 2, 1});
        dims.put(BirdSkeleton.NECK_UPPER,     new int[]{2, 2, 1});
        dims.put(BirdSkeleton.HEAD,           new int[]{5, 5, 5});    // large rounded head
        dims.put(BirdSkeleton.UPPER_BEAK,     new int[]{1, 1, 1});    // small hooked beak
        dims.put(BirdSkeleton.LOWER_BEAK,     new int[]{1, 1, 1});

        // Very broad wings — lateral orientation (width x 1 x depth)
        dims.put(BirdSkeleton.L_UPPER_WING,   new int[]{6, 2, 6});    // wide lateral
        dims.put(BirdSkeleton.L_SCAPULARS,    new int[]{5, 2, 4});
        dims.put(BirdSkeleton.L_FOREARM,      new int[]{5, 2, 5});
        dims.put(BirdSkeleton.L_SECONDARIES,  new int[]{5, 2, 4});
        dims.put(BirdSkeleton.L_HAND,         new int[]{5, 1, 3});
        dims.put(BirdSkeleton.L_PRIMARIES,    new int[]{5, 1, 3});

        dims.put(BirdSkeleton.R_UPPER_WING,   new int[]{6, 2, 6});
        dims.put(BirdSkeleton.R_SCAPULARS,    new int[]{5, 2, 4});
        dims.put(BirdSkeleton.R_FOREARM,      new int[]{5, 2, 5});
        dims.put(BirdSkeleton.R_SECONDARIES,  new int[]{5, 2, 4});
        dims.put(BirdSkeleton.R_HAND,         new int[]{5, 1, 3});
        dims.put(BirdSkeleton.R_PRIMARIES,    new int[]{5, 1, 3});

        // Short square tail — deeper fan
        dims.put(BirdSkeleton.TAIL_BASE,      new int[]{3, 1, 3});
        dims.put(BirdSkeleton.TAIL_FAN,       new int[]{4, 1, 4});

        // Shorter legs (proportionally reduced)
        dims.put(BirdSkeleton.L_THIGH,        new int[]{1, 1, 1});
        dims.put(BirdSkeleton.L_SHIN,         new int[]{1, 3, 1});    // shorter
        dims.put(BirdSkeleton.L_TARSUS,       new int[]{1, 2, 1});    // shorter
        dims.put(BirdSkeleton.L_FOOT,         new int[]{2, 1, 2});    // talons

        dims.put(BirdSkeleton.R_THIGH,        new int[]{1, 1, 1});
        dims.put(BirdSkeleton.R_SHIN,         new int[]{1, 3, 1});
        dims.put(BirdSkeleton.R_TARSUS,       new int[]{1, 2, 1});
        dims.put(BirdSkeleton.R_FOOT,         new int[]{2, 1, 2});

        return dims;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Compute UV offsets from the barn owl dimensions
        BirdUVLayout layout = BirdUVLayout.computeLayout(getBarnOwlDimensions());

        // --- CHEST (root) --- 5,5,6 — large body, deeper, slight forward lean
        int[] uv = layout.getOffset(BirdSkeleton.CHEST);
        PartDefinition chestPart = partDefinition.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.5f, -2.5f, -3.0f, 5.0f, 5.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, 15.5f, 0.0f,
                        (float) Math.toRadians(5.0), 0.0f, 0.0f));

        // --- SHOULDER_MOUNT (child of chest) --- 3,3,3
        uv = layout.getOffset(BirdSkeleton.SHOULDER_MOUNT);
        PartDefinition shoulderPart = chestPart.addOrReplaceChild("shoulder_mount",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -1.5f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, -2.0f, 0.0f));

        // --- TORSO (child of chest) --- 4,4,5 — deeper
        uv = layout.getOffset(BirdSkeleton.TORSO);
        chestPart.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -2.0f, -2.5f, 4.0f, 4.0f, 5.0f),
                PartPose.offset(0.0f, 0.0f, 3.0f));

        // --- HIP (child of chest) --- 4,3,5 — deeper
        uv = layout.getOffset(BirdSkeleton.HIP);
        PartDefinition hipPart = chestPart.addOrReplaceChild("hip",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -1.5f, -2.5f, 4.0f, 3.0f, 5.0f),
                PartPose.offset(0.0f, 2.0f, 1.5f));

        // --- NECK_LOWER (child of chest) --- 2,2,1
        uv = layout.getOffset(BirdSkeleton.NECK_LOWER);
        PartDefinition neckLowerPart = chestPart.addOrReplaceChild("neck_lower",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -2.0f, -0.5f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, -2.5f, -0.5f));

        // --- NECK_MID (child of neck_lower) --- 2,2,1
        uv = layout.getOffset(BirdSkeleton.NECK_MID);
        PartDefinition neckMidPart = neckLowerPart.addOrReplaceChild("neck_mid",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -2.0f, -0.5f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- NECK_UPPER (child of neck_mid) --- 2,2,1
        uv = layout.getOffset(BirdSkeleton.NECK_UPPER);
        PartDefinition neckUpperPart = neckMidPart.addOrReplaceChild("neck_upper",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, -2.0f, -0.5f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- HEAD (child of neck_upper) --- 5,5,5 — large rounded owl head
        uv = layout.getOffset(BirdSkeleton.HEAD);
        PartDefinition headPart = neckUpperPart.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, -1.5f, 0.0f));

        // --- UPPER_BEAK (child of head) --- 1,1,1 — small hooked beak in facial disc
        uv = layout.getOffset(BirdSkeleton.UPPER_BEAK);
        headPart.addOrReplaceChild("upper_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, -2.5f, -3.5f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // --- LOWER_BEAK (child of head) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.LOWER_BEAK);
        headPart.addOrReplaceChild("lower_beak",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, -1.5f, -3.5f, 1.0f, 1.0f, 1.0f),
                PartPose.ZERO);

        // --- Decorative: heart-shaped facial disc (child of head) --- 6,5,1
        // The defining feature of barn owls — broader plate on front of head
        headPart.addOrReplaceChild("facial_disc",
                CubeListBuilder.create()
                        .texOffs(0, 200)
                        .addBox(-3.0f, -4.5f, -3.5f, 6.0f, 5.0f, 1.0f),
                PartPose.ZERO);

        // --- LEFT WING CHAIN (very broad, lateral orientation) ---

        // L_UPPER_WING (child of shoulder_mount) --- 6,2,6
        uv = layout.getOffset(BirdSkeleton.L_UPPER_WING);
        PartDefinition lUpperWingPart = shoulderPart.addOrReplaceChild("L_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -1.0f, -3.0f, 6.0f, 2.0f, 6.0f),
                PartPose.offset(1.5f, -0.5f, 0.0f));

        // L_SCAPULARS (child of L_upper_wing) --- 5,2,4
        uv = layout.getOffset(BirdSkeleton.L_SCAPULARS);
        lUpperWingPart.addOrReplaceChild("L_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -1.0f, -1.5f, 5.0f, 2.0f, 4.0f),
                PartPose.ZERO);

        // L_FOREARM (child of L_upper_wing) --- 5,2,5 offset at (+6, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_FOREARM);
        PartDefinition lForearmPart = lUpperWingPart.addOrReplaceChild("L_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -1.0f, -2.5f, 5.0f, 2.0f, 5.0f),
                PartPose.offset(6.0f, 0.0f, 0.0f));

        // L_SECONDARIES (child of L_forearm) --- 5,2,4
        uv = layout.getOffset(BirdSkeleton.L_SECONDARIES);
        lForearmPart.addOrReplaceChild("L_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -1.0f, -1.5f, 5.0f, 2.0f, 4.0f),
                PartPose.ZERO);

        // L_HAND (child of L_forearm) --- 5,1,3 offset at (+5, 0, 0)
        uv = layout.getOffset(BirdSkeleton.L_HAND);
        PartDefinition lHandPart = lForearmPart.addOrReplaceChild("L_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.0f, -0.5f, -1.5f, 5.0f, 1.0f, 3.0f),
                PartPose.offset(5.0f, 0.0f, 0.0f));

        // L_PRIMARIES (child of L_hand) --- 5,1,3
        uv = layout.getOffset(BirdSkeleton.L_PRIMARIES);
        lHandPart.addOrReplaceChild("L_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(0.5f, -0.5f, -1.0f, 5.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- RIGHT WING CHAIN (mirrored, very broad, lateral) ---

        // R_UPPER_WING (child of shoulder_mount) --- 6,2,6
        uv = layout.getOffset(BirdSkeleton.R_UPPER_WING);
        PartDefinition rUpperWingPart = shoulderPart.addOrReplaceChild("R_upper_wing",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-6.0f, -1.0f, -3.0f, 6.0f, 2.0f, 6.0f),
                PartPose.offset(-1.5f, -0.5f, 0.0f));

        // R_SCAPULARS (child of R_upper_wing) --- 5,2,4
        uv = layout.getOffset(BirdSkeleton.R_SCAPULARS);
        rUpperWingPart.addOrReplaceChild("R_scapulars",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.5f, -1.0f, -1.5f, 5.0f, 2.0f, 4.0f),
                PartPose.ZERO);

        // R_FOREARM (child of R_upper_wing) --- 5,2,5 offset at (-6, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_FOREARM);
        PartDefinition rForearmPart = rUpperWingPart.addOrReplaceChild("R_forearm",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.0f, -1.0f, -2.5f, 5.0f, 2.0f, 5.0f),
                PartPose.offset(-6.0f, 0.0f, 0.0f));

        // R_SECONDARIES (child of R_forearm) --- 5,2,4
        uv = layout.getOffset(BirdSkeleton.R_SECONDARIES);
        rForearmPart.addOrReplaceChild("R_secondaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.5f, -1.0f, -1.5f, 5.0f, 2.0f, 4.0f),
                PartPose.ZERO);

        // R_HAND (child of R_forearm) --- 5,1,3 offset at (-5, 0, 0)
        uv = layout.getOffset(BirdSkeleton.R_HAND);
        PartDefinition rHandPart = rForearmPart.addOrReplaceChild("R_hand",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.0f, -0.5f, -1.5f, 5.0f, 1.0f, 3.0f),
                PartPose.offset(-5.0f, 0.0f, 0.0f));

        // R_PRIMARIES (child of R_hand) --- 5,1,3
        uv = layout.getOffset(BirdSkeleton.R_PRIMARIES);
        rHandPart.addOrReplaceChild("R_primaries",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-5.5f, -0.5f, -1.0f, 5.0f, 1.0f, 3.0f),
                PartPose.ZERO);

        // --- TAIL CHAIN ---

        // TAIL_BASE (child of chest) --- 3,1,3 — deeper, offset further back
        uv = layout.getOffset(BirdSkeleton.TAIL_BASE);
        PartDefinition tailBasePart = chestPart.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(0.0f, 0.0f, 3.0f));

        // TAIL_FAN (child of tail_base) --- 4,1,4 — deeper
        uv = layout.getOffset(BirdSkeleton.TAIL_FAN);
        tailBasePart.addOrReplaceChild("tail_fan",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-2.0f, -0.5f, 0.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, 3.0f));

        // --- LEFT LEG CHAIN (shorter) ---

        // L_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.L_THIGH);
        PartDefinition lThighPart = hipPart.addOrReplaceChild("L_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(1.5f, 1.5f, 0.0f));

        // L_SHIN (child of L_thigh) --- 1,3,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.L_SHIN);
        PartDefinition lShinPart = lThighPart.addOrReplaceChild("L_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // L_TARSUS (child of L_shin) --- 1,2,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.L_TARSUS);
        PartDefinition lTarsusPart = lShinPart.addOrReplaceChild("L_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 3.0f, 0.0f));

        // L_FOOT (child of L_tarsus) --- 2,1,2 (talons)
        uv = layout.getOffset(BirdSkeleton.L_FOOT);
        lTarsusPart.addOrReplaceChild("L_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, 0.0f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // --- RIGHT LEG CHAIN (shorter) ---

        // R_THIGH (child of hip) --- 1,1,1
        uv = layout.getOffset(BirdSkeleton.R_THIGH);
        PartDefinition rThighPart = hipPart.addOrReplaceChild("R_thigh",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 1.0f, 1.0f),
                PartPose.offset(-1.5f, 1.5f, 0.0f));

        // R_SHIN (child of R_thigh) --- 1,3,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.R_SHIN);
        PartDefinition rShinPart = rThighPart.addOrReplaceChild("R_shin",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(0.0f, 1.0f, 0.0f));

        // R_TARSUS (child of R_shin) --- 1,2,1 (shorter)
        uv = layout.getOffset(BirdSkeleton.R_TARSUS);
        PartDefinition rTarsusPart = rShinPart.addOrReplaceChild("R_tarsus",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 3.0f, 0.0f));

        // R_FOOT (child of R_tarsus) --- 2,1,2 (talons)
        uv = layout.getOffset(BirdSkeleton.R_FOOT);
        rTarsusPart.addOrReplaceChild("R_foot",
                CubeListBuilder.create()
                        .texOffs(uv[0], uv[1])
                        .addBox(-1.0f, 0.0f, -1.5f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }

    // =========================================================================
    // Procedural behaviours
    // =========================================================================

    @Override
    protected void configureBehaviours() {
        behaviours.add(new Breathing(0.08f));        // large bird, slow breathing
        behaviours.add(new HeadTracking(1.2f, 0.6f));
        behaviours.add(new WeightShift(0.001f));     // subtle — large bird
        behaviours.add(new LandingImpact());
        behaviours.add(new MovementDrag());
        behaviours.add(new StartleResponse());
        behaviours.add(new SlowHeadScan());          // owl's slow deliberate head rotation
    }

    // =========================================================================
    // Animation
    // =========================================================================

    @Override
    public void setupAnim(BarnOwlRenderState state) {
        selectPoses(state);
        super.setupAnim(state); // runs full skeleton pipeline
    }

    private void selectPoses(BarnOwlRenderState state) {
        PoseResolver resolver = getResolver(state);

        if (state.isHovering) {
            // Quartering flight — hovering over fields, scanning for prey
            resolver.setBasePose(RaptorPoses.HOVER, 3.0f);
            // Rapid wingbeats during hover
            float hoverFlap = (float) (Math.sin(state.ageInTicks * 1.5f) * 0.5f + 0.5f);
            resolver.setActiveCyclic(RaptorPoses.RAPTOR_WINGBEAT, hoverFlap);
            resolver.removeOverlay("legs_tucked");
            // Loose trailing feathers for silent, fluid flight
            resolver.addOverlay(OWL_LOOSE_FEATHERS, 1.0f);
        } else if (state.isFlying) {
            // Sustained flight — slow transition (2.0f) for natural wing-spread
            resolver.setBasePose(BaseBirdPoses.FLYING_CRUISE, 2.0f);
            float flapPhase = state.flapAngle * 0.5f + 0.5f;
            resolver.setActiveCyclic(RaptorPoses.RAPTOR_WINGBEAT, flapPhase);
            resolver.addOverlay(BaseBirdPoses.LEGS_TUCKED, 1.0f);
            // Loose trailing feathers for silent, fluid flight
            resolver.addOverlay(OWL_LOOSE_FEATHERS, 1.0f);
        } else {
            // Perched — slow settle (1.5f) so wings fold gradually on landing
            resolver.setBasePose(RaptorPoses.RAPTOR_PERCH, 1.5f);
            resolver.removeOverlay("legs_tucked");
            resolver.removeOverlay("owl_loose_feathers");

            if (state.walkAnimationSpeed > 0.01f) {
                float walkPhase = (float) (Math.sin(state.walkAnimationPos * 0.6662f) * 0.5f + 0.5f);
                resolver.setActiveCyclic(BaseBirdPoses.WALK_CYCLE, walkPhase);
            } else {
                resolver.clearCyclic();
                // SlowHeadScan procedural behaviour now handles head rotation
            }
        }
    }
}
