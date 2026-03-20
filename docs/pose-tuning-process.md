# Bird Pose Tuning Process

Step-by-step process for tuning bird poses using the standalone preview renderer. Use this whenever adding a new species or adjusting existing poses.

## Prerequisites

- `tools/PosePreview.java` — standalone renderer (no Minecraft dependencies)
- Java installed (same JDK used for the mod build)

## The Feedback Loop

### 1. Define the pose in PosePreview.java

Add or modify a pose in the `static {}` block using the `pose()` helper:

```java
Map<String, float[]> MY_POSE = pose(
    "joint_name", xRot, yRot, zRot,
    "another_joint", xRot, yRot, zRot,
    // ...
);
mirror(MY_POSE);  // copies L_ joints to R_ with sign flips
POSES.put("my_pose", MY_POSE);
```

### 2. Render

```bash
cd tools
javac PosePreview.java
java PosePreview my_pose
```

Output: `tools/preview_output.png` — three panels (front, side, top view).

### 3. Analyse the render

Look at each view:
- **Front view** (from +Z): Check wing spread/fold symmetry, body width, leg position
- **Side view** (from +X): Check body pitch, head angle, tail trailing, wing profile, leg tuck
- **Top view** (from -Y): Check body length, wing Z-fold pattern, tail extension

### 4. Adjust and repeat

Modify the pose values, recompile, re-render. Iterate until satisfied.

### 5. Apply to the mod

Once the preview looks right, copy the values into the corresponding pose class:
- `BaseBirdPoses.java` — shared poses (perched, flying, etc.)
- `PasserinePoses.java` — small songbird poses
- `RaptorPoses.java` — raptor poses
- `WaterfowlPoses.java` — duck/waterfowl poses

Then update the model files if geometry changed.

## Coordinate System Reference

Minecraft model coordinate system:
- **+Y** = down, **-Y** = up
- **+Z** = back (tail direction), **-Z** = front (face direction)
- **+X** = right (from bird's perspective)

### Rotation axes (what they do to each body part)

**xRot (pitch):**
- Positive = top tilts backward / bottom tilts forward
- On chest: **positive xRot pitches bird forward** (for flight)
- On head: negative xRot tilts beak upward / positive tilts down

**yRot (yaw):**
- Positive yRot on LEFT wing: folds wing FORWARD (toward head) — WRONG for perch
- Negative yRot on LEFT wing: folds wing BACKWARD (toward tail) — CORRECT for perch
- On head: positive = look right, negative = look left

**zRot (roll):**
- On LEFT wing (extends in +X): **negative zRot = wing UP, positive = wing DOWN**
- This is the correct axis for wing flapping (since wings extend laterally)
- For perched wing fold: small positive zRot presses wings against flanks

### Wing geometry

Wings extend LATERALLY in ±X (outward from body), NOT downward in +Y. This is critical — it means zRot correctly flaps wings up/down regardless of body pitch.

Left wing cuboids: `addBox(0, -0.5, -depth/2, width, 1, depth)` — thin (Y=1), wide (X=width)
Right wing cuboids: `addBox(-width, -0.5, -depth/2, width, 1, depth)` — mirrored

### Wing fold pattern (perched)

Real birds fold wings in a Z-pattern using yRot:
```
L_upper_wing:  yRot = -1.5  (fold back toward tail)
L_forearm:     yRot = +2.2  (fold forward, creating the Z)
L_hand:        yRot = -1.8  (fold back again)
```
Plus small zRot (+0.3) on upper_wing to press against flanks.

### Flying pose key values (robin-scale passerine)

```
Body:  chest +1.0, torso +0.15, hip +0.1 (positive = forward pitch)
Neck:  neck_lower +0.05, neck_mid 0.0, neck_upper -0.05 (extends forward)
Head:  -0.5 (beak points forward in direction of travel)
Wings: L_upper_wing zRot -0.3 (spread outward)
Tail:  tail_base -0.65, tail_fan -0.15 (counteracts body pitch, trails behind)
Legs:  thigh -1.5, shin -2.5, tarsus +2.0, foot -0.8 (accordion fold against belly)
```

### Wingbeat cyclic (offsets from cruise position)

```
Flap UP:   L_upper_wing zRot -0.4  (negative = up)
Flap DOWN: L_upper_wing zRot +0.4  (positive = down)
```
Trailing feathers follow at reduced amplitude (0.04-0.12).

## Body Proportions (robin-scale)

```
chest:        3×3×4  (wider than tall, elongated)
shoulder_mount: 2×1×2
torso:        3×3×4  (matches chest depth)
hip:          2×2×3  (elongated, set far back)
neck segments: 2×1×1 each (short, -1.0 offset between them)
head:         4×4×4
beaks:        1×1×2 each
upper_wing:   4×1×4 (WIDE in X, thin in Y)
forearm:      3×1×3
hand:         3×1×2
trailing:     3×1×2-3 each
tail_base:    2×1×2
tail_fan:     2×1×4 (long)
thigh:        1×1×1 (short)
shin:         1×2×1 (short)
tarsus:       1×1.5×1 (short)
foot:         2×0.5×2 (flat)
```

## Common Mistakes

1. **Wrong pitch direction**: Positive xRot on chest = forward pitch. Negative = backward.
2. **Wings extending in Y**: Wings MUST extend laterally in ±X. If they extend in Y, zRot flap breaks when body pitches.
3. **Wing fold using zRot**: Use yRot for folding wings back. zRot pushes them down.
4. **Tail pointing up in flight**: Tail is child of chest. When chest pitches forward (+1.0), tail goes with it. Use negative xRot on tail_base (-0.65) to counteract and trail behind.
5. **Legs visible in flight**: Need extreme accordion fold with negative thigh xRot to pull against belly.
6. **Neck periscoping in flight**: Neck extends upward (-Y) from chest. Don't over-compensate with positive xRot — keep values near zero or slightly negative so neck extends forward with body.

## Scaling to Other Species

Adjust proportions and spring values:
- **Raptors**: Wider/longer wing cuboids, slower spring stiffness on wings, deeper body pitch for soaring
- **Waterfowl**: Heavier body (5×5×5 chest), thicker neck (3×2×2), wider bill, webbed feet
- **Small passerines**: Smaller overall, faster wing flap frequency, twitchier head springs

Use the PosePreview tool to validate each species before applying to the mod.
