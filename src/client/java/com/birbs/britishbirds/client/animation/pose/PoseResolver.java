package com.birbs.britishbirds.client.animation.pose;

import com.birbs.britishbirds.client.animation.BirdJoint;
import com.birbs.britishbirds.client.animation.BirdSkeleton;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-entity resolver that maps entity state to blended target angles per joint.
 *
 * <p>Each bird entity has its own PoseResolver instance (managed by AbstractBirdModel
 * in a map keyed by entity ID). The resolver is NOT thread-safe and must only be
 * accessed from the render thread.
 *
 * <p>Resolution order each frame (inside {@link #resolve}):
 * <ol>
 *   <li>Blend previous and current base poses by {@code transitionWeight}.
 *   <li>Advance the transition weight toward 1.0 using {@code transitionSpeed × deltaTime}.
 *   <li>Add the active cyclic offset (if any).
 *   <li>Add all overlay offsets scaled by their weights.
 *   <li>Write the result to each joint via {@link BirdJoint#setTarget}.
 *   <li>Apply any spring parameter overrides from the current base pose.
 * </ol>
 */
public class PoseResolver {

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** Pairs a PoseData with a blend weight (0–1). */
    public static final class ActiveOverlay {
        public final PoseData pose;
        public float weight;

        private ActiveOverlay(PoseData pose, float weight) {
            this.pose = pose;
            this.weight = weight;
        }
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private PoseData currentBasePose   = null;
    private PoseData previousBasePose  = null;
    private float    transitionWeight  = 1.0f;   // 1 = fully on current, no transition in progress
    private float    transitionSpeed   = 4.0f;   // units per second (default – overridden by setBasePose)

    private CyclicAnimation activeCyclic = null;
    private float            cyclicPhase  = 0.0f;

    private final List<ActiveOverlay> overlays = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Base pose control
    // -------------------------------------------------------------------------

    /**
     * Transitions to {@code pose}.  If {@code pose} is already current (compared by name),
     * this is a no-op.  Otherwise the current pose becomes the previous pose, the new pose
     * becomes current, and {@code transitionWeight} resets to 0 so the blend ramps from
     * old → new at {@code transitionSpeed} units/second.
     */
    public void setBasePose(PoseData pose, float transitionSpeed) {
        if (pose == null) return;

        // No-op if we are already on this pose
        if (currentBasePose != null && currentBasePose.getName().equals(pose.getName())) {
            return;
        }

        previousBasePose  = currentBasePose;
        currentBasePose   = pose;
        transitionWeight  = 0.0f;
        this.transitionSpeed = transitionSpeed;
    }

    // -------------------------------------------------------------------------
    // Cyclic animation control
    // -------------------------------------------------------------------------

    /**
     * Sets or replaces the active cyclic animation and its playback phase (0–1).
     */
    public void setActiveCyclic(CyclicAnimation cyclic, float phase) {
        this.activeCyclic = cyclic;
        this.cyclicPhase  = phase;
    }

    /** Removes the active cyclic animation. */
    public void clearCyclic() {
        this.activeCyclic = null;
        this.cyclicPhase  = 0.0f;
    }

    // -------------------------------------------------------------------------
    // Overlay control
    // -------------------------------------------------------------------------

    /**
     * Adds a new overlay or updates an existing one (matched by pose name).
     */
    public void addOverlay(PoseData overlay, float weight) {
        if (overlay == null) return;

        for (ActiveOverlay ao : overlays) {
            if (ao.pose.getName().equals(overlay.getName())) {
                ao.weight = weight;
                return;
            }
        }
        overlays.add(new ActiveOverlay(overlay, weight));
    }

    /**
     * Removes the overlay with the given pose name, if present.
     */
    public void removeOverlay(String poseName) {
        overlays.removeIf(ao -> ao.pose.getName().equals(poseName));
    }

    // -------------------------------------------------------------------------
    // Resolution
    // -------------------------------------------------------------------------

    /**
     * Resolves all active animation layers onto the skeleton joints.
     *
     * @param skeleton  the bird skeleton whose joints will be updated
     * @param deltaTime seconds since last frame
     */
    public void resolve(BirdSkeleton skeleton, float deltaTime) {
        // Step 2: Advance the transition weight before we use it, so that the
        // very first frame after a transition begins already shows partial blend.
        transitionWeight = Math.min(1.0f, transitionWeight + transitionSpeed * deltaTime);

        for (BirdJoint joint : skeleton.getAllJoints()) {
            String name = joint.name;

            // ------------------------------------------------------------------
            // Step 1: Blend base poses
            // ------------------------------------------------------------------
            float baseX = 0f, baseY = 0f, baseZ = 0f;

            if (currentBasePose != null) {
                Vector3f cur = currentBasePose.getAngle(name);
                float curX = (cur != null) ? cur.x : 0f;
                float curY = (cur != null) ? cur.y : 0f;
                float curZ = (cur != null) ? cur.z : 0f;

                if (previousBasePose != null && transitionWeight < 1.0f) {
                    Vector3f prev = previousBasePose.getAngle(name);
                    float prevX = (prev != null) ? prev.x : 0f;
                    float prevY = (prev != null) ? prev.y : 0f;
                    float prevZ = (prev != null) ? prev.z : 0f;

                    float w = transitionWeight;
                    baseX = prevX * (1f - w) + curX * w;
                    baseY = prevY * (1f - w) + curY * w;
                    baseZ = prevZ * (1f - w) + curZ * w;
                } else {
                    baseX = curX;
                    baseY = curY;
                    baseZ = curZ;
                }
            }
            // If currentBasePose is null, base remains (0, 0, 0).

            float angleX = baseX;
            float angleY = baseY;
            float angleZ = baseZ;

            // ------------------------------------------------------------------
            // Step 3: Add cyclic offset
            // ------------------------------------------------------------------
            if (activeCyclic != null) {
                Vector3f cyclicOffset = activeCyclic.getBlendedOffset(name, cyclicPhase);
                if (cyclicOffset != null) {
                    angleX += cyclicOffset.x;
                    angleY += cyclicOffset.y;
                    angleZ += cyclicOffset.z;
                }
            }

            // ------------------------------------------------------------------
            // Step 4: Add overlay offsets
            // ------------------------------------------------------------------
            for (ActiveOverlay ao : overlays) {
                Vector3f ov = ao.pose.getAngle(name);
                if (ov != null) {
                    float w = ao.weight;
                    angleX += ov.x * w;
                    angleY += ov.y * w;
                    angleZ += ov.z * w;
                }
            }

            // ------------------------------------------------------------------
            // Step 5: Write target angles
            // ------------------------------------------------------------------
            joint.setTarget(angleX, angleY, angleZ);

            // ------------------------------------------------------------------
            // Step 6: Apply spring overrides from the current base pose
            // ------------------------------------------------------------------
            if (currentBasePose != null) {
                PoseData.SpringOverride so = currentBasePose.getSpringOverride(name);
                if (so != null) {
                    // Preserve the existing maxVelocity; only stiffness and damping are overridden.
                    joint.setSpring(so.stiffness(), so.damping(), joint.maxVelocity);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Accessors (for debugging / testing)
    // -------------------------------------------------------------------------

    public PoseData getCurrentBasePose()  { return currentBasePose;  }
    public PoseData getPreviousBasePose() { return previousBasePose; }
    public float    getTransitionWeight() { return transitionWeight; }
    public CyclicAnimation getActiveCyclic() { return activeCyclic; }
    public float    getCyclicPhase()      { return cyclicPhase;      }
    public List<ActiveOverlay> getOverlays() { return overlays;      }
}
