# Skeletal Animation System Design

**Date:** 2026-03-20
**Status:** Draft
**Scope:** Replace hard-coded animation system with skeleton-driven spring-damper physics for all bird species

## Problem

The current animation system drives bird ModelPart rotations through discrete boolean states (isFlying, isPecking, etc.) with hard-coded angles and simple `sin(t)` oscillations in each model's `setupAnim()`. This produces four compounding problems:

1. **Hard state switches** — birds snap instantly between poses (grounded → flying)
2. **Mechanical oscillation** — all motion is simple sinusoidal, reads as robotic
3. **No secondary motion** — head, tail, and wings don't react to momentum or body movement
4. **Stiff poses** — within a state, birds hold rigid postures with no subtle shifts

## Solution

A skeletal animation system built on top of Minecraft's existing `ModelPart` cuboid hierarchy. Each bird has a virtual skeleton of 32 joints organised in a tree. A spring-damper physics solver smoothly drives each joint toward target angles set by a pose system and procedural behaviour layer.

### Design Principles

- **All client-side** — the skeleton lives entirely in the model/renderer. Server entities are unaware of it.
- **Universal skeleton** — one joint hierarchy for all 100 species, configured per-species via spring tuning and pose definitions.
- **Beauty and elegance first** — when facing a trade-off between simpler implementation and more lifelike result, choose lifelike. The project exists to celebrate these birds.
- **Incremental build** — each component can be built and tested independently.

## 1. BirdSkeleton & Joint System

### BirdJoint

Each joint holds current rotation, angular velocity, spring parameters, and rotation constraints.

```java
class BirdJoint {
    // Identity
    String name;              // "L_shoulder", "neck_mid", etc.
    BirdJoint parent;         // null for root (chest)

    // Current state (updated by solver each frame)
    float angleX, angleY, angleZ;   // current rotation (radians)
    float velX, velY, velZ;         // angular velocity

    // Target (set by pose resolver + procedural layer)
    float targetX, targetY, targetZ;

    // Spring parameters (configured per-joint, per-archetype)
    float stiffness;          // how hard spring pulls toward target
    float damping;            // how quickly oscillation dies
    float maxVelocity;        // clamp to prevent explosion

    // Constraints
    float minX, maxX;         // rotation limits per axis
    float minY, maxY;
    float minZ, maxZ;
}
```

### Joint Hierarchy (32 joints)

```
chest (root)
├── neck_lower
│   └── neck_mid
│       └── neck_upper
│           └── head
│               ├── upper_beak
│               └── lower_beak
├── shoulder_mount
│   ├── L_upper_wing
│   │   ├── L_scapulars (trailing feathers)
│   │   └── L_forearm
│   │       ├── L_secondaries (trailing feathers)
│   │       └── L_hand
│   │           └── L_primaries (trailing feathers)
│   ├── R_upper_wing
│   │   ├── R_scapulars (trailing feathers)
│   │   └── R_forearm
│   │       ├── R_secondaries (trailing feathers)
│   │       └── R_hand
│   │           └── R_primaries (trailing feathers)
├── torso
├── hip
│   ├── L_thigh
│   │   └── L_shin
│   │       └── L_tarsus
│   │           └── L_foot
│   ├── R_thigh
│   │   └── R_shin
│   │       └── R_tarsus
│   │           └── R_foot
└── tail_base
    └── tail_fan
```

**Anatomical notes:**
- Bird spine is mostly rigid (fused synsacrum) — torso and hip joints should have tight constraints (small range of motion) to allow subtle breathing flex without unrealistic bending. Chest and tail root have more freedom.
- Neck is extremely flexible (3 joints model the S-curve)
- Wing folds in Z-pattern when tucked (shoulder out, elbow back, wrist forward)
- Femur is nearly horizontal and hidden in feathers — visible "thigh" is tibiotarsus
- The "backward knee" is actually an ankle joint
- Upper and lower beak separate for opening mouth (calling, pecking, threat display)

