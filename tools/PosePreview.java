import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Standalone bird skeleton pose preview renderer.
 * Generates a PNG with front/side/top views of a Robin skeleton in a given pose.
 *
 * <p>Usage: {@code javac PosePreview.java && java PosePreview [pose_name]}
 * <p>Pose names: perched, flying, flying_flap_up, flying_flap_down, alert,
 *               takeoff, landing, tucked
 * <p>Output: tools/preview_output.png
 *
 * <p>No external dependencies — uses only java.awt and javax.imageio.
 * Reimplements the minimal skeleton/joint/forward-kinematics logic from the mod.
 */
public class PosePreview {

    // =========================================================================
    // Joint definition
    // =========================================================================

    static class Joint {
        final String name;
        final Joint parent;
        final float offsetX, offsetY, offsetZ; // offset from parent (PartPose.offset)
        float angleX, angleY, angleZ;          // current pose angles (radians)

        // Cuboid definition (addBox parameters)
        final float boxOriginX, boxOriginY, boxOriginZ;
        final float boxW, boxH, boxD;

        // Computed world-space transform
        double[] worldPos = new double[3];
        double[][] worldRot = identity();

        // Colour group for rendering
        final Color colour;

        Joint(String name, Joint parent,
              float offsetX, float offsetY, float offsetZ,
              float boxOriginX, float boxOriginY, float boxOriginZ,
              float boxW, float boxH, float boxD,
              Color colour) {
            this.name = name;
            this.parent = parent;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.boxOriginX = boxOriginX;
            this.boxOriginY = boxOriginY;
            this.boxOriginZ = boxOriginZ;
            this.boxW = boxW;
            this.boxH = boxH;
            this.boxD = boxD;
            this.colour = colour;
        }
    }

    // =========================================================================
    // Colour groups
    // =========================================================================

    static final Color SPINE_BLUE   = new Color(80, 120, 200);
    static final Color NECK_PINK    = new Color(220, 120, 160);
    static final Color WING_GREEN   = new Color(80, 180, 100);
    static final Color LEG_ORANGE   = new Color(220, 160, 60);
    static final Color TAIL_YELLOW  = new Color(220, 200, 60);
    static final Color HEAD_PINK    = new Color(200, 100, 140);
    static final Color BEAK_WHITE   = new Color(180, 180, 180);

    // =========================================================================
    // Build Robin skeleton (from RobinModel.createBodyLayer)
    // =========================================================================

    static final Map<String, Joint> JOINT_MAP = new LinkedHashMap<>();
    static final List<Joint> ALL_JOINTS = new ArrayList<>();

    static Joint addJoint(String name, Joint parent,
                          float ox, float oy, float oz,
                          float bx, float by, float bz,
                          float bw, float bh, float bd,
                          Color colour) {
        Joint j = new Joint(name, parent, ox, oy, oz, bx, by, bz, bw, bh, bd, colour);
        JOINT_MAP.put(name, j);
        ALL_JOINTS.add(j);
        return j;
    }

