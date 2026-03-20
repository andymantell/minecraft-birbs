# Skeletal Animation System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace hard-coded bird animations with a spring-damper skeletal system that produces smooth, organic motion across all species.

**Architecture:** A 32-joint skeleton with per-joint spring physics drives ModelPart rotations. A pose resolver selects target poses from entity state, procedural behaviours add reactive offsets (head tracking, breathing, drag, etc.), and a spring solver smooths everything into fluid motion. Per-entity skeleton state is stored in a map keyed by entity ID since Minecraft model instances are shared singletons.

**Tech Stack:** Fabric mod for Minecraft Java Edition 26.1-rc-1, vanilla `ModelPart` system, JOML `Vector3f`, no external animation libraries.

**Spec:** `docs/superpowers/specs/2026-03-20-skeletal-animation-system-design.md`

**Build command:** `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`

**Run command:** `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew runClient`

**No unit test framework exists.** Testing is done by compiling (`./gradlew build`) and running in-game (`./gradlew runClient`). Each task must compile cleanly before committing.

**Important API note:** This is MC 26.1 with Mojang mappings. Class names differ from Yarn/tutorials — see the API gotchas table in CLAUDE.md. **Before using any Minecraft class field (e.g. `ageInTicks`, `walkAnimationSpeed`, `walkAnimationPos` on render state), verify the actual field name in MC 26.1 by reading the decompiled source.** These fields exist on `LivingEntityRenderState` but may have different names in Mojang mappings.

**Commit convention:** Do NOT auto-commit. Pause after each task and let the user decide when to commit. The commit commands in this plan are suggestions for when the user chooses to commit.

---

## File Structure

### New Files (client-side animation framework)

```
src/client/java/com/birbs/britishbirds/client/animation/
  BirdJoint.java              — Single joint: angles, velocity, spring params, constraints
  BirdSkeleton.java           — Joint tree with 32 joints, factory method, joint lookup
  BirdSkeletonState.java      — Per-entity persistent state (angles, velocities, prev frame time)
  SpringSolver.java           — Per-joint spring-damper tick: acceleration → velocity → angle
  SkeletonModelMapper.java    — Maps joint names to ModelPart references, writes solved angles

src/client/java/com/birbs/britishbirds/client/animation/pose/
  PoseData.java               — Named set of target joint angles, builder with .mirror()
  CyclicAnimation.java        — Two offset sub-poses oscillated by phase (wingbeat, walk, etc.)
  PoseResolver.java           — Entity state → weighted pose stack → blended target angles
  BaseBirdPoses.java          — Shared poses: perched, alert, flying_cruise, flying_takeoff, flying_land, tucked
  PasserinePoses.java         — Family poses: hop, flutter, forage + wingbeat/walk/peck cyclics
  RaptorPoses.java            — Family poses: soar, stoop, hover, strike + wingbeat cyclics
  WaterfowlPoses.java         — Family poses: swim, dabble, waddle + wingbeat/paddle/waddle cyclics

src/client/java/com/birbs/britishbirds/client/animation/procedural/
  ProceduralBehaviour.java    — Interface: apply(skeleton, renderState, deltaTime)
  HeadTracking.java           — Neck chain IK toward look target
  Breathing.java              — Subtle chest expansion cycle
  LandingImpact.java          — One-shot velocity impulse on touchdown
  MovementDrag.java           — Tail/wingtip trailing on turns/dives
  StartleResponse.java        — Whole-body flinch on panic
  WeightShift.java            — Walking lean + perched idle sway
```

### New Files (model infrastructure)

```
src/client/java/com/birbs/britishbirds/client/model/
  AbstractBirdModel.java      — Base model: owns skeleton, state map, mapper; setupAnim pipeline
  BirdUVLayout.java           — Per-species UV offset definitions (shared with TextureGenerator)
```

### Modified Files

```
src/client/java/com/birbs/britishbirds/client/model/
  RobinModel.java             — Full rebuild: nested hierarchy, 32+ cuboids, skeleton binding
  BlueTitModel.java           — Full rebuild
  BarnOwlModel.java           — Full rebuild
  PeregrineFalconModel.java   — Full rebuild
  MallardModel.java           — Full rebuild
  BirdModelLayers.java        — No change (layer registration stays the same)

src/client/java/com/birbs/britishbirds/client/renderer/
  BirdRenderState.java        — Add deltaTime, yawDelta, verticalVelocity, speed, justLanded, justStartled, lookTarget
  AbstractBirdRenderer.java   — Extract new render state fields in extractRenderState()
  RobinRenderer.java          — Update flapFrequency/flapAmplitude (may simplify)
  BlueTitRenderer.java        — Update
  BarnOwlRenderer.java        — Update
  PeregrineFalconRenderer.java — Update
  MallardRenderer.java        — Update

tools/TextureGenerator.java   — 512×512, new UV layout, virtual body painting
```

### Retired Files

```
src/client/java/com/birbs/britishbirds/client/model/BirdAnimations.java  — Fully replaced by skeleton
```

---

## Task 1: BirdJoint and BirdSkeleton

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/BirdJoint.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/BirdSkeleton.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/BirdSkeletonState.java`

- [ ] **Step 1: Create BirdJoint**

The fundamental joint class. Fields: name, parent, current angles (x/y/z), velocities (x/y/z), targets (x/y/z), spring params (stiffness, damping, maxVelocity), constraints (min/max per axis). Constructor takes name + parent. Methods: `setTarget(x, y, z)`, `setSpring(stiffness, damping, maxVelocity)`, `setConstraints(minX, maxX, minY, maxY, minZ, maxZ)`, `clampAngles()` (applies constraints).

```java
package com.birbs.britishbirds.client.animation;