### Spring-Damper Solver

Classic damped harmonic oscillator, per joint, per axis:

```
acceleration = stiffness × (target - current) - damping × velocity
velocity += acceleration × deltaTime
current += velocity × deltaTime
```

Delta time must be computed as the difference in `ageInTicks + partialTick` between the current and previous frame, stored as a field on the skeleton. **Do not use `partialTick` directly** — it is a 0-1 interpolation factor within a single game tick (resets each tick), not an elapsed time. The renderer's `extractRenderState` computes `deltaTime = (currentAgeInTicks + partialTick) - previousAgeAndPartial` and stores it on the render state. Velocity clamped per-joint to prevent spring explosion on frame drops or abrupt state changes.

### Default Spring Tuning by Region

| Region | Stiffness | Damping | Max Vel | Feel |
|--------|-----------|---------|---------|------|
| Neck joints | 80 | 12 | 15 | Very snappy — birds move heads fast |
| Head / beak | 70 | 10 | 12 | Fast but slightly softer than neck |
| Spine (chest, torso, hip) | 50 | 8 | 8 | Moderate — body has mass |
| Wing bones | 40 | 5 | 10 | Smooth with slight overshoot |
| Trailing feathers | 15 | 2 | 8 | Very loose — lags behind leading edge |
| Leg bones | 60 | 9 | 10 | Firm — legs need precision |
| Feet / toes | 45 | 7 | 8 | Slightly softer — grip and flex |
| Tail | 20 | 3 | 6 | Loose and draggy |

These are archetype defaults. Species configs override individual joints as needed (e.g. owl wings heavier/slower, wren tail stiffer for cocked-tail posture).

## 2. Pose System

### PoseData

A pose is a map of joint names to target rotations. Not every joint needs to be specified — unspecified joints inherit from a parent pose or hold current position.

```java
class PoseData {
    String name;                              // "perched", "flying_cruise", etc.
    Map<String, Vector3f> jointAngles;        // joint name → (xRot, yRot, zRot)
    Map<String, SpringOverride> springs;      // optional per-joint spring overrides

    // Builder with auto-mirror for symmetric joints
    static Builder builder(String name);
}

record SpringOverride(float stiffness, float damping) {}
```

The `.mirror()` builder method copies L_ joint definitions to R_ with appropriate sign flips, so poses only need to define one side.

Spring overrides let poses change HOW joints move, not just WHERE — e.g. the "stoop" pose makes wing springs very stiff (locked tight), "gliding" makes trailing feathers even looser.

### Pose Categories

**Static Poses** — full-body target snapshots:
- `perched` — resting on branch, wings tucked
- `alert` — head up, body tense
- `tucked` — sleeping / resting
- `flying_cruise` — level sustained flight
- `flying_takeoff` — launch posture
- `flying_land` — flare with feet forward
- `swimming` — body low, feet back
- `dabbling` — head down, tail up
- `stoop` — wings tucked, diving
- `hover` — body vertical, rapid flap

**Cyclic Animations** — oscillate between two sub-poses:
- `wingbeat` — wings_up ↔ wings_down
- `walk_cycle` — legs_forward ↔ legs_back
- `hop` — crouch ↔ spring ↔ land
- `paddle` — legs alternate push/recovery
- `peck` — head down ↔ head up (beak opens)
- `waddle` — side-to-side body roll with alternating leg stride (waterfowl ground movement)

**Partial Overlays** — affect only some joints, layered on top at a given weight:
- `beak_open` — lower beak only
- `head_tilt` — head yaw/roll
- `tail_wag` — tail fan only
- `legs_tucked` — legs only (in flight)
- `hanging` — body inverted (blue tit)
- `wings_spread` — drying / display
- `threat` — puffed, beak wide

### PoseResolver

Each frame, reads entity state and produces a weighted list of active poses:

