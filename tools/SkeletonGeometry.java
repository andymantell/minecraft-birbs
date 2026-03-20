import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Joint hierarchy, skeleton builders, forward kinematics, and matrix math.
 * Extracted from PoseEditor during refactor — no logic changes.
 */
class SkeletonGeometry {

    // =========================================================================
    // Colour groups (matching PosePreview)
    // =========================================================================

    static final Color SPINE_BLUE   = new Color(80, 120, 200);
    static final Color NECK_PINK    = new Color(220, 120, 160);
    static final Color WING_GREEN   = new Color(80, 180, 100);
    static final Color LEG_ORANGE   = new Color(220, 160, 60);
    static final Color TAIL_YELLOW  = new Color(220, 200, 60);
    static final Color HEAD_PINK    = new Color(200, 100, 140);
    static final Color BEAK_WHITE   = new Color(180, 180, 180);

    // =========================================================================
    // Joint name constants
    // =========================================================================

    static final String[] SPINE_JOINTS = {"chest", "shoulder_mount", "torso", "hip"};
    static final String[] NECK_HEAD_JOINTS = {"neck_lower", "neck_mid", "neck_upper", "head", "upper_beak", "lower_beak"};
    static final String[] LEFT_WING_JOINTS = {"L_upper_wing", "L_scapulars", "L_forearm", "L_secondaries", "L_hand", "L_primaries"};
    static final String[] TAIL_JOINTS = {"tail_base", "tail_fan"};
    static final String[] LEFT_LEG_JOINTS = {"L_thigh", "L_shin", "L_tarsus", "L_foot"};
    static final String[] RIGHT_LEG_JOINTS = {"R_thigh", "R_shin", "R_tarsus", "R_foot"};

    // =========================================================================
    // View enum
    // =========================================================================

    enum View { FRONT, SIDE, TOP }

    // =========================================================================
    // Joint definition
    // =========================================================================

    static class Joint {
        final String name;
        final Joint parent;
        float offsetX, offsetY, offsetZ;
        float angleX, angleY, angleZ;
        float boxOriginX, boxOriginY, boxOriginZ;
        float boxW, boxH, boxD;
        double[] worldPos = new double[3];
        double[][] worldRot = identity();
        final Color colour;

        // Default geometry (set once at construction, used for preset resets)
        final float defaultOffsetX, defaultOffsetY, defaultOffsetZ;
        final float defaultBoxW, defaultBoxH, defaultBoxD;
        final float defaultBoxOriginX, defaultBoxOriginY, defaultBoxOriginZ;

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
            // Store defaults
            this.defaultOffsetX = offsetX;
            this.defaultOffsetY = offsetY;
            this.defaultOffsetZ = offsetZ;
            this.defaultBoxW = boxW;
            this.defaultBoxH = boxH;
            this.defaultBoxD = boxD;
            this.defaultBoxOriginX = boxOriginX;
            this.defaultBoxOriginY = boxOriginY;
            this.defaultBoxOriginZ = boxOriginZ;
        }

        /** Recompute boxOrigin from current dimensions, preserving the original origin/size ratio. */
        void recomputeBoxOrigin() {
            boxOriginX = defaultBoxW != 0 ? defaultBoxOriginX * (boxW / defaultBoxW) : -(boxW / 2f);
            boxOriginY = defaultBoxH != 0 ? defaultBoxOriginY * (boxH / defaultBoxH) : -(boxH / 2f);
            boxOriginZ = defaultBoxD != 0 ? defaultBoxOriginZ * (boxD / defaultBoxD) : -(boxD / 2f);
        }