public class BirdJoint {
    public final String name;
    public final BirdJoint parent;

    // Current state
    public float angleX, angleY, angleZ;
    public float velX, velY, velZ;

    // Targets (set by pose resolver + procedural layer)
    public float targetX, targetY, targetZ;

    // Spring parameters
    public float stiffness = 50.0f;
    public float damping = 8.0f;
    public float maxVelocity = 10.0f;

    // Constraints (radians)
    public float minX = -3.14f, maxX = 3.14f;
    public float minY = -3.14f, maxY = 3.14f;
    public float minZ = -3.14f, maxZ = 3.14f;

    public BirdJoint(String name, BirdJoint parent) {
        this.name = name;
        this.parent = parent;
    }

    public void setTarget(float x, float y, float z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public void setSpring(float stiffness, float damping, float maxVelocity) {
        this.stiffness = stiffness;
        this.damping = damping;
        this.maxVelocity = maxVelocity;
    }

    public void setConstraints(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.minZ = minZ; this.maxZ = maxZ;
    }

    public void clampAngles() {
        angleX = Math.clamp(angleX, minX, maxX);
        angleY = Math.clamp(angleY, minY, maxY);
        angleZ = Math.clamp(angleZ, minZ, maxZ);
    }
}
```

- [ ] **Step 2: Create BirdSkeletonState**

Per-entity persistent state. Flat arrays for angles and velocities (32 joints × 3 axes = 96 floats each). Includes `prevAgeAndPartial` for deltaTime computation.

```java
package com.birbs.britishbirds.client.animation;

public class BirdSkeletonState {
    public static final int JOINT_COUNT = 32;
    public static final int AXIS_COUNT = 3;
    public static final int ARRAY_SIZE = JOINT_COUNT * AXIS_COUNT;

    public final float[] angles = new float[ARRAY_SIZE];
    public final float[] velocities = new float[ARRAY_SIZE];
    public float prevAgeAndPartial = -1.0f; // -1 = uninitialised
    public long lastRenderedTick = 0;       // for stale state cleanup
}
```

- [ ] **Step 3: Create BirdSkeleton**

The joint tree. A static factory method `createUniversal()` builds the full 32-joint hierarchy with default spring tuning per region. Provides `getJoint(String name)` for lookup, `getAllJoints()` for iteration, and `getJointIndex(String name)` for state array indexing.

Joint names as constants. The factory wires up the full tree from the spec:
- chest (root) → neck chain (3 + head + 2 beaks) → shoulder_mount → wings (L/R × 6) → torso → hip → legs (L/R × 4) → tail (2).

Apply default spring tuning from the spec table (neck: 80/12/15, head: 70/10/12, spine: 50/8/8, wing bones: 40/5/10, trailing: 15/2/8, legs: 60/9/10, feet: 45/7/8, tail: 20/3/6).

Methods: `loadState(BirdSkeletonState)` — copies state arrays into joint fields. `saveState(BirdSkeletonState)` — copies joint fields back to state arrays.

- [ ] **Step 4: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL. These classes have no Minecraft dependencies yet (only JOML Vector3f if used), so should compile cleanly.

- [ ] **Step 5: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/BirdJoint.java
git add src/client/java/com/birbs/britishbirds/client/animation/BirdSkeleton.java
git add src/client/java/com/birbs/britishbirds/client/animation/BirdSkeletonState.java
git commit -m "Add BirdJoint, BirdSkeleton, and BirdSkeletonState"
```

---

## Task 2: SpringSolver

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/SpringSolver.java`

- [ ] **Step 1: Implement SpringSolver**

Static utility class with a single method `solve(BirdJoint joint, float deltaTime)` that applies the spring-damper equation per axis:

```
acceleration = stiffness × (target - current) - damping × velocity
velocity += acceleration × deltaTime
velocity = clamp(velocity, -maxVelocity, maxVelocity)
current += velocity × deltaTime
```

Then calls `joint.clampAngles()`. Also a convenience method `solveAll(List<BirdJoint> joints, float deltaTime)` that iterates all joints.

Guard against bad deltaTime: if `deltaTime <= 0` or `deltaTime > 0.5f` (would mean 10+ game ticks elapsed — likely a lag spike), clamp to 0.05f (one tick) to prevent spring explosion.

```java
package com.birbs.britishbirds.client.animation;

import java.util.List;

public class SpringSolver {

    public static void solve(BirdJoint joint, float deltaTime) {
        // Clamp deltaTime for stability
        if (deltaTime <= 0.0f) return;
        if (deltaTime > 0.5f) deltaTime = 0.05f;

        float s = joint.stiffness;
        float d = joint.damping;
        float mv = joint.maxVelocity;

        // X axis
        float accX = s * (joint.targetX - joint.angleX) - d * joint.velX;
        joint.velX = Math.clamp(joint.velX + accX * deltaTime, -mv, mv);
        joint.angleX += joint.velX * deltaTime;

        // Y axis
        float accY = s * (joint.targetY - joint.angleY) - d * joint.velY;
        joint.velY = Math.clamp(joint.velY + accY * deltaTime, -mv, mv);
        joint.angleY += joint.velY * deltaTime;

        // Z axis
        float accZ = s * (joint.targetZ - joint.angleZ) - d * joint.velZ;
        joint.velZ = Math.clamp(joint.velZ + accZ * deltaTime, -mv, mv);
        joint.angleZ += joint.velZ * deltaTime;

        joint.clampAngles();
    }