1. Pick base pose from entity state (isFlying → flying_cruise, etc.)
2. Compute transition weight (ramp up new pose, ramp down old)
3. Add cyclic phase if active (wingbeat phase from flight goal timing)
4. Layer partial overlays (beak_open, head_tilt, etc.)

Output: a weighted pose stack that is blended into per-joint target angles.

### Pose Blending

Cyclic animations define **offset angles from neutral (zero)**, not absolute joint positions. This prevents double-counting — the base pose positions the body, and the cyclic adds motion on top.

For example, the wingbeat cyclic's `wings_up` sub-pose defines `L_upper_wing.zRot = -0.8` as an offset. If the base `flying_cruise` pose already has `L_upper_wing.zRot = -0.3` (wings slightly spread), the result during wings-up is `-0.3 + (-0.8) = -1.1` — a natural upstroke from the cruise position.

```
For each joint:
  target = Σ(basePose.angle × basePose.weight)          // base poses, weights sum to 1.0
         + lerp(cyclic.offsetA, cyclic.offsetB, phase)   // cyclic OFFSETS, additive
         + Σ(overlay.angle × overlay.weight)              // partial overlays additive
```

Base pose angles are absolute positions. Cyclic and overlay angles are additive offsets from zero.

### Pose Inheritance

Three-tier hierarchy:

1. **Base bird poses** — shared by all species (perched, alert, flying_cruise, tucked)
2. **Archetype family poses** — shared within a family (passerine: hop, flutter, forage; raptor: soar, stoop, hover, strike; waterfowl: swim, dabble, waddle)
3. **Species-specific poses** — unique to one species (robin: territorial puff; blue tit: hanging; barn owl: quartering; peregrine: stoop tuck; mallard: drake display)

Species inherit all poses from their archetype and base, and can override any joint in any inherited pose.

Poses are defined in Java code as static constants using a builder pattern, not external data files. Type-safe, refactorable, close to the code that uses them.

## 3. Procedural Behaviours

Reactive animation layers that run every frame after pose resolution, adding small angle offsets to joint targets. Each is an independent, composable class implementing `ProceduralBehaviour.apply(skeleton, renderState, deltaTime)`.

### Head Tracking

**Joints:** neck_lower, neck_mid, neck_upper, head

Bird looks toward the nearest interesting entity (player within 8 blocks, predator, prey, mate). Look direction distributed across the neck chain:

- neck_lower: 20% of yaw
- neck_mid: 30% of yaw
- neck_upper: 25% of yaw + 40% of pitch
- head: 25% of yaw + 60% of pitch

Optional slight head roll when examining something closely. Periodically breaks gaze to scan elsewhere (randomised timer). Stiff neck springs make this naturally snappy.

### Breathing / Idle Pulse

**Joints:** chest, torso, L_scapulars, R_scapulars

Subtle rhythmic chest expansion. Barely visible but subconsciously reads as "alive". Rate varies by species size (small birds ~3 Hz, large birds ~1 Hz) and activity (temporarily faster after landing, then settles).

### Landing Impact

**Joints:** chest, hip, tail_base, tail_fan, legs, wings

Triggered once when isFlying transitions to false and onGround becomes true. Injects **velocity impulses** (not target angle changes) into joints:

- Body compresses down (chest/hip velocity kick)
- Tail flicks up on impact
- Legs absorb with momentary extra bend
- Wings flare briefly

The springs naturally absorb the impulse into a bounce-settle sequence. Zero keyframing needed.

### Movement Drag

**Joints:** tail_base, tail_fan, L_primaries, R_primaries, L_secondaries, R_secondaries, head

Loose extremities trail behind the bird's direction of movement:

- Yaw change → tail swings wide opposite to turn direction
- Vertical velocity → tail pitches from dive/climb momentum
- Speed → wingtip primaries stream backward

This is the critical behaviour that makes flight look fluid rather than rigid.

### Startle Response

**Joints:** all (one-shot impulse)