        /** Reset geometry to archetype defaults. */
        void resetGeometry() {
            offsetX = defaultOffsetX;
            offsetY = defaultOffsetY;
            offsetZ = defaultOffsetZ;
            boxW = defaultBoxW;
            boxH = defaultBoxH;
            boxD = defaultBoxD;
            boxOriginX = defaultBoxOriginX;
            boxOriginY = defaultBoxOriginY;
            boxOriginZ = defaultBoxOriginZ;
        }
    }

    // =========================================================================
    // Skeleton container
    // =========================================================================

    static class Skeleton {
        final Map<String, Joint> jointMap = new LinkedHashMap<>();
        final List<Joint> allJoints = new ArrayList<>();

        Joint addJoint(String name, Joint parent,
                       float ox, float oy, float oz,
                       float bx, float by, float bz,
                       float bw, float bh, float bd,
                       Color colour) {
            Joint j = new Joint(name, parent, ox, oy, oz, bx, by, bz, bw, bh, bd, colour);
            jointMap.put(name, j);
            allJoints.add(j);
            return j;
        }
    }

    // =========================================================================
    // Archetype skeleton builders
    // =========================================================================

    static Skeleton buildPasserine() {
        Skeleton s = new Skeleton();
        Joint chest = s.addJoint("chest", null, 0f, 19f, 0f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);
        Joint shoulderMount = s.addJoint("shoulder_mount", chest, 0f, -0.5f, 0f,
                -1f, -0.5f, -1f, 2f, 1f, 2f, SPINE_BLUE);
        s.addJoint("torso", chest, 0f, 0f, 2f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);
        Joint hip = s.addJoint("hip", chest, 0f, 0.5f, 3f,
                -1f, -1f, -1.5f, 2f, 2f, 3f, SPINE_BLUE);

        Joint neckLower = s.addJoint("neck_lower", chest, 0f, -1f, -0.5f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);
        Joint neckMid = s.addJoint("neck_mid", neckLower, 0f, -1f, 0f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);
        Joint neckUpper = s.addJoint("neck_upper", neckMid, 0f, -1f, 0f,
                -1f, -1f, -0.5f, 2f, 1f, 1f, NECK_PINK);
        Joint head = s.addJoint("head", neckUpper, 0f, -1f, 0f,
                -2f, -4f, -2f, 4f, 4f, 4f, HEAD_PINK);
        s.addJoint("upper_beak", head, 0f, 0f, 0f,
                -0.5f, -2f, -4f, 1f, 1f, 2f, BEAK_WHITE);
        s.addJoint("lower_beak", head, 0f, 0f, 0f,
                -0.5f, -1f, -4f, 1f, 1f, 2f, BEAK_WHITE);

        // Left wing
        Joint lUpper = s.addJoint("L_upper_wing", shoulderMount, 1f, -0.5f, 0f,
                0f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);
        s.addJoint("L_scapulars", lUpper, 0f, 0f, 0f,
                0.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);
        Joint lForearm = s.addJoint("L_forearm", lUpper, 4f, 0f, 0f,
                0f, -0.5f, -1.5f, 3f, 1f, 3f, WING_GREEN);
        s.addJoint("L_secondaries", lForearm, 0f, 0f, 0f,
                0.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);
        Joint lHand = s.addJoint("L_hand", lForearm, 3f, 0f, 0f,
                0f, -0.5f, -1f, 3f, 1f, 2f, WING_GREEN);
        s.addJoint("L_primaries", lHand, 0f, 0f, 0f,
                0.5f, 0f, -0.5f, 3f, 1f, 2f, WING_GREEN);

        // Right wing
        Joint rUpper = s.addJoint("R_upper_wing", shoulderMount, -1f, -0.5f, 0f,
                -4f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);
        s.addJoint("R_scapulars", rUpper, 0f, 0f, 0f,
                -3.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);
        Joint rForearm = s.addJoint("R_forearm", rUpper, -4f, 0f, 0f,
                -3f, -0.5f, -1.5f, 3f, 1f, 3f, WING_GREEN);
        s.addJoint("R_secondaries", rForearm, 0f, 0f, 0f,
                -3.5f, 0f, -1f, 3f, 1f, 3f, WING_GREEN);
        Joint rHand = s.addJoint("R_hand", rForearm, -3f, 0f, 0f,
                -3f, -0.5f, -1f, 3f, 1f, 2f, WING_GREEN);
        s.addJoint("R_primaries", rHand, 0f, 0f, 0f,
                -3.5f, 0f, -0.5f, 3f, 1f, 2f, WING_GREEN);

        // Tail
        Joint tailBase = s.addJoint("tail_base", chest, 0f, 0f, 3.5f,
                -1f, -0.5f, 0f, 2f, 1f, 2f, TAIL_YELLOW);
        s.addJoint("tail_fan", tailBase, 0f, 0f, 2f,
                -1f, -0.5f, 0f, 2f, 1f, 4f, TAIL_YELLOW);

        // Left leg
        Joint lThigh = s.addJoint("L_thigh", hip, 0.75f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1f, 1f, LEG_ORANGE);
        Joint lShin = s.addJoint("L_shin", lThigh, 0f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        Joint lTarsus = s.addJoint("L_tarsus", lShin, 0f, 2f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        s.addJoint("L_foot", lTarsus, 0f, 1.5f, 0f,
                -1f, 0f, -1f, 2f, 0.5f, 2f, LEG_ORANGE);

        // Right leg
        Joint rThigh = s.addJoint("R_thigh", hip, -0.75f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1f, 1f, LEG_ORANGE);
        Joint rShin = s.addJoint("R_shin", rThigh, 0f, 1f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        Joint rTarsus = s.addJoint("R_tarsus", rShin, 0f, 2f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        s.addJoint("R_foot", rTarsus, 0f, 1.5f, 0f,
                -1f, 0f, -1f, 2f, 0.5f, 2f, LEG_ORANGE);

        return s;
    }

    static Skeleton buildRaptor() {
        Skeleton s = new Skeleton();
        Joint chest = s.addJoint("chest", null, 0f, 19f, 0f,
                -2.5f, -2.5f, -3f, 5f, 5f, 6f, SPINE_BLUE);
        Joint shoulderMount = s.addJoint("shoulder_mount", chest, 0f, -1f, 0f,
                -1.5f, -0.5f, -1.5f, 3f, 1f, 3f, SPINE_BLUE);
        s.addJoint("torso", chest, 0f, 0f, 3f,
                -2f, -2f, -2.5f, 4f, 4f, 5f, SPINE_BLUE);
        Joint hip = s.addJoint("hip", chest, 0f, 0.5f, 5f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);

        Joint neckLower = s.addJoint("neck_lower", chest, 0f, -1.5f, -1f,
                -1f, -1f, -0.5f, 2f, 2f, 1f, NECK_PINK);
        Joint neckMid = s.addJoint("neck_mid", neckLower, 0f, -1.5f, 0f,
                -1f, -1f, -0.5f, 2f, 2f, 1f, NECK_PINK);
        Joint neckUpper = s.addJoint("neck_upper", neckMid, 0f, -1.5f, 0f,
                -1f, -1f, -0.5f, 2f, 2f, 1f, NECK_PINK);
        Joint head = s.addJoint("head", neckUpper, 0f, -1.5f, 0f,
                -2.5f, -5f, -2.5f, 5f, 5f, 5f, HEAD_PINK);
        s.addJoint("upper_beak", head, 0f, 0f, 0f,
                -0.75f, -3f, -5f, 1.5f, 1.5f, 2.5f, BEAK_WHITE);
        s.addJoint("lower_beak", head, 0f, 0f, 0f,
                -0.75f, -1.5f, -5f, 1.5f, 1.5f, 2.5f, BEAK_WHITE);

        // Left wing (broader)
        Joint lUpper = s.addJoint("L_upper_wing", shoulderMount, 1.5f, -0.5f, 0f,
                0f, -0.5f, -3f, 6f, 1f, 6f, WING_GREEN);
        s.addJoint("L_scapulars", lUpper, 0f, 0f, 0f,
                0.5f, 0f, -2f, 5f, 1f, 5f, WING_GREEN);
        Joint lForearm = s.addJoint("L_forearm", lUpper, 6f, 0f, 0f,
                0f, -0.5f, -2.5f, 5f, 1f, 5f, WING_GREEN);
        s.addJoint("L_secondaries", lForearm, 0f, 0f, 0f,
                0.5f, 0f, -2f, 4f, 1f, 4f, WING_GREEN);
        Joint lHand = s.addJoint("L_hand", lForearm, 5f, 0f, 0f,
                0f, -0.5f, -1.5f, 5f, 1f, 3f, WING_GREEN);
        s.addJoint("L_primaries", lHand, 0f, 0f, 0f,
                0.5f, 0f, -1f, 5f, 1f, 3f, WING_GREEN);

        // Right wing
        Joint rUpper = s.addJoint("R_upper_wing", shoulderMount, -1.5f, -0.5f, 0f,
                -6f, -0.5f, -3f, 6f, 1f, 6f, WING_GREEN);
        s.addJoint("R_scapulars", rUpper, 0f, 0f, 0f,
                -5.5f, 0f, -2f, 5f, 1f, 5f, WING_GREEN);
        Joint rForearm = s.addJoint("R_forearm", rUpper, -6f, 0f, 0f,
                -5f, -0.5f, -2.5f, 5f, 1f, 5f, WING_GREEN);
        s.addJoint("R_secondaries", rForearm, 0f, 0f, 0f,
                -4.5f, 0f, -2f, 4f, 1f, 4f, WING_GREEN);
        Joint rHand = s.addJoint("R_hand", rForearm, -5f, 0f, 0f,
                -5f, -0.5f, -1.5f, 5f, 1f, 3f, WING_GREEN);
        s.addJoint("R_primaries", rHand, 0f, 0f, 0f,
                -5.5f, 0f, -1f, 5f, 1f, 3f, WING_GREEN);

        // Tail
        Joint tailBase = s.addJoint("tail_base", chest, 0f, 0f, 5.5f,
                -1.5f, -0.5f, 0f, 3f, 1f, 3f, TAIL_YELLOW);
        s.addJoint("tail_fan", tailBase, 0f, 0f, 3f,
                -1.5f, -0.5f, 0f, 3f, 1f, 5f, TAIL_YELLOW);

        // Left leg (longer)
        Joint lThigh = s.addJoint("L_thigh", hip, 1f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        Joint lShin = s.addJoint("L_shin", lThigh, 0f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 3f, 1f, LEG_ORANGE);
        Joint lTarsus = s.addJoint("L_tarsus", lShin, 0f, 3f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        s.addJoint("L_foot", lTarsus, 0f, 2f, 0f,
                -1.5f, 0f, -1.5f, 3f, 0.5f, 3f, LEG_ORANGE);

        // Right leg
        Joint rThigh = s.addJoint("R_thigh", hip, -1f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        Joint rShin = s.addJoint("R_shin", rThigh, 0f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 3f, 1f, LEG_ORANGE);
        Joint rTarsus = s.addJoint("R_tarsus", rShin, 0f, 3f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        s.addJoint("R_foot", rTarsus, 0f, 2f, 0f,
                -1.5f, 0f, -1.5f, 3f, 0.5f, 3f, LEG_ORANGE);

        return s;
    }

    static Skeleton buildWaterfowl() {
        Skeleton s = new Skeleton();
        Joint chest = s.addJoint("chest", null, 0f, 19f, 0f,
                -2.5f, -2.5f, -3f, 5f, 5f, 6f, SPINE_BLUE);
        Joint shoulderMount = s.addJoint("shoulder_mount", chest, 0f, -1f, 0f,
                -1.5f, -0.5f, -1.5f, 3f, 1f, 3f, SPINE_BLUE);
        s.addJoint("torso", chest, 0f, 0f, 3f,
                -2.5f, -2.5f, -3f, 5f, 5f, 6f, SPINE_BLUE);
        Joint hip = s.addJoint("hip", chest, 0f, 0.5f, 5f,
                -1.5f, -1.5f, -2f, 3f, 3f, 4f, SPINE_BLUE);

        // Thick neck
        Joint neckLower = s.addJoint("neck_lower", chest, 0f, -1.5f, -1f,
                -1.5f, -1f, -1f, 3f, 2f, 2f, NECK_PINK);
        Joint neckMid = s.addJoint("neck_mid", neckLower, 0f, -1.5f, 0f,
                -1.5f, -1f, -1f, 3f, 2f, 2f, NECK_PINK);
        Joint neckUpper = s.addJoint("neck_upper", neckMid, 0f, -1.5f, 0f,
                -1.5f, -1f, -1f, 3f, 2f, 2f, NECK_PINK);
        Joint head = s.addJoint("head", neckUpper, 0f, -1.5f, 0f,
                -2.5f, -4f, -2.5f, 5f, 4f, 5f, HEAD_PINK);
        s.addJoint("upper_beak", head, 0f, 0f, 0f,
                -1f, -2.5f, -5f, 2f, 1.5f, 3f, BEAK_WHITE);
        s.addJoint("lower_beak", head, 0f, 0f, 0f,
                -1f, -1f, -5f, 2f, 1.5f, 3f, BEAK_WHITE);

        // Medium wings
        Joint lUpper = s.addJoint("L_upper_wing", shoulderMount, 1.5f, -0.5f, 0f,
                0f, -0.5f, -2.5f, 5f, 1f, 5f, WING_GREEN);
        s.addJoint("L_scapulars", lUpper, 0f, 0f, 0f,
                0.5f, 0f, -1.5f, 4f, 1f, 4f, WING_GREEN);
        Joint lForearm = s.addJoint("L_forearm", lUpper, 5f, 0f, 0f,
                0f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);
        s.addJoint("L_secondaries", lForearm, 0f, 0f, 0f,
                0.5f, 0f, -1.5f, 3f, 1f, 3f, WING_GREEN);
        Joint lHand = s.addJoint("L_hand", lForearm, 4f, 0f, 0f,
                0f, -0.5f, -1.5f, 4f, 1f, 3f, WING_GREEN);
        s.addJoint("L_primaries", lHand, 0f, 0f, 0f,
                0.5f, 0f, -1f, 4f, 1f, 3f, WING_GREEN);

        // Right wing
        Joint rUpper = s.addJoint("R_upper_wing", shoulderMount, -1.5f, -0.5f, 0f,
                -5f, -0.5f, -2.5f, 5f, 1f, 5f, WING_GREEN);
        s.addJoint("R_scapulars", rUpper, 0f, 0f, 0f,
                -4.5f, 0f, -1.5f, 4f, 1f, 4f, WING_GREEN);
        Joint rForearm = s.addJoint("R_forearm", rUpper, -5f, 0f, 0f,
                -4f, -0.5f, -2f, 4f, 1f, 4f, WING_GREEN);
        s.addJoint("R_secondaries", rForearm, 0f, 0f, 0f,
                -3.5f, 0f, -1.5f, 3f, 1f, 3f, WING_GREEN);
        Joint rHand = s.addJoint("R_hand", rForearm, -4f, 0f, 0f,
                -4f, -0.5f, -1.5f, 4f, 1f, 3f, WING_GREEN);
        s.addJoint("R_primaries", rHand, 0f, 0f, 0f,
                -4.5f, 0f, -1f, 4f, 1f, 3f, WING_GREEN);

        // Tail
        Joint tailBase = s.addJoint("tail_base", chest, 0f, 0f, 5.5f,
                -1.5f, -0.5f, 0f, 3f, 1f, 3f, TAIL_YELLOW);
        s.addJoint("tail_fan", tailBase, 0f, 0f, 3f,
                -1.5f, -0.5f, 0f, 3f, 1f, 4f, TAIL_YELLOW);

        // Left leg (webbed feet)
        Joint lThigh = s.addJoint("L_thigh", hip, 1f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        Joint lShin = s.addJoint("L_shin", lThigh, 0f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2.5f, 1f, LEG_ORANGE);
        Joint lTarsus = s.addJoint("L_tarsus", lShin, 0f, 2.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        s.addJoint("L_foot", lTarsus, 0f, 2f, 0f,
                -1.5f, 0f, -1.5f, 3f, 0.5f, 3f, LEG_ORANGE);

        // Right leg
        Joint rThigh = s.addJoint("R_thigh", hip, -1f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 1.5f, 1f, LEG_ORANGE);
        Joint rShin = s.addJoint("R_shin", rThigh, 0f, 1.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2.5f, 1f, LEG_ORANGE);
        Joint rTarsus = s.addJoint("R_tarsus", rShin, 0f, 2.5f, 0f,
                -0.5f, 0f, -0.5f, 1f, 2f, 1f, LEG_ORANGE);
        s.addJoint("R_foot", rTarsus, 0f, 2f, 0f,
                -1.5f, 0f, -1.5f, 3f, 0.5f, 3f, LEG_ORANGE);

        return s;
    }

    // =========================================================================
    // Matrix maths (3x3 rotation + translation)
    // =========================================================================

    static double[][] identity() {
        return new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    }

    static double[][] rotX(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
    }

    static double[][] rotY(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
    }

    static double[][] rotZ(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{c, -s, 0}, {s, c, 0}, {0, 0, 1}};
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
                m[0][0]*v[0] + m[0][1]*v[1] + m[0][2]*v[2],
                m[1][0]*v[0] + m[1][1]*v[1] + m[1][2]*v[2],
                m[2][0]*v[0] + m[2][1]*v[1] + m[2][2]*v[2]
        };
    }

    static double[] addVec(double[] a, double[] b) {
        return new double[]{a[0]+b[0], a[1]+b[1], a[2]+b[2]};
    }

    // =========================================================================
    // Forward kinematics
    // =========================================================================

    static void applyPose(Skeleton skel, Map<String, float[]> poseData) {
        for (Joint j : skel.allJoints) {
            j.angleX = 0; j.angleY = 0; j.angleZ = 0;
        }
        for (var entry : poseData.entrySet()) {
            Joint j = skel.jointMap.get(entry.getKey());
            if (j != null) {
                float[] v = entry.getValue();
                j.angleX = v[0]; j.angleY = v[1]; j.angleZ = v[2];
            }
        }
    }

    static void computeFK(Skeleton skel) {
        for (Joint j : skel.allJoints) {
            double[][] parentRot;
            double[] parentPos;
            if (j.parent == null) {
                parentRot = identity();
                parentPos = new double[]{0, 0, 0};
            } else {
                parentRot = j.parent.worldRot;
                parentPos = j.parent.worldPos;
            }
            double[] localOffset = {j.offsetX, j.offsetY, j.offsetZ};
            double[] worldOffset = mulVec(parentRot, localOffset);
            j.worldPos = addVec(parentPos, worldOffset);
            double[][] localRot = mulMat(mulMat(rotX(j.angleX), rotY(j.angleY)), rotZ(j.angleZ));
            j.worldRot = mulMat(parentRot, localRot);
        }
    }

    // =========================================================================
    // Projection + cuboid geometry
    // =========================================================================

    static double[] project(double[] wp, View view) {
        switch (view) {
            case FRONT: return new double[]{wp[0], wp[1]};
            case SIDE:  return new double[]{wp[2], wp[1]};
            case TOP:   return new double[]{wp[0], wp[2]};
            default:    return new double[]{0, 0};
        }
    }

    static double[][] getCuboidCorners(Joint j) {
        float x0 = j.boxOriginX, y0 = j.boxOriginY, z0 = j.boxOriginZ;
        float x1 = x0+j.boxW, y1 = y0+j.boxH, z1 = z0+j.boxD;
        double[][] local = {
                {x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},
                {x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}
        };
        double[][] world = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double[] rotated = mulVec(j.worldRot, local[i]);
            world[i] = addVec(j.worldPos, rotated);
        }
        return world;
    }
}