    static {
        // Root: chest at (0, 19, 0) — LONGER and SLEEKER body
        Joint chest = addJoint("chest", null,
                0f, 19f, 0f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);  // deeper (4 instead of 3)

        // Shoulder mount (child of chest)
        Joint shoulderMount = addJoint("shoulder_mount", chest,
                0f, -0.5f, 0f,
                -1f, -0.5f, -1f, 2f, 1f, 2f, SPINE_BLUE);  // flatter

        // Torso (child of chest) — elongated
        addJoint("torso", chest,
                0f, 0f, 2f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);  // deeper, offset further back

        // Hip (child of chest) — further back for longer body
        Joint hip = addJoint("hip", chest,
                0f, 0.5f, 3f,
                -1f, -1f, -1.5f, 2f, 2f, 3f, SPINE_BLUE);  // deeper, further back

        // Neck chain (children of chest)
        Joint neckLower = addJoint("neck_lower", chest,
                0f, -1f, -0.5f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);

        Joint neckMid = addJoint("neck_mid", neckLower,
                0f, -1f, 0f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);

        Joint neckUpper = addJoint("neck_upper", neckMid,
                0f, -1f, 0f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);

        // Head (child of neck_upper)
        Joint head = addJoint("head", neckUpper,
                0f, -1f, 0f,
                -2f, -4f, -2f, 4f, 4f, 4f, HEAD_PINK);

        // Beaks (children of head)
        addJoint("upper_beak", head,
                0f, 0f, 0f,
                -0.5f, -2f, -4f, 1f, 1f, 2f, BEAK_WHITE);

        addJoint("lower_beak", head,
                0f, 0f, 0f,
                -0.5f, -1f, -4f, 1f, 1f, 2f, BEAK_WHITE);

        // --- LEFT WING --- (extends in +X outward from body)
        Joint lUpperWing = addJoint("L_upper_wing", shoulderMount,
                1f, -0.5f, 0f,
                0f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);

        addJoint("L_scapulars", lUpperWing,
                0f, 0f, 0f,
                0.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);

        Joint lForearm = addJoint("L_forearm", lUpperWing,
                4f, 0f, 0f,
                0f, -0.5f, -1.5f, 3f, 1f, 3f, WING_GREEN);

        addJoint("L_secondaries", lForearm,
                0f, 0f, 0f,
                0.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);

        Joint lHand = addJoint("L_hand", lForearm,
                3f, 0f, 0f,
                0f, -0.5f, -1f, 3f, 1f, 2f, WING_GREEN);

        addJoint("L_primaries", lHand,
                0f, 0f, 0f,
                0.5f, 0f, -0.5f, 3f, 1f, 2f, WING_GREEN);

        // --- RIGHT WING --- (extends in -X outward from body)
        Joint rUpperWing = addJoint("R_upper_wing", shoulderMount,
                -1f, -0.5f, 0f,
                -4f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);

        addJoint("R_scapulars", rUpperWing,
                0f, 0f, 0f,
                -3.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);

        Joint rForearm = addJoint("R_forearm", rUpperWing,
                -4f, 0f, 0f,
                -3f, -0.5f, -1.5f, 3f, 1f, 3f, WING_GREEN);

        addJoint("R_secondaries", rForearm,
                0f, 0f, 0f,
                -3.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);

        Joint rHand = addJoint("R_hand", rForearm,
                -3f, 0f, 0f,
                -3f, -0.5f, -1f, 3f, 1f, 2f, WING_GREEN);

        addJoint("R_primaries", rHand,
                0f, 0f, 0f,
                -3.5f, 0f, -0.5f, 3f, 1f, 2f, WING_GREEN);

        // --- TAIL --- (further back to match longer body)
        Joint tailBase = addJoint("tail_base", chest,
                0f, 0f, 3.5f,
                -1f, -0.5f, 0f, 2f, 1f, 2f, TAIL_YELLOW);

        addJoint("tail_fan", tailBase,
                0f, 0f, 2f,
                -1f, -0.5f, 0f, 2f, 1f, 4f, TAIL_YELLOW);  // longer tail fan

        // --- LEFT LEG --- (shorter — thigh 1, shin 2, tarsus 1.5)
        Joint lThigh = addJoint("L_thigh", hip,
                0.75f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1f, 1f, LEG_ORANGE);   // shorter thigh

        Joint lShin = addJoint("L_shin", lThigh,
                0f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);   // shorter shin

        Joint lTarsus = addJoint("L_tarsus", lShin,
                0f, 2f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE); // shorter tarsus

        addJoint("L_foot", lTarsus,
                0f, 1.5f, 0f,
                -1f, 0f, -1f, 2f, 0.5f, 2f, LEG_ORANGE);     // flatter foot

        // --- RIGHT LEG --- (shorter to match left)
        Joint rThigh = addJoint("R_thigh", hip,
                -0.75f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1f, 1f, LEG_ORANGE);

        Joint rShin = addJoint("R_shin", rThigh,
                0f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);

        Joint rTarsus = addJoint("R_tarsus", rShin,
                0f, 2f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);

        addJoint("R_foot", rTarsus,
                0f, 1.5f, 0f,
                -1f, 0f, -1f, 2f, 0.5f, 2f, LEG_ORANGE);
    }