Triggered when panic/flee AI goal activates. Uses **velocity impulses only** (same mechanism as landing impact) — no target overrides, keeping all behaviours purely additive:

- Head: large velocity impulse toward threat direction (the stiff neck springs make this look like a snap)
- Body crouches (downward velocity impulse on chest/hip)
- Wings flinch outward (velocity impulse on upper_wing)
- Tail fans in alarm (velocity impulse on tail_fan)

The flinch happens on frame 1 of panic. Springs absorb it over ~200ms, by which time the takeoff pose is blending in. Result: seamless flinch → crouch → launch. Head tracking naturally takes over after the impulse settles, pointing the bird toward the threat.

### Weight Shift & Balance

**Joints:** chest, hip, legs

Walking: body subtly shifts weight over the planted foot (lean into stride).
Perched: gentle aperiodic idle sway using noise function (not sin), very small amplitude — just enough to prevent perfect stillness.

### Composition

All birds get: breathing, weight shift, head tracking.
Flying birds add: drag, landing impact, startle.
Configurable per-archetype — raptors track prey at greater distance, passerines have twitchier heads, etc.

Order of application doesn't matter — all behaviours are strictly additive (offsets to targets, or velocity impulses). No behaviour overrides another's values. Spring solver runs after all behaviours have applied.

Behaviours can read but never write entity state. Purely client-side visual effects.

## 4. Model Geometry Changes

### Universal Cuboid Layout (~32 skeleton-driven parts)

The current models have 14-17 cuboids each but use a mostly flat parent-child hierarchy (most parts are direct children of root). The skeleton requires both **more cuboids** (splitting body, adding neck/wing/leg segments) and a **deeply nested hierarchy** mirroring the joint tree. This means every model's `createBodyLayer()` must be completely rewritten — this is not an incremental modification but a full rebuild of each model's geometry and hierarchy. Each species model is rebuilt with this universal layout:

**Spine (4 parts):** chest, shoulder_mount, torso, hip
- Current single `body` cuboid splits into 4 segments

**Neck (3 parts):** neck_lower, neck_mid, neck_upper
- Entirely new — currently head floats directly on body

**Head (3 parts):** head, upper_beak, lower_beak
- Beak splits into upper and lower mandible for mouth opening

**Wing × 2 (6 parts each):** upper_wing, scapulars, forearm, secondaries, hand, primaries
- Current 2-part wing (wing + wingTip) becomes 6 parts
- Leading edge parts (upper_wing, forearm, hand) are skeleton-driven bones
- Trailing edge parts (scapulars, secondaries, primaries) are children with very loose springs — they lag behind the leading edge during flapping, creating fluid wing ripple
- This trailing edge flex is what makes owl/raptor flight look natural rather than plank-like

**Tail (2 parts):** tail_base, tail_fan
- Same as current, renamed for clarity

**Leg × 2 (4 parts each):** thigh, shin, tarsus, foot
- Current 2-part leg (leg + foot) becomes 4 parts
- Thigh is short and mostly hidden in body feathers
- Shin is the visible "leg" (tibiotarsus)
- Tarsus is the scaly lower section
- The "backward knee" is correctly modelled as an ankle joint

**Species-specific decorative parts** (crown, breast patch, cheek patches, facial disc, drake tail curl, etc.) attach as children of the nearest skeleton-driven part. They move with their parent automatically — no joint needed.

**Part count:** 32 skeleton-driven cuboids + species-specific decorative children (typically 2-6 more, e.g. Robin's breast patch, BarnOwl's facial disc, BlueTit's cheek patches, Mallard's crown/bill_tip/tail_curl).

### ModelPart Hierarchy

The Minecraft `ModelPart` parent-child tree mirrors the skeleton joint tree. When a parent joint rotates, all child cuboids follow automatically. The `SkeletonModelMapper` only needs to write rotation values to each skeleton-driven part; children cascade.

## 5. Integration with Existing Code