    public static void solveAll(List<BirdJoint> joints, float deltaTime) {
        for (BirdJoint joint : joints) {
            solve(joint, deltaTime);
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/SpringSolver.java
git commit -m "Add SpringSolver with per-joint spring-damper physics"
```

---

## Task 3: PoseData and CyclicAnimation

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/PoseData.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/CyclicAnimation.java`

- [ ] **Step 1: Implement PoseData**

Fields: `String name`, `Map<String, Vector3f> jointAngles`, `Map<String, SpringOverride> springOverrides`. Inner record `SpringOverride(float stiffness, float damping)`.

Builder class with:
- `.joint(String name, float x, float y, float z)` — adds a joint angle entry
- `.spring(String name, float stiffness, float damping)` — adds a spring override
- `.mirror()` — copies all `L_` prefixed entries to `R_` equivalents, flipping the sign on Y and Z rotations (yaw and roll mirror, pitch doesn't)
- `.build()` — returns immutable PoseData

Methods on PoseData:
- `getAngle(String jointName)` — returns Vector3f or null if joint not in this pose
- `getSpringOverride(String jointName)` — returns SpringOverride or null
- `hasJoint(String jointName)` — true if this pose specifies that joint

Use `org.joml.Vector3f` for the angle triples. Make the maps unmodifiable in build().

- [ ] **Step 2: Implement CyclicAnimation**

Fields: `String name`, `PoseData offsetA`, `PoseData offsetB`. Both sub-poses define **offsets from zero** (not absolute angles).

Method: `getBlendedOffset(String jointName, float phase)` — returns `lerp(offsetA.getAngle(joint), offsetB.getAngle(joint), phase)` as a Vector3f, or null if joint not in either sub-pose. If joint is in one sub-pose but not the other, treat the missing side as zero (Vector3f.ZERO). Phase is 0.0-1.0.

- [ ] **Step 3: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/pose/PoseData.java
git add src/client/java/com/birbs/britishbirds/client/animation/pose/CyclicAnimation.java
git commit -m "Add PoseData with builder/mirror and CyclicAnimation"
```

---

## Task 4: PoseResolver

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/PoseResolver.java`

- [ ] **Step 1: Implement PoseResolver**

The resolver maps entity state to a blended set of target angles per joint. It manages:
- A `currentBasePose` and `previousBasePose` with a `transitionWeight` (0→1 ramp)
- A list of active `CyclicAnimation` entries with their current phase
- A list of active overlays with their weights

Inner class `ActiveCyclic` holds a CyclicAnimation + phase (float 0-1).
Inner class `ActiveOverlay` holds a PoseData + weight (float 0-1).

Methods:
- `setBasePose(PoseData pose, float transitionSpeed)` — if different from current, starts a transition (previous = current, current = new, weight resets to 0, transitionSpeed stored)
- `setActiveCyclic(CyclicAnimation cyclic, float phase)` — sets or updates the active cyclic
- `clearCyclic()` — removes active cyclic
- `addOverlay(PoseData overlay, float weight)` — adds/updates an overlay
- `removeOverlay(String poseName)` — removes overlay
- `resolve(BirdSkeleton skeleton, float deltaTime)` — for each joint in skeleton:
  1. Blend base poses: `angle = prev.angle × (1-transitionWeight) + current.angle × transitionWeight`. If a joint is missing from a pose, use 0.
  2. Advance transition: `transitionWeight = min(1.0, transitionWeight + transitionSpeed × deltaTime)`
  3. Add cyclic offset if active: `angle += cyclic.getBlendedOffset(joint, phase)`
  4. Add overlay offsets: `angle += overlay.angle × overlay.weight`
  5. Set as joint target: `joint.setTarget(angle.x, angle.y, angle.z)`
  6. Apply any spring overrides from the current base pose

- [ ] **Step 2: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/pose/PoseResolver.java
git commit -m "Add PoseResolver with base pose transitions, cyclics, and overlays"
```

---

## Task 5: SkeletonModelMapper

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/SkeletonModelMapper.java`

- [ ] **Step 1: Implement SkeletonModelMapper**

Simple class that binds joint names to `ModelPart` references and writes solved angles.

Fields: `Map<String, ModelPart> bindings`.

Builder pattern: `.bind(String jointName, ModelPart part)`, `.build()`.

Method: `apply(BirdSkeleton skeleton)` — iterates bindings, looks up each joint by name, copies `joint.angleX/Y/Z` to `part.xRot/yRot/zRot`.

Import `net.minecraft.client.model.geom.ModelPart`.

```java
package com.birbs.britishbirds.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import java.util.HashMap;
import java.util.Map;

public class SkeletonModelMapper {
    private final Map<String, ModelPart> bindings;

    private SkeletonModelMapper(Map<String, ModelPart> bindings) {
        this.bindings = Map.copyOf(bindings);
    }

    public void apply(BirdSkeleton skeleton) {
        for (var entry : bindings.entrySet()) {
            BirdJoint joint = skeleton.getJoint(entry.getKey());
            if (joint != null) {
                ModelPart part = entry.getValue();
                part.xRot = joint.angleX;
                part.yRot = joint.angleY;
                part.zRot = joint.angleZ;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, ModelPart> bindings = new HashMap<>();

        public Builder bind(String jointName, ModelPart part) {
            bindings.put(jointName, part);
            return this;
        }

        public SkeletonModelMapper build() {
            return new SkeletonModelMapper(bindings);
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/SkeletonModelMapper.java
git commit -m "Add SkeletonModelMapper for joint-to-ModelPart binding"
```

---

## Task 6: BirdRenderState and AbstractBirdRenderer Updates

**Do this BEFORE procedural behaviours** — the behaviours reference render state fields that must exist first.

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/renderer/BirdRenderState.java`
- Modify: `src/client/java/com/birbs/britishbirds/client/renderer/AbstractBirdRenderer.java`

- [ ] **Step 1: Add new fields to BirdRenderState**

Add these fields to the existing class (keep existing fields):

```java
public float deltaTime = 0.05f;
public float yawDelta = 0.0f;
public float verticalVelocity = 0.0f;
public float speed = 0.0f;
public boolean justLanded = false;
public boolean justStartled = false;
public net.minecraft.world.phys.Vec3 lookTarget = null;
public int entityId = 0;
```

The `entityId` field is needed by the model to look up per-entity skeleton state.

- [ ] **Step 2: Update AbstractBirdRenderer.extractRenderState()**

Read the current implementation first to understand the exact method signature and existing logic. Add extraction of new fields after the existing extraction code:

- `state.entityId = entity.getId()`
- `state.yawDelta = entity.getYRot() - entity.yRotO`
- `state.verticalVelocity = (float) entity.getDeltaMovement().y`
- `state.speed = (float) entity.getDeltaMovement().horizontalDistance()`
- `state.justLanded = !entity.isFlying() && entity.onGround() && state.isFlying` (edge-detected)
- `state.justStartled = false` (stub — species extractors will set this when panic goal active)
- `state.lookTarget = findLookTarget(entity)` — new helper method

For `findLookTarget()`: query `entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0), e -> e != entity)`, find nearest, return its position or null. **Throttle this:** only recompute every 5 ticks (use `entity.tickCount % 5 == 0`), cache the result on the renderer or pass through on the entity.

Note on `deltaTime`: set to 0.05f default here. The model will compute the precise value per-entity using `BirdSkeletonState.prevAgeAndPartial`.

- [ ] **Step 3: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/renderer/BirdRenderState.java
git add src/client/java/com/birbs/britishbirds/client/renderer/AbstractBirdRenderer.java
git commit -m "Add skeletal animation fields to BirdRenderState and renderer extraction"
```

---

## Task 7: Procedural Behaviours

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/ProceduralBehaviour.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/Breathing.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/HeadTracking.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/WeightShift.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/LandingImpact.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/MovementDrag.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/procedural/StartleResponse.java`

- [ ] **Step 1: Create ProceduralBehaviour interface**

```java
package com.birbs.britishbirds.client.animation.procedural;

import com.birbs.britishbirds.client.animation.BirdSkeleton;
import com.birbs.britishbirds.client.renderer.BirdRenderState;

public interface ProceduralBehaviour {
    void apply(BirdSkeleton skeleton, BirdRenderState state, float deltaTime);
}
```

- [ ] **Step 2: Implement Breathing**

Constructor takes `float breathRate` (default 0.15f for small birds, 0.05f for large). In `apply()`:
- Compute `breathPhase = state.ageInTicks * breathRate`
- `expansion = sin(breathPhase) * 0.015f`
- Add to chest.targetX, subtract half from torso.targetX
- Add to L_scapulars.targetZ (negative) and R_scapulars.targetZ (positive) at 0.3× scale

- [ ] **Step 3: Implement HeadTracking**

Constructor takes `float maxYaw` (default 1.5 radians), `float maxPitch` (default 0.8), `float scanInterval` (ticks between random gaze breaks). In `apply()`:
- If `state.lookTarget` is null, return
- Compute yaw and pitch from bird's position to lookTarget (using state fields)
- Clamp to maxYaw/maxPitch
- Distribute across neck chain per spec ratios: neck_lower 20%, neck_mid 30%, neck_upper 25% yaw + 40% pitch, head 25% yaw + 60% pitch
- Add as offsets to existing targets

- [ ] **Step 4: Implement WeightShift**

Constructor takes `float swayAmplitude` (default 0.002f). In `apply()`:
- If walking (state.walkAnimationSpeed > 0.01f): compute lean from `sin(walkPos × 0.6662f) × walkSpeed × 0.08f`, add to chest.targetZ and half to hip.targetZ
- If idle: use noise-like function `sin(ageInTicks × 0.023f) × sin(ageInTicks × 0.037f) × swayAmplitude` (two incommensurate frequencies ≈ aperiodic), add to chest.targetZ and half to chest.targetX

- [ ] **Step 5: Implement LandingImpact**

In `apply()`: if `state.justLanded` is true, inject velocity impulses:
- chest.velX += 2.0f, hip.velX += 1.5f
- tail_base.velX -= 3.0f, tail_fan.velX -= 2.0f
- L_shin.velX += 2.5f, R_shin.velX += 2.5f
- L_upper_wing.velZ -= 4.0f, R_upper_wing.velZ += 4.0f

These are one-shot — `justLanded` is only true for one frame (edge-detected in renderer).

- [ ] **Step 6: Implement MovementDrag**

In `apply()`:
- `tail_base.targetY -= state.yawDelta × 0.4f`
- `tail_fan.targetY -= state.yawDelta × 0.6f`
- `tail_base.targetX -= state.verticalVelocity × 0.8f`
- `L_primaries.targetZ -= state.speed × 0.3f`
- `R_primaries.targetZ += state.speed × 0.3f`
- `L_secondaries.targetZ -= state.speed × 0.2f`
- `R_secondaries.targetZ += state.speed × 0.2f`

- [ ] **Step 7: Implement StartleResponse**

In `apply()`: if `state.justStartled` is true, inject velocity impulses:
- Compute rough yaw toward threat from `state.lookTarget` (if available)
- head.velY += impulse toward threat direction × 8.0f
- chest.velX += 3.0f, hip.velX += 2.0f
- L_upper_wing.velZ -= 5.0f, R_upper_wing.velZ += 5.0f
- tail_fan.velX -= 3.0f

- [ ] **Step 8: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL. BirdRenderState was updated in Task 6 so all referenced fields exist.

- [ ] **Step 9: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/procedural/
git commit -m "Add 6 procedural behaviours: breathing, head tracking, weight shift, landing, drag, startle"
```

---

## Task 8: AbstractBirdModel Base Class

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/model/AbstractBirdModel.java`

- [ ] **Step 1: Implement AbstractBirdModel**

Generic base: `AbstractBirdModel<S extends BirdRenderState> extends EntityModel<S>`.

Fields:
- `protected final BirdSkeleton skeleton` — created via `BirdSkeleton.createUniversal()`
- `private final Map<Integer, BirdAnimationState> stateMap` — per-entity state, keyed by entity ID
- `protected SkeletonModelMapper mapper` — set by subclass via `initSkeleton()`
- `protected final List<ProceduralBehaviour> behaviours` — composed by subclass via `initSkeleton()`

**`BirdAnimationState`** (new inner class or separate class) bundles per-entity state:
```java
class BirdAnimationState {
    BirdSkeletonState skeletonState;  // angles, velocities
    PoseResolver poseResolver;         // current/previous pose, transition weight, cyclics, overlays
    long lastRenderedTick;             // for stale cleanup
}
```

**Critical: PoseResolver is per-entity, not shared.** Since model instances are singletons (one RobinModel renders all robins), pose resolver state (current pose, transition progress, active cyclics) must be per-entity. Each entity gets its own `PoseResolver` inside `BirdAnimationState`.

Constructor: takes `ModelPart root`. Creates skeleton, empty state map, empty behaviour list. Does NOT call abstract methods (calling abstract methods from constructors is an anti-pattern — subclass fields aren't initialized yet).

**Initialization pattern:** Subclass constructors must call `initSkeleton(root)` at the END of their constructor, AFTER storing their own ModelPart field references:

```java
// In RobinModel constructor:
public RobinModel(ModelPart root) {
    super(root);
    this.chest = root.getChild("chest");
    this.head = root.getChild("chest").getChild("neck_lower")...;
    // ... all field assignments ...
    initSkeleton(root);  // LAST — builds mapper and configures behaviours
}
```

`initSkeleton(ModelPart root)` is a final method on AbstractBirdModel that calls:
1. `buildMapper(root)` — abstract, subclass creates SkeletonModelMapper
2. `configureBehaviours()` — abstract, subclass adds ProceduralBehaviours to the list

Method `setupAnim(S state)`:
1. Look up or create `BirdAnimationState` for `state.entityId`
2. Load skeleton state: `skeleton.loadState(animState.skeletonState)`
3. Compute precise deltaTime: `float dt = computeDeltaTime(state.ageInTicks, animState.skeletonState)`
4. Reset all joint targets to 0
5. `animState.poseResolver.resolve(skeleton, dt)` — per-entity resolver sets targets from poses
6. For each behaviour: `behaviour.apply(skeleton, state, dt)`
7. `SpringSolver.solveAll(skeleton.getAllJoints(), dt)`
8. `mapper.apply(skeleton)` — write to ModelParts
9. Save state back: `skeleton.saveState(animState.skeletonState)`
10. `animState.lastRenderedTick = currentTick`

**Subclass accesses the per-entity resolver** via `getResolver(S state)` helper that looks up the entity's `BirdAnimationState.poseResolver`. The species `selectPoses()` method uses this.

Helper `computeDeltaTime(float ageInTicks, BirdSkeletonState state)`:
- If `state.prevAgeAndPartial < 0` (first frame), return 0.05f and set prev
- Else compute `dt = ageInTicks - state.prevAgeAndPartial`, clamp to [0.001, 0.5], update prev, return dt

Periodic stale state cleanup: in `setupAnim()`, every 200 calls (cheap counter), remove entries where `lastRenderedTick` is 100+ ticks behind current. This prevents memory leaks from entities that leave render distance.

Abstract method: `protected abstract void buildMapper(ModelPart root)` — subclass creates SkeletonModelMapper and sets `this.mapper`.
Abstract method: `protected abstract void configureBehaviours()` — subclass adds behaviours to `this.behaviours`.

- [ ] **Step 2: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/model/AbstractBirdModel.java
git commit -m "Add AbstractBirdModel base class with skeleton pipeline"
```

---

## Task 9: Base Bird Poses

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/BaseBirdPoses.java`

- [ ] **Step 1: Define shared poses**

Static final PoseData constants for poses shared by all birds. Use the builder with `.mirror()` for symmetric joints. Refer to the current hard-coded angles in the existing models as starting points, but adapt for the new joint hierarchy.

Poses to define:
- `PERCHED` — slight forward lean on chest, neck S-curve, wings Z-folded (upper_wing out slightly, forearm tucked back, hand folded forward), tail slightly uptilted, legs straight
- `ALERT` — chest more upright, neck extended taller, head level, tail flat
- `FLYING_CRUISE` — chest pitched forward (-0.6), neck compensates up, wings spread at cruise angle, legs tucked back, tail extended
- `FLYING_TAKEOFF` — chest pitched steeply forward, wings fully spread, legs trailing, tail fanned
- `FLYING_LAND` — chest pitched back (flaring), wings spread wide, legs extended forward, tail fanned down as air brake
- `TUCKED` — chest settled low, head tucked into shoulder, wings tight, tail down

Cyclic animations (offsets from zero):
- `WINGBEAT` — CyclicAnimation with wings_up (upper_wing.zRot offset negative/lifted, forearm follows, hand trails) and wings_down (opposite). Only defines wing joints.
- `WALK_CYCLE` — CyclicAnimation with legs_forward/legs_back. Only defines leg joints.

Overlays:
- `BEAK_OPEN` — lower_beak.xRot = 0.4f
- `LEGS_TUCKED` — all leg joints rotated to trail behind body

Exact angles will need in-game tuning, so use reasonable starting values from the current model code and expect iteration.

- [ ] **Step 2: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/animation/pose/BaseBirdPoses.java
git commit -m "Add base bird poses: perched, alert, flying, tucked, wingbeat cycle, walk cycle"
```

---

## Task 10: BirdUVLayout and TextureGenerator Update

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/model/BirdUVLayout.java`
- Modify: `tools/TextureGenerator.java`

- [ ] **Step 1: Create BirdUVLayout**

A class that defines UV texture offsets for the universal 32-cuboid layout. Each species provides cuboid dimensions (width, height, depth per part); this class computes the `texOffs(u, v)` packing into a 512×512 sheet.

Static method `computeLayout(Map<String, int[]> cuboidDimensions)` returns a `Map<String, int[]>` of joint name → (u, v) texture offset. Uses a simple row-packing algorithm to fit all faces.

Also provides `getTextureWidth()` and `getTextureHeight()` returning 512.

- [ ] **Step 2: Update TextureGenerator for 512×512**

Read the current TextureGenerator first. Key changes:
- Output size changes from 64×64 to 512×512
- UV regions change to match BirdUVLayout packing
- Virtual body painting: create a continuous colour field (gradient map per species) at full body dimensions, then sample from it per-cuboid-face to ensure seam colour continuity
- Update all per-species paint methods (robin breast, blue tit yellow, owl disc, mallard green head, etc.) to reference new cuboid names and UV positions

This is a significant rewrite of TextureGenerator. Read the current implementation first and preserve the per-species colour definitions while changing the underlying UV mapping and resolution.

- [ ] **Step 3: Run TextureGenerator to regenerate all textures**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" java tools/TextureGenerator.java`
(Adjust command based on how TextureGenerator is currently invoked — read the file to check.)
Expected: New 512×512 .png files generated in `src/main/resources/assets/britishbirds/textures/entity/`.

- [ ] **Step 4: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/model/BirdUVLayout.java
git add tools/TextureGenerator.java
git add src/main/resources/assets/britishbirds/textures/
git commit -m "Update texture system to 512x512 with new UV layout for skeletal cuboids"
```

---

## Task 11: Rebuild RobinModel (First Species Migration)

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/RobinModel.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/PasserinePoses.java`

This is the most important task — it's the template for all other species. Get this right and the others follow.

- [ ] **Step 1: Create PasserinePoses**

Passerine family poses (extends base poses):
- `HOP` — CyclicAnimation: crouch (legs bend, body drops) ↔ spring (legs extend, body rises). Only leg + chest joints.
- `FLUTTER` — Similar to WINGBEAT but faster frequency hint (species config controls actual speed). May just reuse base WINGBEAT.
- `FORAGE` — Static pose: body tilted forward, head angled down
- `PECK` — CyclicAnimation: head down + beak open ↔ head up + beak closed

Overlays:
- `HEAD_TILT` — head.yRot and head.zRot for the characteristic side-cock

- [ ] **Step 2: Rebuild RobinModel.createBodyLayer()**

Read the current RobinModel first. Replace the flat ModelPart hierarchy with the full nested skeleton hierarchy. Every cuboid must be a child of its skeleton parent in the ModelPart tree.

The Robin's universal skeleton cuboid sizes (approximate — will need visual tuning):
- chest: 3×3×3 (front body)
- shoulder_mount: 2×2×2 (small, at wing attachment point)
- torso: 3×3×3 (mid body)
- hip: 2×2×2 (rear body)
- neck_lower, neck_mid, neck_upper: 2×2×1 each (small cylinders)
- head: 4×4×4 (large for robin proportions)
- upper_beak: 1×0.5×1.5, lower_beak: 1×0.5×1.5
- L/R_upper_wing: 1×3×3, L/R_scapulars: 1×2×2
- L/R_forearm: 1×3×2, L/R_secondaries: 1×2×2
- L/R_hand: 1×2×2, L/R_primaries: 1×2×2
- tail_base: 2×1×2, tail_fan: 2×1×2
- L/R_thigh: 1×1×1, L/R_shin: 1×3×1, L/R_tarsus: 1×2×1, L/R_foot: 2×0.5×2

Decorative children (not skeleton-driven, ride with parent):
- breast patch: child of chest, 3×2×2 (Robin's red breast)
- crown: child of head, 3×1×3

UV offsets come from BirdUVLayout. Texture size 512×512. **Important:** The `createBodyLayer()` method's `LayerDefinition.create(meshDef, width, height)` call must change from `64, 64` to `512, 512` to match the new texture resolution.

- [ ] **Step 3: Implement RobinModel constructor and buildMapper()**

Store all ModelPart references. In `buildMapper()`, create a SkeletonModelMapper binding every skeleton joint name to its corresponding ModelPart.

- [ ] **Step 4: Implement RobinModel.configurePoses()**

Register with the PoseResolver:
- Base poses from BaseBirdPoses
- Passerine family poses from PasserinePoses
- Robin-specific: territorial puff overlay (chest slightly expanded, head raised)

Add procedural behaviours:
- Breathing (breathRate = 0.15f — small bird, fast)
- HeadTracking (twitchy — low scanInterval)
- WeightShift
- LandingImpact
- MovementDrag
- StartleResponse

- [ ] **Step 5: Implement RobinModel.setupAnim()**

Pose selection MUST happen BEFORE `super.setupAnim()` because the pipeline inside `super.setupAnim()` calls `poseResolver.resolve()`. Use `getResolver(state)` to get the per-entity resolver:

```java
@Override
public void setupAnim(RobinRenderState state) {
    // 1. Species-specific pose selection (before pipeline)
    selectPoses(state);
    // 2. Run skeleton pipeline (resolve, procedural, solve, map)
    super.setupAnim(state);
}

private void selectPoses(RobinRenderState state) {
    PoseResolver resolver = getResolver(state);  // per-entity resolver

    if (state.isFlying) {
        resolver.setBasePose(BaseBirdPoses.FLYING_CRUISE, 3.0f);
        resolver.setActiveCyclic(BaseBirdPoses.WINGBEAT, state.flapAngle);
        resolver.addOverlay(BaseBirdPoses.LEGS_TUCKED, 1.0f);
    } else if (state.isPecking) {
        resolver.setBasePose(PasserinePoses.FORAGE, 4.0f);
        resolver.setActiveCyclic(PasserinePoses.PECK, /* peck phase from state */);
        resolver.removeOverlay("legs_tucked");
    } else {
        resolver.setBasePose(BaseBirdPoses.PERCHED, 2.0f);
        resolver.removeOverlay("legs_tucked");
        if (state.walkAnimationSpeed > 0.01f) {
            resolver.setActiveCyclic(BaseBirdPoses.WALK_CYCLE, state.walkAnimationPos);
        } else {
            resolver.clearCyclic();
        }
    }
}
```

- [ ] **Step 6: Update RobinRenderer if needed**

Read current RobinRenderer. It may need updates to `extractSpeciesState()` for new fields, or may simplify since some state is now pose-driven. The `flapFrequency()` and `flapAmplitude()` methods may become unnecessary since the wingbeat cyclic handles this.

- [ ] **Step 7: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Test in-game**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew runClient`
Expected: Robin spawns, renders with new model. Check:
- Bird is visible and textured correctly (no pink/black missing texture)
- Springs are working — bird doesn't snap between poses
- Wing flapping is smooth with trailing edge lag
- Head tracks player when nearby
- Breathing visible as subtle chest movement when idle
- Walking shows weight shift
- Landing has impact bounce
- No crashes, no visual glitches

**This is the critical visual validation step.** Expect to iterate on cuboid sizes, spring tuning, and pose angles to get it looking right. Don't move to the next species until Robin looks good.

- [ ] **Step 9: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/model/RobinModel.java
git add src/client/java/com/birbs/britishbirds/client/animation/pose/PasserinePoses.java
git commit -m "Rebuild RobinModel with skeletal animation system"
```

---

## Task 12: Migrate Remaining Species

Each species follows the same pattern as Robin. Do them one at a time — compile and test each before moving to the next.

### Task 12a: BlueTitModel

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/BlueTitModel.java`

- [ ] **Step 1: Rebuild BlueTitModel.createBodyLayer()** — smaller cuboids than Robin (blue tit is tiny), very large head proportionally. Decorative: cheek patches (child of head), crown (child of head).
- [ ] **Step 2: Implement buildMapper() and configurePoses()** — uses PasserinePoses. Add hanging overlay (body inverted, `hanging` from spec). Acrobatic clinging spring config: very loose body springs for swinging upside-down.
- [ ] **Step 3: Update selectPoses()** — handle `isHangingUpsideDown` state.
- [ ] **Step 4: Compile and test in-game** — verify hanging animation, flocking, tiny rapid wingbeats.
- [ ] **Step 5: Commit**

### Task 12b: BarnOwlModel

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/BarnOwlModel.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/RaptorPoses.java`

- [ ] **Step 1: Create RaptorPoses** — soar (wings fully spread, minimal movement), stoop (wings tight, body angled down), hover (body vertical, rapid wing cycle), strike (legs extended). Wingbeat cyclic with slow deep strokes.
- [ ] **Step 2: Rebuild BarnOwlModel.createBodyLayer()** — large broad wings (scapulars and secondaries much bigger than passerine), elongated body with lowerBody segment mapped to hip, facial disc as decorative child of head. Very long wing cuboids.
- [ ] **Step 3: Implement buildMapper() and configurePoses()** — RaptorPoses. Owl-specific: quartering flight pose (body angled, head scanning down). Wing springs heavier/slower. Trailing feathers VERY loose for the characteristic silent owl wing flex.
- [ ] **Step 4: Update selectPoses()** — handle `isHovering` state, nocturnal head rotation.
- [ ] **Step 5: Compile and test in-game** — verify slow deep wingbeats with trailing edge ripple, hover behaviour, head rotation cycle.
- [ ] **Step 6: Commit**

### Task 12c: PeregrineFalconModel

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/PeregrineFalconModel.java`

- [ ] **Step 1: Rebuild PeregrineFalconModel.createBodyLayer()** — pointed wings (narrower, longer primaries), compact body. Uses RaptorPoses.
- [ ] **Step 2: configurePoses()** — soar (high circle), stoop (wings locked tight — spring override to very stiff), fast wingbeat. Stoop spring override is critical — wings must look rigid during dive.
- [ ] **Step 3: Compile and test in-game** — verify soaring, stoop dive with locked wings, hunting behaviour.
- [ ] **Step 4: Commit**

### Task 12d: MallardModel

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/MallardModel.java`
- Create: `src/client/java/com/birbs/britishbirds/client/animation/pose/WaterfowlPoses.java`

- [ ] **Step 1: Create WaterfowlPoses** — swim (body level, legs paddle alternately), dabble (body pitched 90° down, tail up), waddle cycle (body rolls side to side). Paddle cyclic (legs alternate). Direct flight wingbeat (fast, stiff).
- [ ] **Step 2: Rebuild MallardModel.createBodyLayer()** — heavy rounded body (larger chest/torso), thick neck (larger neck cuboids), broad flat bill (upper_beak and lower_beak wider/flatter), webbed feet (wider foot cuboids), drake tail curl as decorative child of tail_fan (visibility toggled by sex).
- [ ] **Step 3: configurePoses()** — WaterfowlPoses. Handle swimming, dabbling, waddling, flying states. Duck-specific waddle is the body rolling with heavier springs on legs.
- [ ] **Step 4: Compile and test in-game** — verify swimming with paddling, dabbling, waddle walk, drake tail curl visible on males, duckling follow.
- [ ] **Step 5: Commit**

---

## Task 13: Retire BirdAnimations and Clean Up

**Files:**
- Delete: `src/client/java/com/birbs/britishbirds/client/model/BirdAnimations.java`
- Modify: any files that still import BirdAnimations

- [ ] **Step 1: Search for remaining references to BirdAnimations**

Run: grep for `BirdAnimations` across the codebase. Should be zero if all models are migrated. If any remain, update them.

- [ ] **Step 2: Delete BirdAnimations.java**

- [ ] **Step 3: Clean up species-specific render states**

Check if `RobinRenderState.isPecking`, `BlueTitRenderState.isHangingUpsideDown`, `BarnOwlRenderState.isHovering`, `MallardRenderState.isDabbling/isSwimming/isWaddling` are still needed. They likely are — the pose selection logic in each model's `selectPoses()` reads these. Keep them.

Remove the `flapAngle` field from BirdRenderState if it's no longer used (wingbeat cyclic handles this now). Check all references first.

- [ ] **Step 4: Clean up AbstractBirdRenderer**

Remove `flapFrequency()`, `flapAmplitude()`, `shouldComputeFlap()` abstract methods if no longer called. Remove flapAngle computation from `extractRenderState()` if unused.

- [ ] **Step 5: Compile**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Full in-game test**

Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot" ./gradlew runClient`
Test ALL 5 species:
- Robin: perching, hopping, flying, pecking, head tracking, territorial puff
- Blue Tit: perching, hanging upside-down, flocking, rapid wingbeats
- Barn Owl: perching with head rotation, quartering flight, hovering, hunting
- Peregrine: soaring, stoop dive, hunting small birds
- Mallard: swimming, dabbling, waddling, flying, drake tail curl, duckling following

Check for:
- No visual glitches or missing textures
- Smooth transitions between all states
- Trailing edge wing flex visible on owls
- Spring physics looks organic (overshoot, settle, follow-through)
- Head tracking works on all species
- Breathing visible on all idle birds
- Landing impact bounce on all species
- No crashes, no memory leaks (watch for stale skeleton states)

- [ ] **Step 7: Commit**

```bash
git add src/client/java/com/birbs/britishbirds/client/  # review staged files before committing
git commit -m "Retire BirdAnimations, clean up renderers and render states"
```

---

## Task 14: Visual Tuning Pass

This task is iterative — no fixed steps. Run the client and tune:

- [ ] **Step 1: Spring tuning** — adjust stiffness/damping per species until motion feels right. Owl wings should feel heavy and fluid. Robin head should be snappy. Tail should be loose and draggy.
- [ ] **Step 2: Pose angles** — adjust target angles for each pose until birds look correct in each state. Wings should tuck neatly, legs should bend realistically at the ankle, neck should form a natural S-curve.
- [ ] **Step 3: Cuboid sizes** — adjust cuboid dimensions if proportions look wrong. Robin should be plump and round. Blue tit should have an oversized head. Owl should be elongated.
- [ ] **Step 4: Procedural tuning** — adjust breathing amplitude, head tracking range, landing impact strength, drag coefficients, weight shift magnitude.
- [ ] **Step 5: Transition speeds** — adjust pose transition rates. Startle → flight should be fast. Landing settle should be slow and gentle. State changes should feel natural.
- [ ] **Step 6: Compile and final test**
- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "Visual tuning pass: spring params, pose angles, cuboid proportions"
```
