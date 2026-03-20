package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.animation.BirdSkeletonState;
import com.birbs.britishbirds.client.animation.SkeletonModelMapper;
import com.birbs.britishbirds.client.animation.SpringSolver;
import com.birbs.britishbirds.client.animation.pose.PoseResolver;
import com.birbs.britishbirds.client.animation.procedural.ProceduralBehaviour;
import com.birbs.britishbirds.client.renderer.BirdRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base model class that drives the skeletal animation pipeline for all British birds.
 *
 * <p>Provides all 32 standard skeleton-driven ModelPart fields and a default
 * {@link #buildMapper(ModelPart)} implementation. Subclasses must:
 * <ol>
 *   <li>Call {@link #initCommonParts(ModelPart)} in their constructor to assign
 *       the 32 common ModelPart fields.
 *   <li>Assign any species-specific decorative ModelPart fields.
 *   <li>Call {@link #initSkeleton(ModelPart)} at the END of their constructor.
 *   <li>Implement {@link #configureBehaviours()} to add procedural behaviours.
 *   <li>Optionally override {@link #buildMapper(ModelPart)} to add extra bindings.
 * </ol>
 *
 * <p>The animation pipeline in {@link #setupAnim} runs each frame:
 * load state → reset targets → resolve poses → apply procedural behaviours →
 * solve springs → map to model parts → save state.
 */
public abstract class AbstractBirdModel<S extends BirdRenderState> extends EntityModel<S> {

    // -------------------------------------------------------------------------
    // Per-entity animation state bundle
    // -------------------------------------------------------------------------

    protected static class BirdAnimationState {
        final BirdSkeletonState skeletonState = new BirdSkeletonState();
        final PoseResolver poseResolver = new PoseResolver();
        long lastRenderedTick = 0;
    }

    // -------------------------------------------------------------------------
    // Common 32-joint skeleton ModelPart fields
    // -------------------------------------------------------------------------

    protected ModelPart chest, shoulderMount, torso, hip;
    protected ModelPart neckLower, neckMid, neckUpper, head, upperBeak, lowerBeak;
    protected ModelPart lUpperWing, lScapulars, lForearm, lSecondaries, lHand, lPrimaries;
    protected ModelPart rUpperWing, rScapulars, rForearm, rSecondaries, rHand, rPrimaries;
    protected ModelPart tailBase, tailFan;
    protected ModelPart lThigh, lShin, lTarsus, lFoot;
    protected ModelPart rThigh, rShin, rTarsus, rFoot;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    protected final BirdSkeleton skeleton;
    private final Map<Integer, BirdAnimationState> stateMap = new HashMap<>();
    protected SkeletonModelMapper mapper;
    protected final List<ProceduralBehaviour> behaviours = new ArrayList<>();
    private int cleanupCounter = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public AbstractBirdModel(ModelPart root) {
        super(root);
        this.skeleton = BirdSkeleton.createUniversal();
    }

    // -------------------------------------------------------------------------
    // Subclass initialisation — call at the END of subclass constructor
    // -------------------------------------------------------------------------

    /**
     * Initialises the skeleton mapper and procedural behaviours.
     * Must be called by the subclass constructor AFTER all ModelPart fields are set.
     */
    protected final void initSkeleton(ModelPart root) {
        buildMapper(root);
        configureBehaviours();
    }

    // -------------------------------------------------------------------------
    // Common ModelPart initialisation
    // -------------------------------------------------------------------------

    /**
     * Navigates the standard ModelPart hierarchy and assigns all 32 common
     * skeleton-driven fields. Call this in the subclass constructor before
     * {@link #initSkeleton(ModelPart)}.
     */
    protected void initCommonParts(ModelPart root) {
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
    }

    // -------------------------------------------------------------------------
    // Abstract methods for subclasses
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link SkeletonModelMapper} that binds skeleton joint names to
     * ModelPart fields, and assigns it to {@link #mapper}.
     * <p>The default implementation binds all 32 standard joints. Subclasses
     * only need to override this if they add extra bindings.
     */
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

    /**
     * Adds {@link ProceduralBehaviour} instances to {@link #behaviours}.
     */
    protected abstract void configureBehaviours();

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    protected PoseResolver getResolver(S state) {
        return getOrCreateState(state.entityId).poseResolver;
    }

    private BirdAnimationState getOrCreateState(int entityId) {
        return stateMap.computeIfAbsent(entityId, k -> new BirdAnimationState());
    }

    // -------------------------------------------------------------------------
    // Animation pipeline
    // -------------------------------------------------------------------------

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);

        // 1. Look up or create per-entity state
        BirdAnimationState animState = getOrCreateState(state.entityId);

        // 2. Load skeleton state from persistent storage
        skeleton.loadState(animState.skeletonState);

        // 2b. Reset spring parameters to defaults (prevents bleed between entities)
        skeleton.resetSprings();

        // 3. Compute deltaTime in seconds (ageInTicks is in game ticks, 20 ticks/sec)
        float dt;
        if (animState.skeletonState.prevAgeAndPartial < 0) {
            // First frame — no previous timestamp
            dt = 0.05f;
        } else {
            float dtTicks = state.ageInTicks - animState.skeletonState.prevAgeAndPartial;
            dt = Math.clamp(dtTicks / 20.0f, 0.001f, 0.5f);
        }
        animState.skeletonState.prevAgeAndPartial = state.ageInTicks;

        // 4. Reset all joint targets to zero
        for (BirdJoint joint : skeleton.getAllJoints()) {
            joint.setTarget(0, 0, 0);
        }

        // 5. Pose resolver sets targets from base pose, cyclics, and overlays
        animState.poseResolver.resolve(skeleton, dt);

        // 6. Procedural behaviours add offsets to targets
        for (ProceduralBehaviour behaviour : behaviours) {
            behaviour.apply(skeleton, state, dt);
        }

        // 7. Spring solver drives angles toward targets
        SpringSolver.solveAll(skeleton.getAllJoints(), dt);

        // 8. Map skeleton angles to ModelPart rotations
        if (mapper != null) {
            mapper.apply(skeleton);
        }

        // 9. Save state back to persistent storage
        skeleton.saveState(animState.skeletonState);

        // 10. Update last-rendered tick for stale state cleanup
        cleanupCounter++;
        animState.lastRenderedTick = cleanupCounter;

        // 11. Periodic stale state cleanup (every 200 frames)
        if (cleanupCounter % 200 == 0) {
            long threshold = cleanupCounter - 100;
            Iterator<Map.Entry<Integer, BirdAnimationState>> it = stateMap.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().lastRenderedTick < threshold) {
                    it.remove();
                }
            }
        }
    }
}