    // =========================================================================
    // Pose data
    // =========================================================================

    /** A pose is a map from joint name to [xRot, yRot, zRot]. */
    static Map<String, float[]> pose(Object... args) {
        Map<String, float[]> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 4) {
            String name = (String) args[i];
            float x = ((Number) args[i + 1]).floatValue();
            float y = ((Number) args[i + 2]).floatValue();
            float z = ((Number) args[i + 3]).floatValue();
            m.put(name, new float[]{x, y, z});
        }
        return m;
    }

    /** Mirror: copy L_ joint values to R_ (negating yRot and zRot for symmetry). */
    static void mirror(Map<String, float[]> pose) {
        Map<String, float[]> mirrored = new HashMap<>();
        for (var entry : pose.entrySet()) {
            String name = entry.getKey();
            float[] v = entry.getValue();
            if (name.startsWith("L_")) {
                String rName = "R_" + name.substring(2);
                if (!pose.containsKey(rName)) {
                    mirrored.put(rName, new float[]{v[0], -v[1], -v[2]});
                }
            }
        }
        pose.putAll(mirrored);
    }

    /** Add offset values from a cyclic animation frame to a base pose. */
    static Map<String, float[]> addPoses(Map<String, float[]> base, Map<String, float[]> offset) {
        Map<String, float[]> result = new HashMap<>(base);
        for (var entry : offset.entrySet()) {
            String name = entry.getKey();
            float[] off = entry.getValue();
            float[] existing = result.getOrDefault(name, new float[]{0, 0, 0});
            result.put(name, new float[]{
                    existing[0] + off[0],
                    existing[1] + off[1],
                    existing[2] + off[2]
            });
        }
        return result;
    }

    // Define all poses from BaseBirdPoses.java

    static final Map<String, float[]> PERCHED;
    static final Map<String, float[]> ALERT;
    static final Map<String, float[]> FLYING_CRUISE;
    static final Map<String, float[]> FLYING_TAKEOFF;
    static final Map<String, float[]> FLYING_LAND;
    static final Map<String, float[]> TUCKED;
    static final Map<String, float[]> LEGS_TUCKED;
    static final Map<String, float[]> WINGBEAT_UP;
    static final Map<String, float[]> WINGBEAT_DOWN;

    static {
        PERCHED = pose(
                "chest", 0.1f, 0f, 0f,
                "neck_lower", -0.15f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", -0.05f, 0f, 0f,
                // Wing Z-fold: back, forward, back (yRot) + slight downward (zRot)
                "L_upper_wing", 0f, -1.5f, 0.3f,    // fold back toward tail + slight down
                "L_forearm", 0f, 2.2f, 0f,         // fold forward
                "L_hand", 0f, -1.8f, 0f,             // fold back again
                "L_scapulars", 0f, -0.3f, 0.1f,      // trailing feathers follow
                "L_secondaries", 0f, -0.2f, 0.1f,
                "L_primaries", 0f, -0.2f, 0f,
                "tail_base", -0.2f, 0f, 0f,
                "L_thigh", 0.1f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.4f, 0f, 0f
        );
        mirror(PERCHED);

        ALERT = pose(
                "chest", 0.0f, 0f, 0f,
                "neck_lower", -0.2f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.15f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "L_scapulars", 0f, -0.3f, 0.1f,
                "L_secondaries", 0f, -0.2f, 0.1f,
                "L_primaries", 0f, -0.2f, 0f,
                "tail_base", -0.1f, 0f, 0f
        );
        mirror(ALERT);

        FLYING_CRUISE = pose(
                "chest", -1.2f, 0f, 0f,
                "torso", -0.2f, 0f, 0f,
                "hip", -0.15f, 0f, 0f,
                "neck_lower", 0.05f, 0f, 0f,
                "neck_mid", 0.05f, 0f, 0f,
                "neck_upper", 0.05f, 0f, 0f,
                "head", 0.15f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.3f, 0f, 0f
        );
        mirror(FLYING_CRUISE);

        FLYING_TAKEOFF = pose(
                "chest", -1.4f, 0f, 0f,
                "torso", -0.3f, 0f, 0f,
                "hip", -0.2f, 0f, 0f,
                "neck_lower", 0.15f, 0f, 0f,
                "neck_mid", 0.1f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", 0.2f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.5f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.4f, 0f, 0f
        );
        mirror(FLYING_TAKEOFF);

        FLYING_LAND = pose(
                "chest", 0.3f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.8f,
                "L_forearm", 0f, 0f, -0.3f,
                "L_hand", 0f, 0f, -0.15f,
                "tail_base", -0.8f, 0f, 0f,
                "L_thigh", -0.3f, 0f, 0f,
                "L_shin", -0.5f, 0f, 0f
        );
        mirror(FLYING_LAND);

        TUCKED = pose(
                "chest", 0.15f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", 0.3f, 0f, 0.4f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "L_scapulars", 0f, -0.3f, 0.1f,
                "L_secondaries", 0f, -0.2f, 0.1f,
                "L_primaries", 0f, -0.2f, 0f,
                "tail_base", 0.1f, 0f, 0f
        );
        mirror(TUCKED);

        LEGS_TUCKED = pose(
                "L_thigh", 0.5f, 0f, 0f,
                "L_shin", 0.8f, 0f, 0f,
                "L_tarsus", -0.3f, 0f, 0f,
                "L_foot", 0.2f, 0f, 0f
        );
        mirror(LEGS_TUCKED);

        // Confirmed: negative zRot = wings UP, positive = wings DOWN
        WINGBEAT_UP = pose(
                "L_upper_wing", 0f, 0f, -0.4f,
                "L_forearm", 0f, 0f, -0.12f,
                "L_hand", 0f, 0f, -0.08f,
                "L_scapulars", 0f, 0f, -0.1f,
                "L_secondaries", 0f, 0f, -0.06f,
                "L_primaries", 0f, 0f, -0.04f
        );
        mirror(WINGBEAT_UP);

        WINGBEAT_DOWN = pose(
                "L_upper_wing", 0f, 0f, 0.4f,
                "L_forearm", 0f, 0f, 0.12f,
                "L_hand", 0f, 0f, 0.08f,
                "L_scapulars", 0f, 0f, 0.1f,
                "L_secondaries", 0f, 0f, 0.06f,
                "L_primaries", 0f, 0f, 0.04f
        );
        mirror(WINGBEAT_DOWN);
    }

    static Map<String, Map<String, float[]>> POSES = new LinkedHashMap<>();

    // Iteration 2: neck tucked more forward (negative xRot on neck joints)
    static Map<String, float[]> FLYING_V2, FLYING_V3, FLYING_V4;
    static {
        // V2: neck slightly forward (-0.1 per joint), head +0.3 to look ahead
        FLYING_V2 = pose(
                "chest", -1.2f, 0f, 0f,
                "torso", -0.2f, 0f, 0f,
                "hip", -0.15f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", 0.3f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.3f, 0f, 0f
        );
        mirror(FLYING_V2);

        // V3: neck more aggressively forward (-0.2), head +0.5 to compensate
        FLYING_V3 = pose(
                "chest", -1.2f, 0f, 0f,
                "torso", -0.2f, 0f, 0f,
                "hip", -0.15f, 0f, 0f,
                "neck_lower", -0.2f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", 0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.3f, 0f, 0f
        );
        mirror(FLYING_V3);

        // V4: kept for reference
        FLYING_V4 = pose(
                "chest", 1.2f, 0f, 0f,
                "torso", 0.2f, 0f, 0f,
                "hip", 0.15f, 0f, 0f,
                "neck_lower", 0.1f, 0f, 0f,
                "neck_mid", 0.1f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", -0.3f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", 0.3f, 0f, 0f
        );
        mirror(FLYING_V4);

        // V9: kept for reference
        Map<String, float[]> FLYING_V9 = pose(
                "chest", 1.0f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, 0f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.6f, 0f, 0f,
                "tail_fan", -0.2f, 0f, 0f
        );
        mirror(FLYING_V9);
        Map<String, float[]> LEGS_TUCKED_V9 = pose(
                "L_thigh", -0.8f, 0f, 0f,
                "L_shin", -1.5f, 0f, 0f,
                "L_tarsus", 1.0f, 0f, 0f,
                "L_foot", -0.3f, 0f, 0f
        );
        mirror(LEGS_TUCKED_V9);
        POSES.put("flying_v9", addPoses(FLYING_V9, LEGS_TUCKED_V9));

        // V10: refined body/tail/legs
        Map<String, float[]> FLYING_V10 = pose(
                "chest", 1.0f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, 0f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, -0.2f,
                "L_hand", 0f, 0f, -0.1f,
                "tail_base", -0.65f, 0f, 0f,       // stronger counteraction to get tail horizontal
                "tail_fan", -0.15f, 0f, 0f
        );
        mirror(FLYING_V10);
        Map<String, float[]> LEGS_TUCKED_V10 = pose(
                "L_thigh", -1.2f, 0f, 0f,          // pull thigh even more up against belly
                "L_shin", -2.0f, 0f, 0f,           // fold shin very tight
                "L_tarsus", 1.5f, 0f, 0f,          // fold tarsus back tight
                "L_foot", -0.5f, 0f, 0f
        );
        mirror(LEGS_TUCKED_V10);
        POSES.put("flying_v10", addPoses(FLYING_V10, LEGS_TUCKED_V10));

        // V11: same body as V10 but legs folded to near-invisible
        Map<String, float[]> LEGS_TUCKED_V11 = pose(
                "L_thigh", -1.5f, 0f, 0f,          // thigh almost flat against belly
                "L_shin", -2.5f, 0f, 0f,           // shin doubles back on thigh
                "L_tarsus", 2.0f, 0f, 0f,          // tarsus folds forward against shin
                "L_foot", -0.8f, 0f, 0f            // toes tucked
        );
        mirror(LEGS_TUCKED_V11);
        POSES.put("flying_v11", addPoses(FLYING_V10, LEGS_TUCKED_V11));
        POSES.put("flying_v11_flap_up", addPoses(addPoses(FLYING_V10, LEGS_TUCKED_V11), WINGBEAT_UP));
        POSES.put("flying_v11_flap_down", addPoses(addPoses(FLYING_V10, LEGS_TUCKED_V11), WINGBEAT_DOWN));

        // Axis test: only change L_upper_wing zRot +0.5 to see which way it goes
        Map<String, float[]> TEST_ZROT_POS = pose("L_upper_wing", 0f, 0f, 0.5f);
        mirror(TEST_ZROT_POS);
        POSES.put("test_zpos", addPoses(addPoses(FLYING_V10, LEGS_TUCKED_V11), TEST_ZROT_POS));

        Map<String, float[]> TEST_ZROT_NEG = pose("L_upper_wing", 0f, 0f, -0.5f);
        mirror(TEST_ZROT_NEG);
        POSES.put("test_zneg", addPoses(addPoses(FLYING_V10, LEGS_TUCKED_V11), TEST_ZROT_NEG));
    }

    static {
        POSES.put("perched", PERCHED);
        POSES.put("alert", ALERT);
        POSES.put("flying", addPoses(FLYING_CRUISE, LEGS_TUCKED));
        POSES.put("flying_v2", addPoses(FLYING_V2, LEGS_TUCKED));
        POSES.put("flying_v3", addPoses(FLYING_V3, LEGS_TUCKED));
        POSES.put("flying_v4", addPoses(FLYING_V4, LEGS_TUCKED));
        POSES.put("flying_flap_up", addPoses(addPoses(FLYING_CRUISE, LEGS_TUCKED), WINGBEAT_UP));
        POSES.put("flying_flap_down", addPoses(addPoses(FLYING_CRUISE, LEGS_TUCKED), WINGBEAT_DOWN));
        POSES.put("takeoff", addPoses(FLYING_TAKEOFF, LEGS_TUCKED));
        POSES.put("landing", FLYING_LAND);
        POSES.put("tucked", TUCKED);
    }

    // =========================================================================
    // Matrix maths (3x3 rotation + translation)
    // =========================================================================

    static double[][] identity() {
        return new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    static double[][] rotX(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{
                {1, 0, 0},
                {0, c, -s},
                {0, s, c}
        };
    }

    static double[][] rotY(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{
                {c, 0, s},
                {0, 1, 0},
                {-s, 0, c}
        };
    }

    static double[][] rotZ(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{
                {c, -s, 0},
                {s, c, 0},
                {0, 0, 1}
        };
    }

    static double[][] mulMat(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int k = 0; k < 3; k++)
                    r[i][j] += a[i][k] * b[k][j];
        return r;
    }

    static double[] mulVec(double[][] m, double[] v) {
        return new double[]{
                m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
                m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
                m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]
        };
    }

    static double[] addVec(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    // =========================================================================
    // Forward kinematics
    // =========================================================================

    static void applyPose(Map<String, float[]> poseData) {
        // Reset all angles
        for (Joint j : ALL_JOINTS) {
            j.angleX = 0;
            j.angleY = 0;
            j.angleZ = 0;
        }
        // Apply pose
        for (var entry : poseData.entrySet()) {
            Joint j = JOINT_MAP.get(entry.getKey());
            if (j != null) {
                float[] v = entry.getValue();
                j.angleX = v[0];
                j.angleY = v[1];
                j.angleZ = v[2];
            }
        }
    }

    static void computeForwardKinematics() {
        for (Joint j : ALL_JOINTS) {
            double[][] parentRot;
            double[] parentPos;
            if (j.parent == null) {
                parentRot = identity();
                parentPos = new double[]{0, 0, 0};
            } else {
                parentRot = j.parent.worldRot;
                parentPos = j.parent.worldPos;
            }

            // Local offset transformed by parent rotation
            double[] localOffset = {j.offsetX, j.offsetY, j.offsetZ};
            double[] worldOffset = mulVec(parentRot, localOffset);
            j.worldPos = addVec(parentPos, worldOffset);

            // Local rotation: XYZ order (Minecraft convention)
            double[][] localRot = mulMat(mulMat(rotX(j.angleX), rotY(j.angleY)), rotZ(j.angleZ));
            j.worldRot = mulMat(parentRot, localRot);
        }
    }

    // =========================================================================
    // 3D cuboid corners
    // =========================================================================

    static double[][] getCuboidCorners(Joint j) {
        // 8 corners of the box in local space
        float x0 = j.boxOriginX, y0 = j.boxOriginY, z0 = j.boxOriginZ;
        float x1 = x0 + j.boxW, y1 = y0 + j.boxH, z1 = z0 + j.boxD;
        double[][] localCorners = {
                {x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0},
                {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}
        };
        // Transform to world space
        double[][] worldCorners = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double[] rotated = mulVec(j.worldRot, localCorners[i]);
            worldCorners[i] = addVec(j.worldPos, rotated);
        }
        return worldCorners;
    }

    // =========================================================================
    // Orthographic projection
    // =========================================================================

    enum View {
        FRONT,  // looking from +Z towards -Z, project onto XY
        SIDE,   // looking from +X towards -X, project onto ZY
        TOP     // looking from -Y towards +Y, project onto XZ
    }

    static double[] project(double[] worldPoint, View view) {
        switch (view) {
            case FRONT: return new double[]{worldPoint[0], worldPoint[1]};
            case SIDE:  return new double[]{worldPoint[2], worldPoint[1]};
            case TOP:   return new double[]{worldPoint[0], worldPoint[2]};
            default:    return new double[]{0, 0};
        }
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    static final int PANEL_W = 400;
    static final int PANEL_H = 400;
    static final int TOTAL_W = PANEL_W * 3;
    static final int TOTAL_H = PANEL_H;
    static final float SCALE = 18f; // pixels per Minecraft unit

    static int toScreenX(double worldCoord, int panelIndex) {
        return panelIndex * PANEL_W + PANEL_W / 2 + (int) (worldCoord * SCALE);
    }

    static int toScreenY(double worldCoord) {
        // Y is up in MC, but down on screen. Also offset so model is centred.
        // The chest is at Y=19 in MC coords. Centre around Y=20 (roughly mid-model).
        return PANEL_H / 2 + (int) ((worldCoord - 19f) * SCALE);
    }

    static void drawCuboid(Graphics2D g, Joint j, View view, int panelIndex) {
        double[][] corners = getCuboidCorners(j);
        int[] sx = new int[8], sy = new int[8];
        for (int i = 0; i < 8; i++) {
            double[] p2d = project(corners[i], view);
            sx[i] = toScreenX(p2d[0], panelIndex);
            sy[i] = toScreenY(p2d[1]);
        }

        // Draw filled faces (semi-transparent)
        Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), 60);
        g.setColor(fill);

        // 6 faces: front(0123), back(4567), left(0374), right(1265), top(0154), bottom(3267)
        int[][] faces = {
                {0, 1, 2, 3}, {4, 5, 6, 7}, {0, 3, 7, 4},
                {1, 2, 6, 5}, {0, 1, 5, 4}, {3, 2, 6, 7}
        };
        for (int[] face : faces) {
            int[] fx = new int[4], fy = new int[4];
            for (int i = 0; i < 4; i++) {
                fx[i] = sx[face[i]];
                fy[i] = sy[face[i]];
            }
            g.fillPolygon(fx, fy, 4);
        }

        // Draw edges
        Color edge = new Color(j.colour.getRed() / 2, j.colour.getGreen() / 2, j.colour.getBlue() / 2, 200);
        g.setColor(edge);
        g.setStroke(new BasicStroke(1.2f));
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int[] e : edges) {
            g.drawLine(sx[e[0]], sy[e[0]], sx[e[1]], sy[e[1]]);
        }
    }

    static void drawJointDot(Graphics2D g, Joint j, View view, int panelIndex) {
        double[] p2d = project(j.worldPos, view);
        int x = toScreenX(p2d[0], panelIndex);
        int y = toScreenY(p2d[1]);

        // Filled circle
        g.setColor(j.colour);
        g.fillOval(x - 4, y - 4, 8, 8);

        // Dark outline
        g.setColor(j.colour.darker().darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - 4, y - 4, 8, 8);
    }

    static void drawJointLabel(Graphics2D g, Joint j, View view, int panelIndex) {
        // Only label key joints to avoid clutter
        Set<String> labelled = Set.of(
                "chest", "head", "hip", "tail_fan",
                "L_upper_wing", "L_hand", "R_upper_wing", "R_hand",
                "L_foot", "R_foot", "shoulder_mount"
        );
        if (!labelled.contains(j.name)) return;

        double[] p2d = project(j.worldPos, view);
        int x = toScreenX(p2d[0], panelIndex);
        int y = toScreenY(p2d[1]);

        g.setColor(new Color(60, 60, 60));
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));

        // Shorten label
        String label = j.name.replace("_", " ");
        g.drawString(label, x + 6, y - 4);
    }

    static void drawGroundPlane(Graphics2D g, View view, int panelIndex) {
        // Ground is at Y=24 in MC coords (the bird stands on its feet at ~Y=24)
        int groundY = toScreenY(24.0);
        g.setColor(new Color(140, 100, 60, 100));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{6f, 4f}, 0f));
        int x0 = panelIndex * PANEL_W + 10;
        int x1 = panelIndex * PANEL_W + PANEL_W - 10;
        g.drawLine(x0, groundY, x1, groundY);

        g.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g.setColor(new Color(140, 100, 60, 180));
        g.drawString("ground", x0, groundY + 12);
    }

    static void drawPanel(Graphics2D g, View view, int panelIndex, String viewLabel) {
        // Panel background
        int x0 = panelIndex * PANEL_W;
        g.setColor(new Color(245, 245, 240));
        g.fillRect(x0, 0, PANEL_W, PANEL_H);

        // Panel border
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(1f));
        g.drawRect(x0, 0, PANEL_W - 1, PANEL_H - 1);

        // View label
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(viewLabel, x0 + 10, 22);

        // Ground plane
        drawGroundPlane(g, view, panelIndex);

        // Draw cuboids (back to front: sort by depth for the view)
        // For simplicity, just draw in joint order — good enough for wireframe
        for (Joint j : ALL_JOINTS) {
            drawCuboid(g, j, view, panelIndex);
        }

        // Draw joint dots on top
        for (Joint j : ALL_JOINTS) {
            drawJointDot(g, j, view, panelIndex);
        }

        // Draw labels on top
        for (Joint j : ALL_JOINTS) {
            drawJointLabel(g, j, view, panelIndex);
        }
    }

    static void drawLegend(Graphics2D g) {
        int x = TOTAL_W - 150;
        int y = TOTAL_H - 90;
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));

        Object[][] legend = {
                {SPINE_BLUE, "Spine"},
                {NECK_PINK, "Neck"},
                {HEAD_PINK, "Head"},
                {WING_GREEN, "Wings"},
                {LEG_ORANGE, "Legs"},
                {TAIL_YELLOW, "Tail"},
        };

        for (Object[] item : legend) {
            Color c = (Color) item[0];
            String label = (String) item[1];
            g.setColor(c);
            g.fillOval(x, y - 8, 10, 10);
            g.setColor(new Color(60, 60, 60));
            g.drawString(label, x + 14, y);
            y += 14;
        }
    }

    // =========================================================================
    // Main
    // =========================================================================

    public static void main(String[] args) throws Exception {
        String poseName = args.length > 0 ? args[0] : "flying";

        Map<String, float[]> poseData = POSES.get(poseName);
        if (poseData == null) {
            System.err.println("Unknown pose: " + poseName);
            System.err.println("Available poses: " + String.join(", ", POSES.keySet()));
            System.exit(1);
        }

        System.out.println("Rendering pose: " + poseName);

        // Apply pose angles
        applyPose(poseData);

        // Compute forward kinematics
        computeForwardKinematics();

        // Create image
        BufferedImage image = new BufferedImage(TOTAL_W, TOTAL_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw 3 panels
        drawPanel(g, View.FRONT, 0, "FRONT (from +Z)");
        drawPanel(g, View.SIDE,  1, "SIDE (from +X)");
        drawPanel(g, View.TOP,   2, "TOP (from -Y)");

        // Title
        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        String title = "Robin Skeleton — " + poseName.toUpperCase().replace('_', ' ');
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (TOTAL_W - fm.stringWidth(title)) / 2, TOTAL_H - 10);

        // Legend
        drawLegend(g);

        g.dispose();

        // Write PNG
        // Determine output path: if CWD is tools/, write here; otherwise write to tools/
        File outputFile;
        File toolsDir = new File("tools");
        if (new File("PosePreview.java").exists()) {
            // We're in the tools directory
            outputFile = new File("preview_output.png");
        } else if (toolsDir.exists()) {
            outputFile = new File(toolsDir, "preview_output.png");
        } else {
            outputFile = new File("preview_output.png");
        }

        ImageIO.write(image, "PNG", outputFile);
        System.out.println("Written: " + outputFile.getAbsolutePath());
        System.out.println("Image size: " + TOTAL_W + "x" + TOTAL_H);
    }
}