### setupAnim() Replacement

Every species model's `setupAnim()` currently has 40-80 lines of if/else chains and manual angle setting. Replaced by a 4-step pipeline:

```java
@Override
public void setupAnim(SpeciesRenderState state) {
    super.setupAnim(state);
    skeleton.resolve(state);          // pick poses from state
    skeleton.applyProcedural(state);  // head track, breathe, etc.
    skeleton.solve(state.deltaTime);  // spring physics
    skeleton.mapToModel(modelParts);  // write angles to cuboids
}
```

All species-specific behaviour lives in pose definitions and behaviour composition, not in setupAnim().

### Skeleton State Persistence

**Critical:** In Minecraft's rendering architecture, model instances are shared singletons — one `RobinModel` renders ALL robins. The skeleton's per-frame state (joint angles, velocities) must be stored **per entity**, not on the model.

Solution: `BirdSkeletonState` is a lightweight object holding the current angle/velocity for all 32 joints. These are stored in a `Map<Integer, BirdSkeletonState>` keyed by entity ID, owned by `AbstractBirdModel`. When `setupAnim()` runs for a specific entity, it looks up (or creates) that entity's skeleton state. States are removed when the entity leaves render distance (on `EntityLeaveWorldEvent` or equivalent client-side hook, or via a periodic cleanup sweep of stale entries).

```java
class BirdSkeletonState {
    float[] angles;      // 32 joints × 3 axes = 96 floats
    float[] velocities;  // 32 joints × 3 axes = 96 floats
    float prevAgeAndPartial;  // for deltaTime computation
}
```

This is compact (~800 bytes per bird) and the lookup is O(1).

### SkeletonModelMapper

Maps joint names to ModelPart references. Defined once per model class. Simple write: `part.xRot = joint.angleX`, etc.

### BirdRenderState Additions

New fields for procedural behaviour inputs:

- `float deltaTime` — frame delta for springs, computed as `(ageInTicks + partialTick) - previousAgeAndPartial` (NOT raw partialTick)
- `float yawDelta` — turn rate for drag
- `float verticalVelocity` — for drag/landing
- `float speed` — movement speed
- `boolean justLanded` — landing trigger (edge-detected)
- `boolean justStartled` — startle trigger (edge-detected)
- `Vec3 lookTarget` — head tracking target position

### BirdAnimations.java — Retired

Every helper method has a skeleton equivalent:

- `animateWingFlap()` → wingbeat cyclic pose + spring physics
- `foldWings()` → perched pose with tucked wing angles
- `tuckLegs()` → legs_tucked partial overlay
- `animateTailBob()` → breathing procedural + tail spring looseness
- `animateWalkingLegs()` → walk_cycle cyclic pose

### AbstractBirdModel (new base class)

Owns BirdSkeleton and SkeletonModelMapper. Provides:

- Universal skeleton (32 joints)
- Spring solver
- Base poses (perched, alert, flying)
- Breathing, head tracking, weight shift behaviours

Species models define:

- ModelPart geometry (cuboid sizes, positions, UV offsets)
- Joint → ModelPart binding map
- Decorative child parts
- Species config (spring overrides, additional poses, pose resolver rules, procedural behaviour parameters)

**Adding a new species:** ~80 lines geometry + ~20 lines config. Everything else inherited.

### What Stays the Same

- Entity classes and AI goals (server side) — completely unchanged
- Render state extraction pipeline — extended, not replaced
- Textures (regenerated for new UV layout, same pipeline)
- Sounds, spawning, registry, creative tab

## 6. Texture System

### Resolution

**512×512 for all species.** This gives ample room for:

- Wing feather patterning (barring, speckling, iridescence)
- Facial detail (owl facial disc shading, eye rings, cheek patches)
- Smooth colour gradients on breast/belly
- Plumage texture — suggesting individual feathers through painting

A 512×512 RGBA texture is 1MB uncompressed. Even with 20 birds on screen, that's 20MB VRAM — negligible.

