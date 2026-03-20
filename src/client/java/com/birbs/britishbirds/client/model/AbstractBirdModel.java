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
 * <p>Subclasses must:
 * <ol>
 *   <li>Store their own ModelPart field references in their constructor.
 *   <li>Call {@link #initSkeleton(ModelPart)} at the END of their constructor,
 *       after all ModelPart fields are assigned.
 *   <li>Implement {@link #buildMapper(ModelPart)} to bind skeleton joints to ModelParts.
 *   <li>Implement {@link #configureBehaviours()} to add procedural behaviours.
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
    // Abstract methods for subclasses
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link SkeletonModelMapper} that binds skeleton joint names to
     * the subclass's ModelPart fields, and assigns it to {@link #mapper}.
     */
    protected abstract void buildMapper(ModelPart root);

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