### BirdUVLayout

New shared class referenced by both model geometry and TextureGenerator. Single source of truth for where each cuboid's faces are on the texture sheet. Defined per-species (cuboid sizes vary between species, so UV regions differ).

### Colour Continuity at Seams

The key challenge: adjacent cuboids must have matching colours at their borders. When chest meets torso, the back face of the chest must match the front face of the torso.

Solution: the TextureGenerator paints a **virtual continuous body** first (one unified colour field for the whole bird), then samples UV regions for each cuboid from that field. Seams are invisible because the colour derives from the same source.

### Sexual Dimorphism

No change — male and female remain separate texture files with the same UV layout. Generator produces both variants.

### Wing Detail

The trailing feather pieces (scapulars, secondaries, primaries) can each get distinct colouring:
- Mallard speculum (iridescent blue-green on secondaries)
- Wing bars on finches
- Owl barring patterns across wing segments
- Primary vs secondary colour differences

## 7. New Classes Summary

### Client-side (src/client/)

| Class | Package | Purpose |
|-------|---------|---------|
| `BirdJoint` | `client/animation` | Single joint with angle, velocity, spring params, constraints |
| `BirdSkeleton` | `client/animation` | Joint tree, factory methods to create universal skeleton |
| `BirdSkeletonState` | `client/animation` | Per-entity persistent state: angles, velocities, prev frame time |
| `SpringSolver` | `client/animation` | Per-joint spring-damper physics tick |
| `PoseData` | `client/animation/pose` | Named set of target joint angles with builder |
| `PoseResolver` | `client/animation/pose` | State → weighted pose stack mapping |
| `BaseBirdPoses` | `client/animation/pose` | Shared poses: perched, alert, flying, tucked |
| `PasserinePoses` | `client/animation/pose` | Family poses: hop, flutter, forage |
| `RaptorPoses` | `client/animation/pose` | Family poses: soar, stoop, hover, strike |
| `WaterfowlPoses` | `client/animation/pose` | Family poses: swim, dabble, waddle |
| `ProceduralBehaviour` | `client/animation/procedural` | Interface: `apply(skeleton, renderState, deltaTime)` |
| `HeadTracking` | `client/animation/procedural` | Neck chain IK toward look target |
| `Breathing` | `client/animation/procedural` | Subtle chest expansion cycle |
| `LandingImpact` | `client/animation/procedural` | One-shot velocity impulse on touchdown |
| `MovementDrag` | `client/animation/procedural` | Tail/wingtip trailing on turns/dives |
| `StartleResponse` | `client/animation/procedural` | Whole-body flinch on panic |
| `WeightShift` | `client/animation/procedural` | Walking lean + perched idle sway |
| `SkeletonModelMapper` | `client/animation` | Joint name → ModelPart binding and angle write |
| `AbstractBirdModel` | `client/model` | Base model class owning skeleton + mapper |
| `BirdUVLayout` | `client/model` | Shared UV offset definitions per species |

### Shared (tools/)

| Class | Purpose |
|-------|---------|
| `TextureGenerator` | Updated for 512×512, new cuboid UV layout, virtual body painting |

### Modified

| Class | Change |
|-------|--------|
| `RobinModel`, `BlueTitModel`, `BarnOwlModel`, `PeregrineFalconModel`, `MallardModel` | Full rebuild: new nested ModelPart hierarchy, new cuboid geometry, skeleton binding, setupAnim() replaced with pipeline call. `createBodyLayer()` rewritten from scratch. |
| `BirdRenderState` | Add deltaTime, yawDelta, verticalVelocity, speed, justLanded, justStartled, lookTarget |
| `AbstractBirdRenderer` | Extract new render state fields |
| Species-specific render states | May simplify (some boolean flags become pose-driven) |

### Retired

| Class | Reason |
|-------|--------|
| `BirdAnimations` | All helpers replaced by skeleton system |
