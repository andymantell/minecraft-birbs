import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Interactive bird skeleton pose editor with 3-view preview and code export.
 *
 * <p>Usage: {@code javac PoseEditor.java && java PoseEditor}
 *
 * <p>Features:
 * <ul>
 *   <li>3 archetypes: Passerine, Raptor, Waterfowl (different skeleton geometry)</li>
 *   <li>Pose presets loaded from actual game code values</li>
 *   <li>Per-joint sliders (xRot, yRot, zRot) with numeric entry</li>
 *   <li>Real-time 3-view orthographic preview (front, side, top)</li>
 *   <li>Auto-mirroring: edit left joints, right joints follow</li>
 *   <li>Export to Java PoseData.builder() code</li>
 * </ul>
 *
 * <p>No external dependencies — uses only java.awt, javax.swing, javax.imageio.
 */
public class PoseEditor extends JFrame {

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
    // Archetype skeleton builders
    // =========================================================================

    /** Ordered list of LEFT-side joint names that the slider panel controls. */
    static final String[] SPINE_JOINTS = {"chest", "shoulder_mount", "torso", "hip"};
    static final String[] NECK_HEAD_JOINTS = {"neck_lower", "neck_mid", "neck_upper", "head", "upper_beak", "lower_beak"};
    static final String[] LEFT_WING_JOINTS = {"L_upper_wing", "L_scapulars", "L_forearm", "L_secondaries", "L_hand", "L_primaries"};
    static final String[] TAIL_JOINTS = {"tail_base", "tail_fan"};
    static final String[] LEFT_LEG_JOINTS = {"L_thigh", "L_shin", "L_tarsus", "L_foot"};

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
    // Rendering
    // =========================================================================

    enum View { FRONT, SIDE, TOP }

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

    // =========================================================================
    // Pose presets
    // =========================================================================

    /** Pose = map from joint name to {xRot, yRot, zRot}. */
    static Map<String, float[]> pose(Object... args) {
        Map<String, float[]> m = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 4) {
            String name = (String) args[i];
            float x = ((Number) args[i+1]).floatValue();
            float y = ((Number) args[i+2]).floatValue();
            float z = ((Number) args[i+3]).floatValue();
            m.put(name, new float[]{x, y, z});
        }
        return m;
    }

    static void mirror(Map<String, float[]> pose) {
        Map<String, float[]> mirrored = new LinkedHashMap<>();
        for (var entry : pose.entrySet()) {
            String name = entry.getKey();
            float[] v = entry.getValue();
            if (name.startsWith("L_")) {
                String rName = "R_" + name.substring(2);
                if (!pose.containsKey(rName)) {
                    // Lateral wing geometry: yRot flips (fold direction), zRot does NOT (flap direction)
                    // xRot stays the same (pitch is symmetric)
                    mirrored.put(rName, new float[]{v[0], -v[1], v[2]});
                }
            }
        }
        pose.putAll(mirrored);
    }

    /** Named preset: archetype + pose name + joint values. */
    static class Preset {
        final String name;
        final Map<String, float[]> joints;
        // If non-null, this is a cyclic preset: basePose + offsetA (phase=0) or offsetB (phase=1)
        final Map<String, float[]> basePose;    // base pose values (e.g. flying_cruise + legs_tucked)
        final Map<String, float[]> offsetA;     // cyclic offset A (phase = 0)
        final Map<String, float[]> offsetB;     // cyclic offset B (phase = 1)
        final String cyclicName;                // e.g. "wingbeat"
        final String endpointName;              // e.g. "wings_up"

        Preset(String name, Map<String, float[]> joints) {
            this.name = name;
            this.joints = joints;
            this.basePose = null;
            this.offsetA = null;
            this.offsetB = null;
            this.cyclicName = null;
            this.endpointName = null;
        }

        /** Constructor for cyclic presets. joints = combined (base + this endpoint's offset). */
        Preset(String name, Map<String, float[]> joints,
               Map<String, float[]> basePose, Map<String, float[]> offsetA,
               Map<String, float[]> offsetB, String cyclicName, String endpointName) {
            this.name = name;
            this.joints = joints;
            this.basePose = basePose;
            this.offsetA = offsetA;
            this.offsetB = offsetB;
            this.cyclicName = cyclicName;
            this.endpointName = endpointName;
        }

        boolean isCyclic() { return cyclicName != null; }
    }

    static Map<String, List<Preset>> buildPresets() {
        Map<String, List<Preset>> presets = new LinkedHashMap<>();

        // --- Base poses (shared) ---
        List<Preset> base = new ArrayList<>();
        base.add(preset("perched",
                "chest", 0.1f, 0f, 0f,
                "neck_lower", -0.15f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", -0.05f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_scapulars", 0f, -0.2f, 0f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_secondaries", 0f, -0.15f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "L_primaries", 0f, -0.1f, 0f,
                "tail_base", -0.2f, 0f, 0f,
                "L_thigh", 0.1f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.4f, 0f, 0f));
        base.add(preset("alert",
                "chest", 0.0f, 0f, 0f,
                "neck_lower", -0.2f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.15f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.1f, 0f, 0f));
        base.add(preset("flying_cruise",
                "chest", 1.0f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.3f,
                "L_forearm", 0f, 0f, 0f,
                "L_hand", 0f, 0f, 0f,
                "tail_base", -0.65f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f));
        base.add(preset("flying_takeoff",
                "chest", 1.2f, 0f, 0f,
                "torso", 0.2f, 0f, 0f,
                "hip", 0.15f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.6f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.5f,
                "L_forearm", 0f, 0f, 0f,
                "L_hand", 0f, 0f, 0f,
                "tail_base", -0.8f, 0f, 0f,
                "tail_fan", -0.2f, 0f, 0f));
        base.add(preset("flying_land",
                "chest", -0.3f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.1f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.8f,
                "L_forearm", 0f, 0f, -0.1f,
                "L_hand", 0f, 0f, -0.05f,
                "tail_base", -0.8f, 0f, 0f,
                "L_thigh", -0.3f, 0f, 0f,
                "L_shin", -0.5f, 0f, 0f));
        base.add(preset("tucked",
                "chest", 0.15f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", 0.3f, 0f, 0.4f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", 0.1f, 0f, 0f));

        // --- Shared cyclic base poses ---
        // flying_cruise + legs_tucked (used as base for wingbeat cyclics)
        Map<String, float[]> flightBase = new LinkedHashMap<>();
        flightBase.put("chest",        new float[]{1.0f, 0f, 0f});
        flightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        flightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        flightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        flightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        flightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        flightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        flightBase.put("L_upper_wing", new float[]{0f, 0f, -0.1f});
        flightBase.put("L_forearm",    new float[]{0f, 0f, 0f});
        flightBase.put("L_hand",       new float[]{0f, 0f, 0f});
        flightBase.put("tail_base",    new float[]{-0.65f, 0f, 0f});
        flightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        flightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        flightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        flightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        flightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(flightBase);

        // WINGBEAT offsets (from BaseBirdPoses.WINGBEAT)
        Map<String, float[]> wingbeatOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.4f,
            "L_forearm",     0f, 0f, -0.15f,
            "L_hand",        0f, 0f, -0.1f,
            "L_scapulars",   0f, 0f, -0.1f,
            "L_secondaries", 0f, 0f, -0.08f,
            "L_primaries",   0f, 0f, -0.06f);
        Map<String, float[]> wingbeatOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.4f,
            "L_forearm",     0f, 0f, 0.1f,
            "L_hand",        0f, 0f, 0.08f,
            "L_scapulars",   0f, 0f, 0.08f,
            "L_secondaries", 0f, 0f, 0.06f,
            "L_primaries",   0f, 0f, 0.05f);

        // WALK_CYCLE offsets (from BaseBirdPoses.WALK_CYCLE)
        Map<String, float[]> walkOffA = pose(
            "L_thigh",  -0.3f, 0f, 0f,
            "L_shin",    0.4f, 0f, 0f,
            "L_tarsus", -0.2f, 0f, 0f,
            "R_thigh",   0.3f, 0f, 0f,
            "R_shin",   -0.1f, 0f, 0f,
            "R_tarsus",  0.1f, 0f, 0f);
        Map<String, float[]> walkOffB = pose(
            "L_thigh",   0.3f, 0f, 0f,
            "L_shin",   -0.1f, 0f, 0f,
            "L_tarsus",  0.1f, 0f, 0f,
            "R_thigh",  -0.3f, 0f, 0f,
            "R_shin",    0.4f, 0f, 0f,
            "R_tarsus", -0.2f, 0f, 0f);
        Map<String, float[]> walkBase = new LinkedHashMap<>();  // neutral stand

        // HOP offsets (from PasserinePoses.HOP)
        Map<String, float[]> hopOffA = pose(
            "chest",    0.15f, 0f, 0f,
            "L_thigh",  0.3f,  0f, 0f,
            "L_shin",   0.5f,  0f, 0f,
            "L_tarsus", -0.4f, 0f, 0f,
            "R_thigh",  0.3f,  0f, 0f,
            "R_shin",   0.5f,  0f, 0f,
            "R_tarsus", -0.4f, 0f, 0f);
        Map<String, float[]> hopOffB = pose(
            "chest",    -0.1f,  0f, 0f,
            "L_thigh",  -0.15f, 0f, 0f,
            "L_shin",   -0.1f,  0f, 0f,
            "L_tarsus",  0.1f,  0f, 0f,
            "R_thigh",  -0.15f, 0f, 0f,
            "R_shin",   -0.1f,  0f, 0f,
            "R_tarsus",  0.1f,  0f, 0f);
        Map<String, float[]> hopBase = new LinkedHashMap<>();  // neutral stand

        // RAPTOR_WINGBEAT offsets (from RaptorPoses.RAPTOR_WINGBEAT)
        Map<String, float[]> raptorWingOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.5f,
            "L_forearm",     0f, 0f, -0.2f,
            "L_hand",        0f, 0f, -0.15f,
            "L_scapulars",   0f, 0f, -0.12f,
            "L_secondaries", 0f, 0f, -0.1f,
            "L_primaries",   0f, 0f, -0.08f);
        Map<String, float[]> raptorWingOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.5f,
            "L_forearm",     0f, 0f, 0.15f,
            "L_hand",        0f, 0f, 0.1f,
            "L_scapulars",   0f, 0f, 0.1f,
            "L_secondaries", 0f, 0f, 0.08f,
            "L_primaries",   0f, 0f, 0.06f);

        // Raptor flight base (soar pose)
        Map<String, float[]> raptorFlightBase = new LinkedHashMap<>();
        raptorFlightBase.put("chest",        new float[]{1.1f, 0f, 0f});
        raptorFlightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        raptorFlightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        raptorFlightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        raptorFlightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        raptorFlightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        raptorFlightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        raptorFlightBase.put("L_upper_wing", new float[]{0f, 0f, -0.4f});
        raptorFlightBase.put("L_forearm",    new float[]{0f, 0f, -0.05f});
        raptorFlightBase.put("L_hand",       new float[]{0f, 0f, -0.03f});
        raptorFlightBase.put("tail_base",    new float[]{-0.7f, 0f, 0f});
        raptorFlightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        raptorFlightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        raptorFlightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        raptorFlightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        raptorFlightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(raptorFlightBase);

        // WATERFOWL_WINGBEAT offsets (from WaterfowlPoses.WATERFOWL_WINGBEAT)
        Map<String, float[]> waterfowlWingOffA = offsetPose(
            "L_upper_wing",  0f, 0f, -0.5f,
            "L_forearm",     0f, 0f, -0.18f,
            "L_hand",        0f, 0f, -0.12f,
            "L_scapulars",   0f, 0f, -0.1f,
            "L_secondaries", 0f, 0f, -0.08f,
            "L_primaries",   0f, 0f, -0.06f);
        Map<String, float[]> waterfowlWingOffB = offsetPose(
            "L_upper_wing",  0f, 0f, 0.5f,
            "L_forearm",     0f, 0f, 0.12f,
            "L_hand",        0f, 0f, 0.1f,
            "L_scapulars",   0f, 0f, 0.08f,
            "L_secondaries", 0f, 0f, 0.06f,
            "L_primaries",   0f, 0f, 0.05f);

        // Waterfowl flight base (flying_cruise)
        Map<String, float[]> waterfowlFlightBase = new LinkedHashMap<>();
        waterfowlFlightBase.put("chest",        new float[]{1.0f, 0f, 0f});
        waterfowlFlightBase.put("torso",        new float[]{0.15f, 0f, 0f});
        waterfowlFlightBase.put("hip",          new float[]{0.1f, 0f, 0f});
        waterfowlFlightBase.put("neck_lower",   new float[]{0.05f, 0f, -0.05f});
        waterfowlFlightBase.put("neck_mid",     new float[]{0.0f, 0f, 0f});
        waterfowlFlightBase.put("neck_upper",   new float[]{0.0f, 0f, 0f});
        waterfowlFlightBase.put("head",         new float[]{-0.5f, 0f, 0f});
        waterfowlFlightBase.put("L_upper_wing", new float[]{0f, 0f, -0.1f});
        waterfowlFlightBase.put("L_forearm",    new float[]{0f, 0f, 0f});
        waterfowlFlightBase.put("L_hand",       new float[]{0f, 0f, 0f});
        waterfowlFlightBase.put("tail_base",    new float[]{-0.65f, 0f, 0f});
        waterfowlFlightBase.put("tail_fan",     new float[]{-0.15f, 0f, 0f});
        waterfowlFlightBase.put("L_thigh",      new float[]{-1.5f, 0f, 0f});
        waterfowlFlightBase.put("L_shin",       new float[]{-2.5f, 0f, 0f});
        waterfowlFlightBase.put("L_tarsus",     new float[]{2.0f, 0f, 0f});
        waterfowlFlightBase.put("L_foot",       new float[]{-0.8f, 0f, 0f});
        mirror(waterfowlFlightBase);

        // --- Passerine-specific ---
        List<Preset> passerine = new ArrayList<>(base);
        passerine.add(preset("forage",
                "chest", 0.3f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", 0.4f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.15f, 0f, 0f,
                "L_thigh", 0.2f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.4f, 0f, 0f));
        // Cyclic presets for Passerine
        passerine.addAll(cyclicPresets("wingbeat", "wings_up", "wings_down",
                flightBase, wingbeatOffA, wingbeatOffB));
        passerine.addAll(cyclicPresets("walk", "legs_forward", "legs_back",
                walkBase, walkOffA, walkOffB));
        passerine.addAll(cyclicPresets("hop", "crouch", "spring",
                hopBase, hopOffA, hopOffB));
        presets.put("Passerine", passerine);

        // --- Raptor-specific ---
        List<Preset> raptor = new ArrayList<>(base);
        raptor.add(preset("soar",
                "chest", 1.1f, 0f, 0f,
                "torso", 0.15f, 0f, 0f,
                "hip", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.5f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.4f,
                "L_forearm", 0f, 0f, -0.05f,
                "L_hand", 0f, 0f, -0.03f,
                "tail_base", -0.7f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f,
                "L_thigh", -1.5f, 0f, 0f,
                "L_shin", -2.5f, 0f, 0f,
                "L_tarsus", 2.0f, 0f, 0f,
                "L_foot", -0.8f, 0f, 0f));
        raptor.add(preset("stoop",
                "chest", 1.0f, 0f, 0f,
                "neck_lower", -0.3f, 0f, 0f,
                "neck_mid", -0.15f, 0f, 0f,
                "neck_upper", -0.15f, 0f, 0f,
                "head", -0.4f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.1f,
                "L_forearm", 0f, 2.5f, 0f,
                "L_hand", 0f, -2.3f, 0f,
                "tail_base", -0.1f, 0f, 0f,
                "tail_fan", 0.1f, 0f, 0f,
                "L_thigh", -1.5f, 0f, 0f,
                "L_shin", -2.5f, 0f, 0f,
                "L_tarsus", 2.0f, 0f, 0f,
                "L_foot", -0.8f, 0f, 0f));
        raptor.add(preset("hover",
                "chest", 0.6f, 0f, 0f,
                "torso", 0.1f, 0f, 0f,
                "neck_lower", 0.05f, 0f, -0.05f,
                "neck_mid", 0.0f, 0f, 0f,
                "neck_upper", 0.0f, 0f, 0f,
                "head", -0.3f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.5f,
                "L_forearm", 0f, 0f, -0.05f,
                "L_hand", 0f, 0f, -0.03f,
                "tail_base", -0.5f, 0f, 0f,
                "tail_fan", -0.15f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.1f, 0f, 0f,
                "L_tarsus", -0.1f, 0f, 0f));
        raptor.add(preset("raptor_perch",
                "chest", 0.35f, 0f, 0f,
                "neck_lower", -0.15f, 0f, 0f,
                "neck_mid", -0.1f, 0f, 0f,
                "neck_upper", -0.1f, 0f, 0f,
                "head", -0.15f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", 0.25f, 0f, 0f,
                "L_thigh", 0.15f, 0f, 0f,
                "L_shin", 0.4f, 0f, 0f,
                "L_tarsus", -0.5f, 0f, 0f));
        raptor.add(preset("strike",
                "chest", 0.4f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.2f, 0f, 0f,
                "L_upper_wing", 0f, 0f, -0.6f,
                "L_forearm", 0f, 0f, -0.1f,
                "L_hand", 0f, 0f, -0.05f,
                "tail_base", -0.6f, 0f, 0f,
                "L_thigh", -0.6f, 0f, 0f,
                "L_shin", -0.3f, 0f, 0f,
                "L_tarsus", 0.2f, 0f, 0f,
                "L_foot", -0.4f, 0f, 0f));
        // Cyclic presets for Raptor
        raptor.addAll(cyclicPresets("raptor_wingbeat", "up", "down",
                raptorFlightBase, raptorWingOffA, raptorWingOffB));
        presets.put("Raptor", raptor);

        // --- Waterfowl-specific ---
        List<Preset> waterfowl = new ArrayList<>(base);
        waterfowl.add(preset("swim",
                "chest", 0.0f, 0f, 0f,
                "neck_lower", -0.1f, 0f, 0f,
                "neck_mid", -0.05f, 0f, 0f,
                "neck_upper", -0.05f, 0f, 0f,
                "head", -0.05f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -0.17f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.4f, 0f, 0f,
                "L_tarsus", -0.2f, 0f, 0f));
        waterfowl.add(preset("dabble",
                "chest", 1.3f, 0f, 0f,
                "neck_lower", 0.2f, 0f, 0f,
                "neck_mid", 0.1f, 0f, 0f,
                "neck_upper", 0.1f, 0f, 0f,
                "head", 0.2f, 0f, 0f,
                "L_upper_wing", 0f, -1.5f, 0.3f,
                "L_forearm", 0f, 2.2f, 0f,
                "L_hand", 0f, -1.8f, 0f,
                "tail_base", -1.2f, 0f, 0f,
                "tail_fan", -0.3f, 0f, 0f,
                "L_thigh", 0.3f, 0f, 0f,
                "L_shin", 0.3f, 0f, 0f,
                "L_tarsus", -0.1f, 0f, 0f));
        // Cyclic presets for Waterfowl
        waterfowl.addAll(cyclicPresets("waterfowl_wingbeat", "up", "down",
                waterfowlFlightBase, waterfowlWingOffA, waterfowlWingOffB));
        presets.put("Waterfowl", waterfowl);

        return presets;
    }

    static Preset preset(String name, Object... args) {
        Map<String, float[]> joints = pose(args);
        mirror(joints);
        return new Preset(name, joints);
    }

    /**
     * Build a pair of cyclic presets (endpoint A and B) from a base pose and two offset maps.
     * The returned list has [endpointA, endpointB].
     * @param cyclicName  e.g. "wingbeat"
     * @param nameA       e.g. "wings_up"  (phase=0 endpoint name used in the combo)
     * @param nameB       e.g. "wings_down" (phase=1 endpoint name)
     * @param basePose    base pose map (values applied before the offset)
     * @param offsetA     offset map for endpoint A (raw cyclic offset, no base mixed in)
     * @param offsetB     offset map for endpoint B
     */
    static List<Preset> cyclicPresets(String cyclicName, String nameA, String nameB,
                                       Map<String, float[]> basePose,
                                       Map<String, float[]> offsetA,
                                       Map<String, float[]> offsetB) {
        // Combined = base + offsetA for the A preset joints shown in sliders
        Map<String, float[]> combinedA = mergePoseOffset(basePose, offsetA);
        Map<String, float[]> combinedB = mergePoseOffset(basePose, offsetB);

        String displayNameA = cyclicName + ": " + nameA;
        String displayNameB = cyclicName + ": " + nameB;

        Preset pA = new Preset(displayNameA, combinedA, basePose, offsetA, offsetB, cyclicName, nameA);
        Preset pB = new Preset(displayNameB, combinedB, basePose, offsetA, offsetB, cyclicName, nameB);
        return List.of(pA, pB);
    }

    /** Merge a base pose with an offset map: result[joint] = base + offset (per-component). */
    static Map<String, float[]> mergePoseOffset(Map<String, float[]> base, Map<String, float[]> offset) {
        Map<String, float[]> result = new LinkedHashMap<>(base);
        for (var entry : offset.entrySet()) {
            String name = entry.getKey();
            float[] off = entry.getValue();
            float[] existing = result.get(name);
            if (existing != null) {
                result.put(name, new float[]{existing[0] + off[0], existing[1] + off[1], existing[2] + off[2]});
            } else {
                result.put(name, new float[]{off[0], off[1], off[2]});
            }
        }
        return result;
    }

    /** Build a pose offset map from flat args (like pose()), then auto-mirror L_ entries. */
    static Map<String, float[]> offsetPose(Object... args) {
        Map<String, float[]> m = pose(args);
        mirror(m);
        return m;
    }

    // =========================================================================
    // Preview panel
    // =========================================================================

    class PreviewPanel extends JPanel {
        final float SCALE = 14f;

        // Per-panel zoom levels
        float zoomFront = 1.0f, zoomSide = 1.0f, zoomTop = 1.0f, zoom3D = 1.0f;

        // Per-panel pan offsets (in screen pixels)
        float panFrontX = 0, panFrontY = 0;
        float panSideX  = 0, panSideY  = 0;
        float panTopX   = 0, panTopY   = 0;
        float pan3DX    = 0, pan3DY    = 0;

        // 3D camera rotation (mouse drag)
        double camYaw = 0.4;    // initial slight angle
        double camPitch = 0.3;
        int dragStartX, dragStartY;
        double dragStartYaw, dragStartPitch;

        // Selection state
        String selectedJoint = null;

        // Drag handle state: 0=none, 1=xRot(red), 2=yRot(green), 3=zRot(blue)
        int draggingAxis = 0;
        String draggingJoint = null;
        int handleDragStartX, handleDragStartY;
        float handleDragStartValue;
        // Current drag position for tooltip
        int dragCurX, dragCurY;

        // Hovered axis for cursor feedback: 0=none, 1=xRot, 2=yRot, 3=zRot
        int hoveredAxis = 0;

        // Whether we are dragging the 3D camera
        boolean dragging3DCamera = false;

        // IK drag state
        boolean ikDragging = false;
        String ikJointName = null;         // the joint being dragged
        int ikViewQuadrant = -1;           // 0=FRONT, 1=SIDE, 2=TOP, 3=3D
        List<String> ikChain = new ArrayList<>();  // chain from effector up to root (inclusive)
        int ikTargetScreenX, ikTargetScreenY;      // current mouse target in screen coords
        boolean ikMirrorTarget = false;             // true when R_ wing/leg clicked — mirror target X

        PreviewPanel() {
            setPreferredSize(new Dimension(900, 600));
            setOpaque(true);
            setBackground(new Color(245, 245, 240));
            setMinimumSize(new Dimension(600, 400));

            // Mouse wheel zoom per panel — zooms toward mouse cursor position
            addMouseWheelListener(e -> {
                int w = getWidth(), h = getHeight();
                int cellW = w / 2, cellH = h / 2;
                int mx = e.getX(), my = e.getY();
                boolean rightCol = mx > cellW;
                boolean bottomRow = my > cellH;
                float factor = e.getWheelRotation() < 0 ? 1.1f : 0.9f;

                // Panel centre in screen coords
                int panelCX = (rightCol ? 1 : 0) * cellW + cellW / 2;
                int panelCY = (bottomRow ? 1 : 0) * cellH + cellH / 2;
                // Mouse position relative to panel centre
                float relX = mx - panelCX;
                float relY = my - panelCY;

                if (!rightCol && !bottomRow) {
                    float oldZoom = zoomFront;
                    zoomFront = Math.max(0.3f, Math.min(5.0f, zoomFront * factor));
                    float ratio = zoomFront / oldZoom;
                    panFrontX = panFrontX * ratio + relX * (1 - ratio);
                    panFrontY = panFrontY * ratio + relY * (1 - ratio);
                } else if (rightCol && !bottomRow) {
                    float oldZoom = zoomSide;
                    zoomSide = Math.max(0.3f, Math.min(5.0f, zoomSide * factor));
                    float ratio = zoomSide / oldZoom;
                    panSideX = panSideX * ratio + relX * (1 - ratio);
                    panSideY = panSideY * ratio + relY * (1 - ratio);
                } else if (!rightCol && bottomRow) {
                    float oldZoom = zoomTop;
                    zoomTop = Math.max(0.3f, Math.min(5.0f, zoomTop * factor));
                    float ratio = zoomTop / oldZoom;
                    panTopX = panTopX * ratio + relX * (1 - ratio);
                    panTopY = panTopY * ratio + relY * (1 - ratio);
                } else {
                    float oldZoom = zoom3D;
                    zoom3D = Math.max(0.3f, Math.min(5.0f, zoom3D * factor));
                    float ratio = zoom3D / oldZoom;
                    pan3DX = pan3DX * ratio + relX * (1 - ratio);
                    pan3DY = pan3DY * ratio + relY * (1 - ratio);
                }
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int w = getWidth(), h = getHeight();
                    int cellW = w / 2, cellH = h / 2;
                    int mx = e.getX(), my = e.getY();

                    // First check if clicking on a rotation handle
                    if (selectedJoint != null) {
                        int handleHit = hitTestHandles(mx, my, cellW, cellH);
                        if (handleHit > 0) {
                            captureState();  // snapshot BEFORE drag starts
                            draggingAxis = handleHit;
                            draggingJoint = selectedJoint;
                            handleDragStartX = mx;
                            handleDragStartY = my;
                            dragCurX = mx;
                            dragCurY = my;
                            // Get the current rotation value for this axis
                            String sliderName = draggingJoint.startsWith("R_") ?
                                    "L_" + draggingJoint.substring(2) : draggingJoint;
                            JointSliderGroup grp = sliderGroups.get(sliderName);
                            if (grp != null) {
                                handleDragStartValue = draggingAxis == 1 ? grp.getX() :
                                        draggingAxis == 2 ? grp.getY() : grp.getZ();
                            }
                            return;
                        }
                    }

                    // Hit test for joint under cursor — used both for IK drag and selection
                    Map<String, float[]> poseData = getCurrentPose();
                    applyPose(skeleton, poseData);
                    computeFK(skeleton);

                    String hit = hitTestJoint(mx, my, cellW, cellH);

                    // If clicking on a joint body (not a handle), start IK drag
                    if (hit != null) {
                        captureState();  // snapshot BEFORE IK starts
                        ikDragging = true;
                        // Always solve on L_ side — mirror the target X when R_ is clicked
                        String ikTarget = hit.startsWith("R_") ? "L_" + hit.substring(2) : hit;
                        ikJointName = ikTarget;
                        ikMirrorTarget = hit.startsWith("R_");  // flag to negate target X
                        ikViewQuadrant = getQuadrant(mx, my, cellW, cellH);
                        ikChain = buildIkChain(ikTarget);
                        ikTargetScreenX = mx;
                        ikTargetScreenY = my;
                        selectedJoint = hit;
                        updateSliderVisibility();
                        repaint();
                        return;
                    }

                    // Check for 3D camera drag (bottom-right, no joint hit)
                    if (mx > cellW && my > cellH) {
                        dragging3DCamera = true;
                        dragStartX = mx;
                        dragStartY = my;
                        dragStartYaw = camYaw;
                        dragStartPitch = camPitch;
                    }

                    // No joint hit — deselect
                    selectedJoint = hit;
                    updateSliderVisibility();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (ikDragging) {
                        updateExportText();
                        if (editingCyclic) updateCurrentEndpointFromSliders();
                        ikDragging = false;
                        ikJointName = null;
                        ikChain.clear();
                    }
                    draggingAxis = 0;
                    draggingJoint = null;
                    dragging3DCamera = false;
                    hoveredAxis = 0;
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (selectedJoint != null) {
                        int w = getWidth(), h = getHeight();
                        int cellW = w / 2, cellH = h / 2;
                        int hit = hitTestHandles(e.getX(), e.getY(), cellW, cellH);
                        if (hit != hoveredAxis) {
                            hoveredAxis = hit;
                            repaint();
                        }
                        if (hit > 0) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    } else {
                        if (hoveredAxis != 0) {
                            hoveredAxis = 0;
                            repaint();
                        }
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // IK drag
                    if (ikDragging && ikJointName != null) {
                        int w = getWidth(), h = getHeight();
                        int cellW = w / 2, cellH = h / 2;
                        ikTargetScreenX = e.getX();
                        ikTargetScreenY = e.getY();

                        // Convert screen position to world coordinates
                        Joint draggedJoint = skeleton.jointMap.get(ikJointName);
                        if (draggedJoint != null && !ikChain.isEmpty()) {
                            double[] targetWorld = screenToWorld(
                                    ikTargetScreenX, ikTargetScreenY,
                                    ikViewQuadrant, cellW, cellH, draggedJoint);
                            // Mirror target X when user clicked R_ side — solve on L_ side
                            if (ikMirrorTarget) {
                                targetWorld[0] = -targetWorld[0];
                            }
                            solveIK(targetWorld, 8);
                            syncIkSliderFields();
                            // Force R_ joints to mirror L_ values before repaint
                            Map<String, float[]> finalPose = getCurrentPose();
                            applyPose(skeleton, finalPose);
                            computeFK(skeleton);
                        }
                        repaint();
                        return;
                    }

                    // Handle rotation handle drag
                    if (draggingAxis > 0 && draggingJoint != null) {
                        int dx = e.getX() - handleDragStartX;
                        int dy = e.getY() - handleDragStartY;
                        dragCurX = e.getX();
                        dragCurY = e.getY();
                        // Compute drag distance based on axis
                        int dragDist;
                        if (draggingAxis == 1) dragDist = dx;        // red: horizontal
                        else if (draggingAxis == 2) dragDist = -dy;  // green: vertical (invert)
                        else dragDist = (dx - dy) / 2;               // blue: diagonal
                        float newVal = handleDragStartValue + dragDist * 0.01f;
                        newVal = Math.max(-(float)Math.PI, Math.min((float)Math.PI, newVal));

                        // Update the slider
                        String sliderName = draggingJoint.startsWith("R_") ?
                                "L_" + draggingJoint.substring(2) : draggingJoint;
                        JointSliderGroup grp = sliderGroups.get(sliderName);
                        if (grp != null) {
                            if (draggingAxis == 1) {
                                grp.xSlider.setValue(Math.round(newVal * 100));
                                grp.xField.setText(String.format("%.2f", newVal));
                            } else if (draggingAxis == 2) {
                                grp.ySlider.setValue(Math.round(newVal * 100));
                                grp.yField.setText(String.format("%.2f", newVal));
                            } else {
                                grp.zSlider.setValue(Math.round(newVal * 100));
                                grp.zField.setText(String.format("%.2f", newVal));
                            }
                        }
                        repaint();
                        return;
                    }

                    // 3D camera drag
                    if (dragging3DCamera) {
                        camYaw = dragStartYaw + (e.getX() - dragStartX) * 0.01;
                        camPitch = dragStartPitch + (e.getY() - dragStartY) * 0.01;
                        camPitch = Math.max(-1.5, Math.min(1.5, camPitch));
                        repaint();
                    }
                }
            });
        }

        /** Determine which panel quadrant a screen point is in: 0=FRONT, 1=SIDE, 2=TOP, 3=3D */
        int getQuadrant(int mx, int my, int cellW, int cellH) {
            boolean right = mx > cellW;
            boolean bottom = my > cellH;
            if (!right && !bottom) return 0;
            if (right && !bottom) return 1;
            if (!right && bottom) return 2;
            return 3;
        }

        float zoomForQuadrant(int q) {
            return switch (q) { case 0 -> zoomFront; case 1 -> zoomSide; case 2 -> zoomTop; default -> zoom3D; };
        }

        float panXForQuadrant(int q) {
            return switch (q) { case 0 -> panFrontX; case 1 -> panSideX; case 2 -> panTopX; default -> pan3DX; };
        }

        float panYForQuadrant(int q) {
            return switch (q) { case 0 -> panFrontY; case 1 -> panSideY; case 2 -> panTopY; default -> pan3DY; };
        }

        // ----------------------------------------------------------------
        // IK chain definitions
        // ----------------------------------------------------------------

        /** Returns the name of the chain root for a given joint name. */
        String ikChainRoot(String jointName) {
            // Wing joints → shoulder_mount
            if (jointName.endsWith("_primaries") || jointName.endsWith("_hand") ||
                    jointName.endsWith("_secondaries") || jointName.endsWith("_forearm") ||
                    jointName.endsWith("_scapulars") || jointName.endsWith("_upper_wing")) {
                return "shoulder_mount";
            }
            // Leg joints → hip
            if (jointName.endsWith("_foot") || jointName.endsWith("_tarsus") ||
                    jointName.endsWith("_shin") || jointName.endsWith("_thigh")) {
                return "hip";
            }
            // Neck joints → chest (short chain to prevent wild spinning)
            if (jointName.equals("neck_upper") || jointName.equals("neck_mid") ||
                    jointName.equals("neck_lower")) {
                return "chest";
            }
            // Head/beak → neck_lower only (2-joint chain max, prevents spinning)
            if (jointName.equals("head") || jointName.equals("lower_beak") ||
                    jointName.equals("upper_beak")) {
                return "neck_lower";
            }
            // Tail joints → short chains (don't include chest!)
            if (jointName.equals("tail_fan")) {
                return "tail_base";
            }
            if (jointName.equals("tail_base")) {
                return "tail_base";  // 1-joint chain — just rotates itself
            }
            // Spine joints → chest
            if (jointName.equals("hip") || jointName.equals("torso") ||
                    jointName.equals("shoulder_mount")) {
                return "chest";
            }
            return null; // chest itself or unknown — no chain
        }

        /**
         * Build the IK chain from the dragged joint back to (and including) the chain root.
         * The list is ordered: [root, ..., parent_of_dragged, dragged_joint].
         * The CCD solver walks from index (size-2) down to 0.
         */
        List<String> buildIkChain(String draggedJointName) {
            String root = ikChainRoot(draggedJointName);
            if (root == null) return new ArrayList<>();

            List<String> chain = new ArrayList<>();
            chain.add(draggedJointName);

            Joint j = skeleton.jointMap.get(draggedJointName);
            while (j != null && !j.name.equals(root)) {
                if (j.parent == null) break;
                chain.add(0, j.parent.name);
                if (j.parent.name.equals(root)) break;
                j = j.parent;
            }
            // Ensure root is the first element
            if (!chain.isEmpty() && !chain.get(0).equals(root)) {
                chain.add(0, root);
            }
            return chain;
        }

        // ----------------------------------------------------------------
        // IK solver
        // ----------------------------------------------------------------

        /**
         * Convert a screen position back to world coordinates for a 2D orthographic view.
         * Returns world XYZ with the unused axis taken from the dragged joint's current world pos.
         */
        double[] screenToWorld(int sx, int sy, int q, int cellW, int cellH, Joint draggedJoint) {
            float es = SCALE * zoomForQuadrant(q);
            float px = panXForQuadrant(q);
            float py = panYForQuadrant(q);
            int col = (q == 1 || q == 3) ? 1 : 0;
            int row = (q == 2 || q == 3) ? 1 : 0;

            // Inverse of toScreenX/toScreenY:
            // sx = col*cellW + cellW/2 + worldCoord * es + px
            // => worldCoord = (sx - col*cellW - cellW/2 - px) / es
            double wA = (sx - col * cellW - cellW / 2.0 - px) / es;
            double wB = (sy - row * cellH - cellH / 2.0 - py) / es + 19.0;

            double wx = draggedJoint.worldPos[0];
            double wy = draggedJoint.worldPos[1];
            double wz = draggedJoint.worldPos[2];

            switch (q) {
                case 0 -> { wx = wA; wy = wB; }           // FRONT: screen=(worldX, worldY)
                case 1 -> { wz = wA; wy = wB; }           // SIDE:  screen=(worldZ, worldY)
                case 2 -> { wx = wA; wz = wB; }           // TOP:   screen=(worldX, worldZ)
                default -> {
                    // 3D view: project target onto plane perpendicular to camera at dragged joint's depth
                    // Approximate by using the two visible axes for the current camera angle
                    // Use the same simple approach: treat 3D like FRONT (worldX, worldY) for simplicity
                    wx = wA; wy = wB;
                }
            }
            return new double[]{wx, wy, wz};
        }

        /**
         * One CCD step: rotate the given joint so the end effector moves toward the target.
         * Operates in 2D in the plane of the view quadrant.
         * Returns the slider-name equivalent (L_ prefix normalised) of the joint that was changed.
         */
        void ccdStep(Joint joint, double[] endEffectorPos, double[] targetPos, int q) {
            double[] jp = joint.worldPos;

            double ex = endEffectorPos[0] - jp[0];
            double ey = endEffectorPos[1] - jp[1];
            double ez = endEffectorPos[2] - jp[2];

            double tx = targetPos[0] - jp[0];
            double ty = targetPos[1] - jp[1];
            double tz = targetPos[2] - jp[2];

            double angleToEnd, angleToTarget, delta;
            float damping = 0.12f;
            float maxStep = 0.08f;

            // Map the joint name to the slider name (L_ for mirrored joints)
            String sliderName = joint.name.startsWith("R_") ? "L_" + joint.name.substring(2) : joint.name;
            JointSliderGroup grp = sliderGroups.get(sliderName);
            if (grp == null) return;

            // Use cross product to determine rotation direction — robust regardless of geometry orientation
            switch (q) {
                case 0 -> {  // FRONT view — XY plane: adjust zRot
                    // Cross product Z component: ex*ty - ey*tx (positive = rotate CCW in XY = positive zRot)
                    double cross = ex * ty - ey * tx;
                    double mag = Math.sqrt((ex*ex+ey*ey) * (tx*tx+ty*ty));
                    if (mag > 0.001) {
                        delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                        joint.angleZ = clampAngle(joint.angleZ + (float) delta);
                    }
                }
                case 1 -> {  // SIDE view — ZY plane: adjust xRot
                    // Cross product X component (negated for correct rotation sense)
                    double cross = -(ez * ty - ey * tz);
                    double mag = Math.sqrt((ez*ez+ey*ey) * (tz*tz+ty*ty));
                    if (mag > 0.001) {
                        delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                        joint.angleX = clampAngle(joint.angleX + (float) delta);
                    }
                }
                case 2 -> {  // TOP view — XZ plane: adjust yRot
                    // Cross product Y component: ex*tz - ez*tx (negative because Y is down)
                    double cross = -(ex * tz - ez * tx);
                    double mag = Math.sqrt((ex*ex+ez*ez) * (tx*tx+tz*tz));
                    if (mag > 0.001) {
                        delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                        joint.angleY = clampAngle(joint.angleY + (float) delta);
                    }
                }
                default -> {  // 3D view: combine front + side
                    double cross0 = ex * ty - ey * tx;
                    double mag0 = Math.sqrt((ex*ex+ey*ey) * (tx*tx+ty*ty));
                    if (mag0 > 0.001) {
                        delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross0 / mag0))) * damping, maxStep);
                        joint.angleZ = clampAngle(joint.angleZ + (float) delta);
                    }
                    double cross1 = ez * ty - ey * tz;
                    double mag1 = Math.sqrt((ez*ez+ey*ey) * (tz*tz+ty*ty));
                    if (mag1 > 0.001) {
                        double d2 = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross1 / mag1))) * damping, maxStep);
                        joint.angleX = clampAngle(joint.angleX + (float) d2);
                    }
                }
            }

            // Enforce joint constraints — prevent unrealistic rotations
            // Only constrain the upper_wing to prevent body-wrapping; other joints are free
            if (joint.name.contains("upper_wing")) {
                float limit = 1.2f;  // ~69 degrees
                joint.angleX = Math.max(-limit, Math.min(limit, joint.angleX));
                joint.angleY = Math.max(-limit, Math.min(limit, joint.angleY));
                joint.angleZ = Math.max(-limit, Math.min(limit, joint.angleZ));
            }
        }

        double clampDelta(double d, float max) {
            return Math.max(-max, Math.min(max, d));
        }

        double normaliseAngle(double a) {
            while (a >  Math.PI) a -= 2 * Math.PI;
            while (a < -Math.PI) a += 2 * Math.PI;
            return a;
        }

        float clampAngle(float a) {
            return Math.max(-(float) Math.PI, Math.min((float) Math.PI, a));
        }

        /**
         * Run full CCD solve: iterate multiple times, each time walking from the joint
         * just above the end effector toward the root, adjusting each joint to point
         * the chain at the target.
         */
        void solveIK(double[] targetWorldPos, int iterations) {
            Joint endEffector = skeleton.jointMap.get(ikJointName);
            if (endEffector == null || ikChain.isEmpty()) return;

            // First, apply current slider state to skeleton
            Map<String, float[]> poseData = getCurrentPose();
            applyPose(skeleton, poseData);
            computeFK(skeleton);

            // Special case: 1-joint chain — rotate the end effector directly toward target
            if (ikChain.size() == 1) {
                double[] jp = endEffector.worldPos;
                double tx = targetWorldPos[0] - jp[0];
                double ty = targetWorldPos[1] - jp[1];
                double tz = targetWorldPos[2] - jp[2];
                float damp = 0.08f;
                float ms = 0.05f;
                switch (ikViewQuadrant) {
                    case 0 -> { // FRONT: XY → zRot (-)
                        double a = Math.atan2(ty, tx);
                        endEffector.angleZ = clampAngle(endEffector.angleZ - (float)(clampDelta(a * damp, ms)));
                    }
                    case 1 -> { // SIDE: ZY → xRot (+)
                        double a = Math.atan2(ty, tz);
                        endEffector.angleX = clampAngle(endEffector.angleX + (float)(clampDelta(a * damp, ms)));
                    }
                    case 2 -> { // TOP: XZ → yRot (+)
                        double a = Math.atan2(tz, tx);
                        endEffector.angleY = clampAngle(endEffector.angleY + (float)(clampDelta(a * damp, ms)));
                    }
                    default -> {
                        double a = Math.atan2(ty, tx);
                        endEffector.angleZ = clampAngle(endEffector.angleZ - (float)(clampDelta(a * damp, ms)));
                    }
                }
                computeFK(skeleton);
                batchUpdating = true;
                String sn = ikJointName.startsWith("R_") ? "L_" + ikJointName.substring(2) : ikJointName;
                JointSliderGroup grp = sliderGroups.get(sn);
                if (grp != null) {
                    grp.xSlider.setValue(Math.round(endEffector.angleX * 100));
                    grp.ySlider.setValue(Math.round(endEffector.angleY * 100));
                    grp.zSlider.setValue(Math.round(endEffector.angleZ * 100));
                }
                batchUpdating = false;
                return;
            }

            // Skip structural roots (shoulder_mount, hip, chest) but allow non-structural roots
            String rootName = ikChain.get(0);
            boolean structuralRoot = rootName.equals("shoulder_mount") ||
                                     rootName.equals("hip") || rootName.equals("chest");
            int endIndex = structuralRoot ? 1 : 0;

            for (int iter = 0; iter < iterations; iter++) {
                // Adjust parent joints (standard CCD)
                for (int i = ikChain.size() - 2; i >= endIndex; i--) {
                    String jName = ikChain.get(i);
                    Joint joint = skeleton.jointMap.get(jName);
                    if (joint == null) continue;

                    double[] endPos = skeleton.jointMap.get(ikJointName).worldPos.clone();
                    ccdStep(joint, endPos, targetWorldPos, ikViewQuadrant);
                    computeFK(skeleton);
                }

                // Also adjust the end effector using its PARENT's position as reference
                // This lets tips bend relative to their parent joint
                if (endEffector.parent != null) {
                    double[] parentPos = endEffector.parent.worldPos;
                    double[] effPos = endEffector.worldPos.clone();
                    // Pretend the parent is the "joint" and the end effector is the "end"
                    // to get a meaningful rotation for the end effector
                    double pex = effPos[0] - parentPos[0];
                    double pey = effPos[1] - parentPos[1];
                    double pez = effPos[2] - parentPos[2];
                    double ptx = targetWorldPos[0] - parentPos[0];
                    double pty = targetWorldPos[1] - parentPos[1];
                    double ptz = targetWorldPos[2] - parentPos[2];
                    // Use the same cross product approach but apply to end effector at half strength
                    float halfDamp = 0.06f;
                    float halfMax = 0.04f;
                    switch (ikViewQuadrant) {
                        case 0 -> { double c = pex*pty - pey*ptx; double m = Math.sqrt((pex*pex+pey*pey)*(ptx*ptx+pty*pty));
                            if(m>0.001) endEffector.angleZ = clampAngle(endEffector.angleZ + (float)clampDelta(Math.asin(Math.max(-1,Math.min(1,c/m)))*halfDamp, halfMax)); }
                        case 1 -> { double c = -(pez*pty - pey*ptz); double m = Math.sqrt((pez*pez+pey*pey)*(ptz*ptz+pty*pty));
                            if(m>0.001) endEffector.angleX = clampAngle(endEffector.angleX + (float)clampDelta(Math.asin(Math.max(-1,Math.min(1,c/m)))*halfDamp, halfMax)); }
                        case 2 -> { double c = -(pex*ptz - pez*ptx); double m = Math.sqrt((pex*pex+pez*pez)*(ptx*ptx+ptz*ptz));
                            if(m>0.001) endEffector.angleY = clampAngle(endEffector.angleY + (float)clampDelta(Math.asin(Math.max(-1,Math.min(1,c/m)))*halfDamp, halfMax)); }
                        default -> { double c = pex*pty - pey*ptx; double m = Math.sqrt((pex*pex+pey*pey)*(ptx*ptx+pty*pty));
                            if(m>0.001) endEffector.angleZ = clampAngle(endEffector.angleZ + (float)clampDelta(Math.asin(Math.max(-1,Math.min(1,c/m)))*halfDamp, halfMax)); }
                    }
                }
                computeFK(skeleton);
            }

            // After solving, write chain joint angles back to sliders
            // IK always solves on L_ joints, so direct write (no R_ negation needed)
            batchUpdating = true;
            for (String jName : ikChain) {
                Joint joint = skeleton.jointMap.get(jName);
                JointSliderGroup grp = sliderGroups.get(jName);
                if (grp != null && joint != null) {
                    grp.xSlider.setValue(Math.round(joint.angleX * 100));
                    grp.ySlider.setValue(Math.round(joint.angleY * 100));
                    grp.zSlider.setValue(Math.round(joint.angleZ * 100));
                }
            }
            batchUpdating = false;
        }

        /** Push all IK chain joint values into their sliders' text fields (already done per-step, but ensure fields are synced). */
        void syncIkSliderFields() {
            for (String jName : ikChain) {
                String sliderName = jName.startsWith("R_") ? "L_" + jName.substring(2) : jName;
                JointSliderGroup grp = sliderGroups.get(sliderName);
                if (grp != null) {
                    grp.xField.setText(String.format("%.2f", grp.getX()));
                    grp.yField.setText(String.format("%.2f", grp.getY()));
                    grp.zField.setText(String.format("%.2f", grp.getZ()));
                }
            }
        }

        /** Hit test rotation handles around selected joint. Returns axis (1/2/3) or 0 for no hit. */
        int hitTestHandles(int mx, int my, int cellW, int cellH) {
            if (selectedJoint == null) return 0;
            Joint j = skeleton.jointMap.get(selectedJoint);
            if (j == null) return 0;

            int q = getQuadrant(mx, my, cellW, cellH);
            float es = SCALE * zoomForQuadrant(q);
            float px = panXForQuadrant(q);
            float py = panYForQuadrant(q);
            int col = (q == 1 || q == 3) ? 1 : 0;
            int row = (q == 2 || q == 3) ? 1 : 0;

            int jx, jy;
            if (q < 3) {
                View view = q == 0 ? View.FRONT : q == 1 ? View.SIDE : View.TOP;
                double[] p2d = project(j.worldPos, view);
                jx = toScreenX(p2d[0], cellW, col, row, es, px);
                jy = toScreenY(p2d[1], cellH, col, row, es, py);
            } else {
                double[] p3 = project3D(j.worldPos, cellW, cellH, col, row, es, px, py);
                jx = (int) p3[0];
                jy = (int) p3[1];
            }

            // Red handle at (+20, 0)
            if (Math.hypot(mx - (jx + 20), my - jy) < 11) return 1;
            // Green handle at (0, -20)
            if (Math.hypot(mx - jx, my - (jy - 20)) < 11) return 2;
            // Blue handle at (+14, -14)
            if (Math.hypot(mx - (jx + 14), my - (jy - 14)) < 11) return 3;
            return 0;
        }

        /** Hit test joints under cursor. Returns joint name or null. */
        String hitTestJoint(int mx, int my, int cellW, int cellH) {
            int q = getQuadrant(mx, my, cellW, cellH);
            float es = SCALE * zoomForQuadrant(q);
            float px = panXForQuadrant(q);
            float py = panYForQuadrant(q);
            int col = (q == 1 || q == 3) ? 1 : 0;
            int row = (q == 2 || q == 3) ? 1 : 0;

            String bestJoint = null;
            double bestArea = Double.MAX_VALUE;

            for (Joint j : skeleton.allJoints) {
                double[][] corners = getCuboidCorners(j);
                int minSx = Integer.MAX_VALUE, maxSx = Integer.MIN_VALUE;
                int minSy = Integer.MAX_VALUE, maxSy = Integer.MIN_VALUE;

                for (int i = 0; i < 8; i++) {
                    int sx, sy;
                    if (q < 3) {
                        View view = q == 0 ? View.FRONT : q == 1 ? View.SIDE : View.TOP;
                        double[] p2d = project(corners[i], view);
                        sx = toScreenX(p2d[0], cellW, col, row, es, px);
                        sy = toScreenY(p2d[1], cellH, col, row, es, py);
                    } else {
                        double[] p3 = project3D(corners[i], cellW, cellH, col, row, es, px, py);
                        sx = (int) p3[0];
                        sy = (int) p3[1];
                    }
                    if (sx < minSx) minSx = sx;
                    if (sx > maxSx) maxSx = sx;
                    if (sy < minSy) minSy = sy;
                    if (sy > maxSy) maxSy = sy;
                }

                if (mx >= minSx && mx <= maxSx && my >= minSy && my <= maxSy) {
                    double area = (double)(maxSx - minSx) * (maxSy - minSy);
                    if (area < bestArea) {
                        bestArea = area;
                        bestJoint = j.name;
                    }
                }
            }
            return bestJoint;
        }

        // For 2x2 grid: col 0-1, row 0-1 — with effective scale and pan offset
        int toScreenX(double worldCoord, int cellW, int col, int row, float scale, float panX) {
            return col * cellW + cellW / 2 + (int)(worldCoord * scale) + (int)panX;
        }

        int toScreenY(double worldCoord, int cellH, int col, int row, float scale, float panY) {
            return row * cellH + cellH / 2 + (int)((worldCoord - 19f) * scale) + (int)panY;
        }

        // 3D perspective projection with effective scale and pan offset
        double[] project3D(double[] p, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
            double px = p[0], py = p[1] - 19.0, pz = p[2];

            double cosY = Math.cos(camYaw), sinY = Math.sin(camYaw);
            double rx = px * cosY + pz * sinY;
            double rz = -px * sinY + pz * cosY;
            double ry = py;

            double cosP = Math.cos(camPitch), sinP = Math.sin(camPitch);
            double ry2 = ry * cosP - rz * sinP;
            double rz2 = ry * sinP + rz * cosP;

            double dist = 30.0;
            double perspScale = dist / (dist + rz2) * scale;

            double screenX = col * cellW + cellW / 2 + rx * perspScale + panX;
            double screenY = row * cellH + cellH / 2 + ry2 * perspScale + panY;
            return new double[]{screenX, screenY, rz2};
        }

        boolean isSelected(Joint j) {
            return selectedJoint != null && selectedJoint.equals(j.name);
        }

        void drawCuboid(Graphics2D g, Joint j, View view, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
            double[][] corners = getCuboidCorners(j);
            int[] sx = new int[8], sy = new int[8];
            for (int i = 0; i < 8; i++) {
                double[] p2d = project(corners[i], view);
                sx[i] = toScreenX(p2d[0], cellW, col, row, scale, panX);
                sy[i] = toScreenY(p2d[1], cellH, col, row, scale, panY);
            }
            Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), solidFill ? 200 : 60);
            g.setColor(fill);
            int[][] faces = {
                    {0,1,2,3},{4,5,6,7},{0,3,7,4},{1,2,6,5},{0,1,5,4},{3,2,6,7}
            };
            for (int[] face : faces) {
                int[] fx = new int[4], fy = new int[4];
                for (int i = 0; i < 4; i++) { fx[i] = sx[face[i]]; fy[i] = sy[face[i]]; }
                g.fillPolygon(fx, fy, 4);
            }
            if (isSelected(j)) {
                g.setColor(new Color(255, 180, 40));
                g.setStroke(new BasicStroke(3f));
            } else {
                Color edge = new Color(j.colour.getRed()/2, j.colour.getGreen()/2, j.colour.getBlue()/2, 200);
                g.setColor(edge);
                g.setStroke(new BasicStroke(1.2f));
            }
            int[][] edges = {
                    {0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}
            };
            for (int[] e : edges) {
                g.drawLine(sx[e[0]], sy[e[0]], sx[e[1]], sy[e[1]]);
            }
        }

        void drawJointDot(Graphics2D g, Joint j, View view, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
            double[] p2d = project(j.worldPos, view);
            int x = toScreenX(p2d[0], cellW, col, row, scale, panX);
            int y = toScreenY(p2d[1], cellH, col, row, scale, panY);
            g.setColor(j.colour);
            g.fillOval(x-3, y-3, 6, 6);
            g.setColor(j.colour.darker().darker());
            g.setStroke(new BasicStroke(1f));
            g.drawOval(x-3, y-3, 6, 6);
        }

        void drawHandles(Graphics2D g, Joint j, int jx, int jy) {
            // Red handle = xRot at (+20, 0)
            drawRotationHandle(g, jx, jy, new Color(220, 40, 40), "X", 20, 0, hoveredAxis == 1);
            // Green handle = yRot at (0, -20)
            drawRotationHandle(g, jx, jy, new Color(40, 180, 40), "Y", 0, -20, hoveredAxis == 2);
            // Blue handle = zRot at (+14, -14)
            drawRotationHandle(g, jx, jy, new Color(40, 80, 220), "Z", 14, -14, hoveredAxis == 3);
        }

        void drawRotationHandle(Graphics2D g, int cx, int cy, Color color, String axis, int offsetX, int offsetY, boolean hovered) {
            int hx = cx + offsetX;
            int hy = cy + offsetY;

            // Background circle for hit area
            if (hovered) {
                g.setColor(new Color(255, 255, 180, 220));
            } else {
                g.setColor(new Color(255, 255, 255, 180));
            }
            g.fillOval(hx - 10, hy - 10, 20, 20);

            // Colored arc arrow
            g.setColor(color);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int startAngle, arcAngle;
            switch (axis) {
                case "X" -> { startAngle = 45;   arcAngle = 180; }  // vertical arc (pitch)
                case "Y" -> { startAngle = -45;  arcAngle = 180; }  // horizontal arc (yaw)
                default  -> { startAngle = 135;  arcAngle = 180; }  // diagonal arc (roll)
            }
            g.drawArc(hx - 8, hy - 8, 16, 16, startAngle, arcAngle);

            // Arrowhead at end of arc
            double endAngleRad = Math.toRadians(startAngle + arcAngle);
            int ax = hx + (int)(8 * Math.cos(endAngleRad));
            int ay = hy - (int)(8 * Math.sin(endAngleRad));
            // Tangent direction at end (perpendicular to radius, in direction of arc)
            double tanX = Math.sin(endAngleRad);  // tangent = rotate radius 90 deg in arc direction
            double tanY = Math.cos(endAngleRad);
            int arrowSize = 4;
            int[] arrowXs = {
                ax,
                ax + (int)((-tanX - tanY * 0.5) * arrowSize),
                ax + (int)((-tanX + tanY * 0.5) * arrowSize)
            };
            int[] arrowYs = {
                ay,
                ay + (int)((-tanY + tanX * 0.5) * arrowSize),
                ay + (int)((-tanY - tanX * 0.5) * arrowSize)
            };
            g.fillPolygon(arrowXs, arrowYs, 3);

            // Axis label
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString(axis, hx + 7, hy - 7);

            // Border circle
            Stroke prev = g.getStroke();
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), hovered ? 200 : 100));
            g.drawOval(hx - 10, hy - 10, 20, 20);
            g.setStroke(prev);
        }

        void drawGroundPlane(Graphics2D g, int cellW, int cellH, int col, int row, float scale, float panY) {
            int groundY = toScreenY(24.0, cellH, col, row, scale, panY);
            g.setColor(new Color(140, 100, 60, 100));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 4f}, 0f));
            int x0 = col * cellW + 10;
            int x1 = col * cellW + cellW - 10;
            g.drawLine(x0, groundY, x1, groundY);
        }

        void drawPanel(Graphics2D g, View view, int cellW, int cellH, int col, int row, String label, float zoom, float panX, float panY) {
            float es = SCALE * zoom;
            int x0 = col * cellW, y0 = row * cellH;
            g.setColor(new Color(245, 245, 240));
            g.fillRect(x0, y0, cellW, cellH);
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(1f));
            g.drawRect(x0, y0, cellW - 1, cellH - 1);
            g.setColor(new Color(100, 100, 100));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(label, x0 + 8, y0 + 16);

            // Clip to panel bounds so nothing bleeds into adjacent panels
            Shape oldClip = g.getClip();
            g.setClip(x0, y0, cellW, cellH);

            drawGroundPlane(g, cellW, cellH, col, row, es, panY);

            for (Joint j : skeleton.allJoints) drawCuboid(g, j, view, cellW, cellH, col, row, es, panX, panY);
            for (Joint j : skeleton.allJoints) drawJointDot(g, j, view, cellW, cellH, col, row, es, panX, panY);

            // Draw handles on selected joint
            if (selectedJoint != null) {
                Joint sel = skeleton.jointMap.get(selectedJoint);
                if (sel != null) {
                    double[] p2d = project(sel.worldPos, view);
                    int jx = toScreenX(p2d[0], cellW, col, row, es, panX);
                    int jy = toScreenY(p2d[1], cellH, col, row, es, panY);
                    drawHandles(g, sel, jx, jy);
                }
            }

            // XYZ axis indicator in bottom-left corner
            drawAxisIndicator(g, view, cellW, cellH, col, row);

            g.setClip(oldClip);
        }

        void drawCuboid3D(Graphics2D g, Joint j, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
            double[][] corners = getCuboidCorners(j);
            int[] sx = new int[8], sy = new int[8];
            double[] depths = new double[8];
            for (int i = 0; i < 8; i++) {
                double[] p3 = project3D(corners[i], cellW, cellH, col, row, scale, panX, panY);
                sx[i] = (int) p3[0];
                sy[i] = (int) p3[1];
                depths[i] = p3[2];
            }
            Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), solidFill ? 200 : 60);
            g.setColor(fill);
            int[][] faces = {
                    {0,1,2,3},{4,5,6,7},{0,3,7,4},{1,2,6,5},{0,1,5,4},{3,2,6,7}
            };
            for (int[] face : faces) {
                int[] fx = new int[4], fy = new int[4];
                for (int i = 0; i < 4; i++) { fx[i] = sx[face[i]]; fy[i] = sy[face[i]]; }
                g.fillPolygon(fx, fy, 4);
            }
            if (isSelected(j)) {
                g.setColor(new Color(255, 180, 40));
                g.setStroke(new BasicStroke(3f));
            } else {
                Color edge = new Color(j.colour.getRed()/2, j.colour.getGreen()/2, j.colour.getBlue()/2, 200);
                g.setColor(edge);
                g.setStroke(new BasicStroke(1.2f));
            }
            int[][] edges = {
                    {0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}
            };
            for (int[] e : edges) {
                g.drawLine(sx[e[0]], sy[e[0]], sx[e[1]], sy[e[1]]);
            }
        }

        void drawJointDot3D(Graphics2D g, Joint j, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
            double[] p3 = project3D(j.worldPos, cellW, cellH, col, row, scale, panX, panY);
            int x = (int) p3[0];
            int y = (int) p3[1];
            g.setColor(j.colour);
            g.fillOval(x-3, y-3, 6, 6);
            g.setColor(j.colour.darker().darker());
            g.setStroke(new BasicStroke(1f));
            g.drawOval(x-3, y-3, 6, 6);
        }

        void draw3DPanel(Graphics2D g, int cellW, int cellH, int col, int row, String label, float zoom, float panX, float panY) {
            float es = SCALE * zoom;
            int x0 = col * cellW, y0 = row * cellH;
            g.setColor(new Color(235, 235, 230));
            g.fillRect(x0, y0, cellW, cellH);
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(1f));
            g.drawRect(x0, y0, cellW - 1, cellH - 1);
            g.setColor(new Color(100, 100, 100));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(label, x0 + 8, y0 + 16);

            // Clip to panel bounds so nothing bleeds into adjacent panels
            Shape oldClip = g.getClip();
            g.setClip(x0, y0, cellW, cellH);

            // Sort joints by depth (painter's algorithm: draw furthest first)
            List<Joint> sorted = new ArrayList<>(skeleton.allJoints);
            sorted.sort((a, b) -> {
                double[] pa = project3D(a.worldPos, cellW, cellH, col, row, es, panX, panY);
                double[] pb = project3D(b.worldPos, cellW, cellH, col, row, es, panX, panY);
                return Double.compare(pb[2], pa[2]);  // furthest (largest z) first
            });
            for (Joint j : sorted) drawCuboid3D(g, j, cellW, cellH, col, row, es, panX, panY);
            for (Joint j : sorted) drawJointDot3D(g, j, cellW, cellH, col, row, es, panX, panY);

            // Draw handles on selected joint
            if (selectedJoint != null) {
                Joint sel = skeleton.jointMap.get(selectedJoint);
                if (sel != null) {
                    double[] p3 = project3D(sel.worldPos, cellW, cellH, col, row, es, panX, panY);
                    int jx = (int) p3[0];
                    int jy = (int) p3[1];
                    drawHandles(g, sel, jx, jy);
                }
            }

            // 3D axis indicator that rotates with camera
            drawAxisIndicator3D(g, cellW, cellH, col, row);

            g.setClip(oldClip);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cellW = w / 2, cellH = h / 2;

            // Apply current slider values and compute FK
            Map<String, float[]> poseData = getCurrentPose();
            applyPose(skeleton, poseData);
            computeFK(skeleton);

            drawPanel(g, View.FRONT, cellW, cellH, 0, 0, "FRONT (from +Z)", zoomFront, panFrontX, panFrontY);
            drawPanel(g, View.SIDE,  cellW, cellH, 1, 0, "SIDE (from +X)",  zoomSide,  panSideX,  panSideY);
            drawPanel(g, View.TOP,   cellW, cellH, 0, 1, "TOP (from -Y)",   zoomTop,   panTopX,   panTopY);
            draw3DPanel(g, cellW, cellH, 1, 1, "3D (drag to rotate)", zoom3D, pan3DX, pan3DY);

            // Draw IK drag overlay
            if (ikDragging && ikJointName != null) {
                drawIkOverlay(g, cellW, cellH);
            }

            // Draw drag tooltip
            if (draggingAxis > 0 && draggingJoint != null) {
                String sliderName = draggingJoint.startsWith("R_") ?
                        "L_" + draggingJoint.substring(2) : draggingJoint;
                JointSliderGroup grp = sliderGroups.get(sliderName);
                if (grp != null) {
                    float val = draggingAxis == 1 ? grp.getX() :
                            draggingAxis == 2 ? grp.getY() : grp.getZ();
                    String axisName = draggingAxis == 1 ? "xRot" : draggingAxis == 2 ? "yRot" : "zRot";
                    Color axisColor = draggingAxis == 1 ? new Color(220, 40, 40) :
                            draggingAxis == 2 ? new Color(40, 180, 40) : new Color(40, 80, 220);
                    String tip = String.format("%s: %.2f", axisName, val);
                    g.setFont(new Font("Monospaced", Font.BOLD, 12));
                    FontMetrics tfm = g.getFontMetrics();
                    int tw = tfm.stringWidth(tip) + 8;
                    int th = tfm.getHeight() + 4;
                    int tx = dragCurX + 16, ty = dragCurY - 16;
                    g.setColor(new Color(40, 40, 40, 200));
                    g.fillRoundRect(tx, ty, tw, th, 6, 6);
                    g.setColor(axisColor);
                    g.drawString(tip, tx + 4, ty + th - 5);
                }
            }

            // Overlay: archetype + pose name
            g.setColor(new Color(40, 40, 40));
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String title = currentArchetype + " — " + currentPoseName.toUpperCase().replace('_', ' ');
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, (w - fm.stringWidth(title)) / 2, h - 8);

            // Legend
            drawLegend(g, w, h);
        }

        void drawIkOverlay(Graphics2D g, int cellW, int cellH) {
            int q = ikViewQuadrant;
            float es = SCALE * zoomForQuadrant(q);
            float px = panXForQuadrant(q);
            float py = panYForQuadrant(q);
            int col = (q == 1 || q == 3) ? 1 : 0;
            int row = (q == 2 || q == 3) ? 1 : 0;

            // Clip to the quadrant panel so drawing doesn't bleed
            Shape oldClip = g.getClip();
            g.setClip(col * cellW, row * cellH, cellW, cellH);

            // Highlight all joints in the IK chain with a glow ring
            for (String jName : ikChain) {
                Joint j = skeleton.jointMap.get(jName);
                if (j == null) continue;
                int jx, jy;
                if (q < 3) {
                    View view = q == 0 ? View.FRONT : q == 1 ? View.SIDE : View.TOP;
                    double[] p2d = project(j.worldPos, view);
                    jx = toScreenX(p2d[0], cellW, col, row, es, px);
                    jy = toScreenY(p2d[1], cellH, col, row, es, py);
                } else {
                    double[] p3 = project3D(j.worldPos, cellW, cellH, col, row, es, px, py);
                    jx = (int) p3[0];
                    jy = (int) p3[1];
                }
                // Glow ring around chain joint
                g.setColor(new Color(255, 220, 60, 120));
                g.setStroke(new BasicStroke(3f));
                g.drawOval(jx - 8, jy - 8, 16, 16);
            }

            // Draw dashed line from end effector to mouse cursor target
            Joint endJoint = skeleton.jointMap.get(ikJointName);
            if (endJoint != null) {
                int ex, ey;
                if (q < 3) {
                    View view = q == 0 ? View.FRONT : q == 1 ? View.SIDE : View.TOP;
                    double[] p2d = project(endJoint.worldPos, view);
                    ex = toScreenX(p2d[0], cellW, col, row, es, px);
                    ey = toScreenY(p2d[1], cellH, col, row, es, py);
                } else {
                    double[] p3 = project3D(endJoint.worldPos, cellW, cellH, col, row, es, px, py);
                    ex = (int) p3[0];
                    ey = (int) p3[1];
                }

                g.setColor(new Color(255, 200, 40, 200));
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10f, new float[]{5f, 4f}, 0f));
                g.drawLine(ex, ey, ikTargetScreenX, ikTargetScreenY);

                // Small crosshair at target
                g.setStroke(new BasicStroke(2f));
                g.setColor(new Color(255, 200, 40, 220));
                int cs = 6;
                g.drawLine(ikTargetScreenX - cs, ikTargetScreenY, ikTargetScreenX + cs, ikTargetScreenY);
                g.drawLine(ikTargetScreenX, ikTargetScreenY - cs, ikTargetScreenX, ikTargetScreenY + cs);
                g.drawOval(ikTargetScreenX - 4, ikTargetScreenY - 4, 8, 8);
            }

            g.setClip(oldClip);
        }

        void drawLegend(Graphics2D g, int w, int h) {
            int x = w - 120;
            int y = h - 90;
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            Object[][] legend = {
                    {SPINE_BLUE, "Spine"}, {NECK_PINK, "Neck"}, {HEAD_PINK, "Head"},
                    {WING_GREEN, "Wings"}, {LEG_ORANGE, "Legs"}, {TAIL_YELLOW, "Tail"},
            };
            for (Object[] item : legend) {
                Color c = (Color) item[0];
                String label = (String) item[1];
                g.setColor(c);
                g.fillOval(x, y-8, 10, 10);
                g.setColor(new Color(60, 60, 60));
                g.drawString(label, x+14, y);
                y += 14;
            }
        }

        /** Draw XYZ axis indicator in a panel corner. */
        void drawAxisIndicator(Graphics2D g, View view, int cellW, int cellH, int col, int row) {
            int cx = col * cellW + 40;  // bottom-left of panel
            int cy = row * cellH + cellH - 30;
            int len = 25;

            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setFont(new Font("SansSerif", Font.BOLD, 10));

            // Determine which screen directions correspond to X, Y, Z in this view
            // In MC model space: +X=right, +Y=down, +Z=back
            switch (view) {
                case FRONT -> {  // looking from +Z: screen X=world X, screen Y=world Y
                    drawAxisArrow(g, cx, cy, cx + len, cy, Color.RED, "X");       // right
                    drawAxisArrow(g, cx, cy, cx, cy - len, Color.GREEN, "Y(-up)"); // up (Y is inverted)
                }
                case SIDE -> {   // looking from +X: screen X=world Z, screen Y=world Y
                    drawAxisArrow(g, cx, cy, cx + len, cy, Color.BLUE, "Z");
                    drawAxisArrow(g, cx, cy, cx, cy - len, Color.GREEN, "Y(-up)");
                }
                case TOP -> {    // looking from -Y: screen X=world X, screen Y=world Z
                    drawAxisArrow(g, cx, cy, cx + len, cy, Color.RED, "X");
                    drawAxisArrow(g, cx, cy, cx, cy - len, Color.BLUE, "Z(-fwd)");
                }
            }
        }

        /** Draw XYZ axis indicator for 3D view using camera rotation. */
        void drawAxisIndicator3D(Graphics2D g, int cellW, int cellH, int col, int row) {
            int cx = col * cellW + 40;
            int cy = row * cellH + cellH - 30;
            int len = 25;

            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setFont(new Font("SansSerif", Font.BOLD, 10));

            double cosY = Math.cos(camYaw), sinY = Math.sin(camYaw);
            double cosP = Math.cos(camPitch), sinP = Math.sin(camPitch);

            // X axis (1,0,0) -> rotated by yaw then pitch
            double xx = cosY, xz = -sinY;
            double xy2 = -xz * sinP;
            drawAxisArrow(g, cx, cy, cx + (int)(xx * len), cy + (int)(xy2 * len), Color.RED, "X");

            // Y axis (0,1,0) -> rotated by pitch only
            double yy = cosP, yz = sinP;
            drawAxisArrow(g, cx, cy, cx, cy + (int)(yy * len), Color.GREEN, "Y");

            // Z axis (0,0,1) -> rotated by yaw then pitch
            double zx = sinY, zz = cosY;
            double zy2 = -zz * sinP;
            drawAxisArrow(g, cx, cy, cx + (int)(zx * len), cy + (int)(zy2 * len), Color.BLUE, "Z");
        }

        void drawAxisArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color, String label) {
            int dx = x2 - x1, dy = y2 - y1;
            if (dx == 0 && dy == 0) return;  // degenerate
            g.setColor(color);
            g.drawLine(x1, y1, x2, y2);
            // Arrowhead
            double angle = Math.atan2(dy, dx);
            int ax1 = x2 - (int)(7 * Math.cos(angle - 0.4));
            int ay1 = y2 - (int)(7 * Math.sin(angle - 0.4));
            int ax2 = x2 - (int)(7 * Math.cos(angle + 0.4));
            int ay2 = y2 - (int)(7 * Math.sin(angle + 0.4));
            g.fillPolygon(new int[]{x2, ax1, ax2}, new int[]{y2, ay1, ay2}, 3);
            // Label
            g.drawString(label, x2 + 3, y2 - 3);
        }
    }

    // =========================================================================
    // Slider system
    // =========================================================================

    /** One joint's 3 rotation sliders + 6 geometry sliders + text fields. */
    class JointSliderGroup {
        final String jointName;
        final JSlider xSlider, ySlider, zSlider;
        final JTextField xField, yField, zField;

        // Geometry sliders: offset X/Y/Z and size W/H/D
        final JSlider offXSlider, offYSlider, offZSlider;
        final JTextField offXField, offYField, offZField;
        final JSlider sizeWSlider, sizeHSlider, sizeDSlider;
        final JTextField sizeWField, sizeHField, sizeDField;
        JPanel geometryPanel;

        JointSliderGroup(String jointName) {
            this.jointName = jointName;
            xSlider = makeRotSlider();
            ySlider = makeRotSlider();
            zSlider = makeRotSlider();
            xField = makeField("0.00");
            yField = makeField("0.00");
            zField = makeField("0.00");

            linkRotSliderAndField(xSlider, xField);
            linkRotSliderAndField(ySlider, yField);
            linkRotSliderAndField(zSlider, zField);

            // Offset sliders: -100 to 100 => -10.0 to 10.0 (step 0.1)
            offXSlider = makeGeomSlider(-100, 100, 0);
            offYSlider = makeGeomSlider(-100, 100, 0);
            offZSlider = makeGeomSlider(-100, 100, 0);
            offXField = makeField("0.0");
            offYField = makeField("0.0");
            offZField = makeField("0.0");

            // Size sliders: 2 to 40 => 0.5 to 10.0 (step 0.25, slider in units of 0.25)
            sizeWSlider = makeGeomSlider(2, 40, 4);
            sizeHSlider = makeGeomSlider(2, 40, 4);
            sizeDSlider = makeGeomSlider(2, 40, 4);
            sizeWField = makeField("1.0");
            sizeHField = makeField("1.0");
            sizeDField = makeField("1.0");

            linkOffsetSliderAndField(offXSlider, offXField);
            linkOffsetSliderAndField(offYSlider, offYField);
            linkOffsetSliderAndField(offZSlider, offZField);
            linkSizeSliderAndField(sizeWSlider, sizeWField);
            linkSizeSliderAndField(sizeHSlider, sizeHField);
            linkSizeSliderAndField(sizeDSlider, sizeDField);
        }

        JSlider makeRotSlider() {
            // Range: -314 to 314 (representing -pi to pi, scaled by 100)
            JSlider s = new JSlider(-314, 314, 0);
            s.setPreferredSize(new Dimension(120, 20));
            return s;
        }

        JSlider makeGeomSlider(int min, int max, int value) {
            JSlider s = new JSlider(min, max, value);
            s.setPreferredSize(new Dimension(120, 18));
            return s;
        }

        JTextField makeField(String initial) {
            JTextField f = new JTextField(initial, 5);
            f.setFont(new Font("Monospaced", Font.PLAIN, 11));
            f.setHorizontalAlignment(JTextField.RIGHT);
            return f;
        }

        void linkRotSliderAndField(JSlider slider, JTextField field) {
            // Capture state BEFORE slider drag starts
            slider.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!batchUpdating) {
                        captureState();
                        // Auto-pause and snap to endpoint when editing during cyclic mode
                        if (editingCyclic && animPlaying) {
                            animPlaying = false;
                            animTimer.stop();
                            if (playPauseBtn != null) playPauseBtn.setText("Play");
                            // Snap to nearest endpoint
                            float snapPhase = animPhase < 0.5f ? 0f : 1f;
                            cyclicEndpoint = snapPhase == 0f ? "A" : "B";
                            animPhase = snapPhase;
                            batchUpdating = true;
                            phaseSlider.setValue(Math.round(snapPhase * 100));
                            batchUpdating = false;
                            applyPhase(snapPhase);
                        }
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!batchUpdating && editingCyclic) updateCurrentEndpointFromSliders();
                }
            });
            slider.addChangeListener(e -> {
                if (!batchUpdating) {
                    float val = slider.getValue() / 100f;
                    field.setText(String.format("%.2f", val));
                    previewPanel.repaint();
                    updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(-(float)Math.PI, Math.min((float)Math.PI, val));
                    batchUpdating = true;
                    slider.setValue(Math.round(val * 100));
                    batchUpdating = false;
                    field.setText(String.format("%.2f", val));
                    previewPanel.repaint();
                    updateExportText();
                    captureState();
                    if (editingCyclic) updateCurrentEndpointFromSliders();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        void linkOffsetSliderAndField(JSlider slider, JTextField field) {
            slider.addChangeListener(e -> {
                if (!batchUpdating) {
                    float val = slider.getValue() / 10f;
                    field.setText(String.format("%.1f", val));
                    applyGeometryToJoint();
                    previewPanel.repaint();
                    updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(-10f, Math.min(10f, val));
                    batchUpdating = true;
                    slider.setValue(Math.round(val * 10));
                    batchUpdating = false;
                    field.setText(String.format("%.1f", val));
                    applyGeometryToJoint();
                    previewPanel.repaint();
                    updateExportText();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        void linkSizeSliderAndField(JSlider slider, JTextField field) {
            slider.addChangeListener(e -> {
                if (!batchUpdating) {
                    float val = slider.getValue() * 0.25f;
                    field.setText(String.format("%.2f", val));
                    applyGeometryToJoint();
                    previewPanel.repaint();
                    updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(0.5f, Math.min(10f, val));
                    batchUpdating = true;
                    slider.setValue(Math.round(val / 0.25f));
                    batchUpdating = false;
                    field.setText(String.format("%.2f", val));
                    applyGeometryToJoint();
                    previewPanel.repaint();
                    updateExportText();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        /** Push geometry slider values into the Joint object. */
        void applyGeometryToJoint() {
            Joint j = skeleton.jointMap.get(jointName);
            if (j == null) return;
            j.offsetX = offXSlider.getValue() / 10f;
            j.offsetY = offYSlider.getValue() / 10f;
            j.offsetZ = offZSlider.getValue() / 10f;
            j.boxW = sizeWSlider.getValue() * 0.25f;
            j.boxH = sizeHSlider.getValue() * 0.25f;
            j.boxD = sizeDSlider.getValue() * 0.25f;
            j.recomputeBoxOrigin();
        }

        float getX() { return xSlider.getValue() / 100f; }
        float getY() { return ySlider.getValue() / 100f; }
        float getZ() { return zSlider.getValue() / 100f; }

        void setValues(float x, float y, float z) {
            xSlider.setValue(Math.round(x * 100));
            ySlider.setValue(Math.round(y * 100));
            zSlider.setValue(Math.round(z * 100));
            xField.setText(String.format("%.2f", x));
            yField.setText(String.format("%.2f", y));
            zField.setText(String.format("%.2f", z));
        }

        /** Set geometry sliders from a Joint's current values. */
        void setGeometryFromJoint(Joint j) {
            offXSlider.setValue(Math.round(j.offsetX * 10));
            offYSlider.setValue(Math.round(j.offsetY * 10));
            offZSlider.setValue(Math.round(j.offsetZ * 10));
            offXField.setText(String.format("%.1f", j.offsetX));
            offYField.setText(String.format("%.1f", j.offsetY));
            offZField.setText(String.format("%.1f", j.offsetZ));
            sizeWSlider.setValue(Math.round(j.boxW / 0.25f));
            sizeHSlider.setValue(Math.round(j.boxH / 0.25f));
            sizeDSlider.setValue(Math.round(j.boxD / 0.25f));
            sizeWField.setText(String.format("%.2f", j.boxW));
            sizeHField.setText(String.format("%.2f", j.boxH));
            sizeDField.setText(String.format("%.2f", j.boxD));
        }

        JPanel buildPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(1, 2, 1, 2);
            c.fill = GridBagConstraints.HORIZONTAL;

            // Joint name label
            c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
            JLabel nameLabel = new JLabel(jointName);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            p.add(nameLabel, c);
            c.gridwidth = 1;

            addRow(p, c, 1, "xRot", xSlider, xField, false);
            addRow(p, c, 2, "yRot", ySlider, yField, false);
            addRow(p, c, 3, "zRot", zSlider, zField, false);

            // Geometry sub-panel (hidden by default)
            geometryPanel = new JPanel(new GridBagLayout());
            geometryPanel.setBackground(new Color(235, 240, 248));
            geometryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 190, 210)),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(1, 2, 1, 2);
            gc.fill = GridBagConstraints.HORIZONTAL;

            addRow(geometryPanel, gc, 0, "Offset X", offXSlider, offXField, true);
            addRow(geometryPanel, gc, 1, "Offset Y", offYSlider, offYField, true);
            addRow(geometryPanel, gc, 2, "Offset Z", offZSlider, offZField, true);
            addRow(geometryPanel, gc, 3, "Size W", sizeWSlider, sizeWField, true);
            addRow(geometryPanel, gc, 4, "Size H", sizeHSlider, sizeHField, true);
            addRow(geometryPanel, gc, 5, "Size D", sizeDSlider, sizeDField, true);

            geometryPanel.setVisible(showGeometry);

            c.gridx = 0; c.gridy = 4; c.gridwidth = 3;
            p.add(geometryPanel, c);

            return p;
        }

        void addRow(JPanel p, GridBagConstraints c, int row, String label,
                    JSlider slider, JTextField field, boolean isGeometry) {
            c.gridy = row;
            c.gridx = 0; c.weightx = 0;
            JLabel lbl = new JLabel(label);
            if (isGeometry) {
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 9));
                lbl.setForeground(new Color(80, 90, 120));
            } else {
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                lbl.setForeground(new Color(100, 100, 100));
            }
            p.add(lbl, c);
            c.gridx = 1; c.weightx = 1.0;
            p.add(slider, c);
            c.gridx = 2; c.weightx = 0;
            p.add(field, c);
        }
    }

    // =========================================================================
    // Undo/Redo
    // =========================================================================

    List<Map<String, float[]>> undoStack = new ArrayList<>();
    int undoIndex = -1;
    static final int MAX_UNDO = 100;

    /** Snapshot current joint angles and push onto the undo stack. */
    void captureState() {
        Map<String, float[]> snapshot = new LinkedHashMap<>();
        for (var entry : sliderGroups.entrySet()) {
            JointSliderGroup g = entry.getValue();
            snapshot.put(entry.getKey(), new float[]{g.getX(), g.getY(), g.getZ()});
        }
        // Discard any redo entries beyond current index
        while (undoStack.size() > undoIndex + 1) {
            undoStack.remove(undoStack.size() - 1);
        }
        undoStack.add(snapshot);
        if (undoStack.size() > MAX_UNDO) {
            undoStack.remove(0);
        } else {
            undoIndex++;
        }
        updateUndoButtons();
    }

    void applySnapshot(Map<String, float[]> snapshot) {
        batchUpdating = true;
        for (var entry : snapshot.entrySet()) {
            JointSliderGroup g = sliderGroups.get(entry.getKey());
            if (g != null) {
                float[] v = entry.getValue();
                g.setValues(v[0], v[1], v[2]);
            }
        }
        batchUpdating = false;
        previewPanel.repaint();
        updateExportText();
    }

    void undo() {
        if (undoIndex > 0) {
            undoIndex--;
            applySnapshot(undoStack.get(undoIndex));
            updateUndoButtons();
        }
    }

    void redo() {
        if (undoIndex < undoStack.size() - 1) {
            undoIndex++;
            applySnapshot(undoStack.get(undoIndex));
            updateUndoButtons();
        }
    }

    JButton undoBtn;
    JButton redoBtn;

    void updateUndoButtons() {
        if (undoBtn != null) undoBtn.setEnabled(undoIndex > 0);
        if (redoBtn != null) redoBtn.setEnabled(undoIndex < undoStack.size() - 1);
    }

    // =========================================================================
    // State
    // =========================================================================

    String currentArchetype = "Passerine";
    String currentPoseName = "perched";
    Skeleton skeleton;
    Map<String, List<Preset>> allPresets;
    Map<String, JointSliderGroup> sliderGroups = new LinkedHashMap<>();
    boolean batchUpdating = false;
    boolean showGeometry = false;

    // --- Cyclic animation editing state ---
    boolean editingCyclic = false;
    Map<String, float[]> cyclicBasePose = null;
    Map<String, float[]> cyclicOffsetA = null;
    Map<String, float[]> cyclicOffsetB = null;
    String cyclicAnimName = null;    // e.g. "wingbeat"
    String cyclicEndpoint = null;    // "A" or "B" — which endpoint the sliders currently show
    float animPhase = 0f;            // 0.0 = offset A, 1.0 = offset B
    float animDirection = 1f;         // kept for manual scrub compatibility
    float animElapsedTicks = 0f;      // simulated MC ticks elapsed

    // --- Animation playback ---
    boolean animPlaying = false;
    float animSpeed = 1.0f;          // multiplier: 1.0 = 1 full cycle per second
    javax.swing.Timer animTimer;
    JSlider phaseSlider;
    JSlider speedSlider;
    JPanel animControlsPanel;
    JButton playPauseBtn;
    JLabel cyclicStatusLabel;

    PreviewPanel previewPanel;
    JTextArea exportTextArea;
    JComboBox<String> archetypeCombo;
    JComboBox<String> poseCombo;
    JPanel sliderPanel;
    JScrollPane sliderScrollPane;
    JCheckBox geometryToggle;
    boolean solidFill = false;
    Map<String, JPanel> jointToSection = new HashMap<>();  // joint name -> its section panel
    List<JPanel> allSections = new ArrayList<>();

    // =========================================================================
    // Build current pose from sliders
    // =========================================================================

    Map<String, float[]> getCurrentPose() {
        Map<String, float[]> pose = new LinkedHashMap<>();
        for (var entry : sliderGroups.entrySet()) {
            String name = entry.getKey();
            JointSliderGroup g = entry.getValue();
            float x = g.getX(), y = g.getY(), z = g.getZ();
            if (x != 0 || y != 0 || z != 0) {
                pose.put(name, new float[]{x, y, z});
            }
            // Auto-mirror for left joints
            if (name.startsWith("L_")) {
                String rName = "R_" + name.substring(2);
                if (skeleton.jointMap.containsKey(rName)) {
                    if (x != 0 || y != 0 || z != 0) {
                        pose.put(rName, new float[]{x, -y, -z});
                    }
                }
            }
        }
        return pose;
    }

    // =========================================================================
    // Export
    // =========================================================================

    String generateExportCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Exported from PoseEditor — archetype: ").append(currentArchetype)
                .append(", pose: ").append(currentPoseName).append("\n");
        sb.append("// Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        float flapsPerSec = animSpeed * 20f / (2f * (float) Math.PI);
        sb.append("// Flap Frequency: ").append(String.format("%.2f", animSpeed))
          .append(" (").append(String.format("%.1f", flapsPerSec)).append(" flaps/sec)\n");
        sb.append("// Use in renderer: flapFrequency() { return ").append(String.format("%.2f", animSpeed)).append("f; }\n\n");

        if (editingCyclic && cyclicBasePose != null && cyclicOffsetA != null && cyclicOffsetB != null) {
            // --- Cyclic export: compute offsets from current slider values ---
            // Current slider values = base + current offset (at the phase the user last stopped at)
            // We export both endpoint offsets (A and B) as a CyclicAnimation block.
            // To compute offsets: offset = currentSliderValue - basePoseValue
            // We use the stored offsetA and offsetB directly (the user may have modified them
            // if they edited sliders, in which case we read back slider values for the active endpoint).
            sb.append("public static final CyclicAnimation ").append(cyclicAnimName.toUpperCase())
                    .append(" = new CyclicAnimation(\n");
            sb.append("    \"").append(cyclicAnimName).append("\",\n");

            // Endpoint A
            sb.append("    PoseData.builder(\"").append(getOffsetAName()).append("\")\n");
            boolean mirrorA = exportOffsetBlock(sb, cyclicOffsetA);
            if (mirrorA) sb.append("            .mirror()\n");
            sb.append("            .build(),\n");

            // Endpoint B
            sb.append("    PoseData.builder(\"").append(getOffsetBName()).append("\")\n");
            boolean mirrorB = exportOffsetBlock(sb, cyclicOffsetB);
            if (mirrorB) sb.append("            .mirror()\n");
            sb.append("            .build()\n");
            sb.append(");\n");

            sb.append("\n// Current phase: ").append(String.format("%.2f", animPhase))
              .append("  (0=").append(getOffsetAName()).append(", 1=").append(getOffsetBName()).append(")\n");
        } else {
            // --- Static pose export ---
            sb.append("public static final PoseData ").append(currentPoseName.toUpperCase())
                    .append(" = PoseData.builder(\"").append(currentPoseName).append("\")\n");

            boolean hasMirror = false;
            for (var entry : sliderGroups.entrySet()) {
                String name = entry.getKey();
                JointSliderGroup g = entry.getValue();
                float x = g.getX(), y = g.getY(), z = g.getZ();
                if (x != 0 || y != 0 || z != 0) {
                    String skelName = toSkeletonConstant(name);
                    sb.append("        .joint(BirdSkeleton.").append(skelName).append(", ")
                            .append(formatFloat(x)).append(", ")
                            .append(formatFloat(y)).append(", ")
                            .append(formatFloat(z)).append(")\n");
                    if (name.startsWith("L_")) hasMirror = true;
                }
            }
            if (hasMirror) sb.append("        .mirror()\n");
            sb.append("        .build();\n");
        }

        // Geometry section
        sb.append("\n// Geometry (archetype: ").append(currentArchetype).append(")\n");
        for (Joint j : skeleton.allJoints) {
            if (j.name.startsWith("R_")) continue;
            sb.append("// ").append(j.name)
                    .append(": offset(").append(formatGeomFloat(j.offsetX))
                    .append(", ").append(formatGeomFloat(j.offsetY))
                    .append(", ").append(formatGeomFloat(j.offsetZ))
                    .append(") size(").append(formatGeomFloat(j.boxW))
                    .append(", ").append(formatGeomFloat(j.boxH))
                    .append(", ").append(formatGeomFloat(j.boxD))
                    .append(")\n");
        }

        return sb.toString();
    }

    /** Write .joint() lines for an offset map. Returns true if any L_ joints were written (need .mirror()). */
    boolean exportOffsetBlock(StringBuilder sb, Map<String, float[]> offset) {
        boolean hasMirror = false;
        for (var entry : offset.entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("R_")) continue;  // skip; .mirror() handles them
            float[] v = entry.getValue();
            if (v[0] != 0 || v[1] != 0 || v[2] != 0) {
                String skelName = toSkeletonConstant(name);
                sb.append("            .joint(BirdSkeleton.").append(skelName).append(", ")
                        .append(formatFloat(v[0])).append(", ")
                        .append(formatFloat(v[1])).append(", ")
                        .append(formatFloat(v[2])).append(")\n");
                if (name.startsWith("L_")) hasMirror = true;
            }
        }
        return hasMirror;
    }

    String getOffsetAName() {
        // Reconstruct endpoint A name from current preset name
        // The preset name format is "cyclicName: endpointName"
        // offsetA's endpoint is the one stored in the A preset
        if (cyclicAnimName == null) return "offset_a";
        List<Preset> presets = allPresets.get(currentArchetype);
        if (presets == null) return "offset_a";
        for (Preset p : presets) {
            if (p.isCyclic() && p.cyclicName.equals(cyclicAnimName)) {
                if (mapsApproxEqual(p.joints, mergePoseOffset(p.basePose, p.offsetA))) {
                    return p.endpointName;
                }
            }
        }
        return "offset_a";
    }

    String getOffsetBName() {
        if (cyclicAnimName == null) return "offset_b";
        List<Preset> presets = allPresets.get(currentArchetype);
        if (presets == null) return "offset_b";
        for (Preset p : presets) {
            if (p.isCyclic() && p.cyclicName.equals(cyclicAnimName)) {
                if (mapsApproxEqual(p.joints, mergePoseOffset(p.basePose, p.offsetB))) {
                    return p.endpointName;
                }
            }
        }
        return "offset_b";
    }

    String formatGeomFloat(float v) {
        if (v == (int) v) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }

    String toSkeletonConstant(String jointName) {
        return jointName.toUpperCase();
    }

    String formatFloat(float v) {
        if (v == 0f) return "0f";
        if (v == (int) v) return String.format("%.1ff", v);
        // Trim trailing zeros but keep at least one decimal
        String s = String.format("%.2f", v);
        if (s.endsWith("0") && !s.endsWith(".0")) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "f";
    }

    void updateExportText() {
        if (exportTextArea != null) {
            exportTextArea.setText(generateExportCode());
        }
    }

    // =========================================================================
    // UI construction
    // =========================================================================

    PoseEditor() {
        super("British Birds — Pose Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        allPresets = buildPresets();
        skeleton = buildPasserine();

        // --- Left panel: controls ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        leftPanel.setPreferredSize(new Dimension(200, 0));

        JLabel archLabel = new JLabel("Archetype:");
        archLabel.setAlignmentX(0f);
        leftPanel.add(archLabel);
        archetypeCombo = new JComboBox<>(new String[]{"Passerine", "Raptor", "Waterfowl"});
        archetypeCombo.setMaximumSize(new Dimension(200, 28));
        archetypeCombo.setAlignmentX(0f);
        leftPanel.add(archetypeCombo);
        leftPanel.add(Box.createVerticalStrut(8));

        JLabel poseLabel = new JLabel("Pose Preset:");
        poseLabel.setAlignmentX(0f);
        leftPanel.add(poseLabel);
        poseCombo = new JComboBox<>();
        poseCombo.setMaximumSize(new Dimension(200, 28));
        poseCombo.setAlignmentX(0f);
        updatePoseCombo();
        leftPanel.add(poseCombo);
        leftPanel.add(Box.createVerticalStrut(8));

        JButton loadBtn = new JButton("Load Preset");
        loadBtn.setAlignmentX(0f);
        leftPanel.add(loadBtn);
        leftPanel.add(Box.createVerticalStrut(16));

        JButton resetBtn = new JButton("Reset All to Zero");
        resetBtn.setAlignmentX(0f);
        leftPanel.add(resetBtn);
        leftPanel.add(Box.createVerticalStrut(16));

        JPanel undoRedoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        undoRedoPanel.setAlignmentX(0f);
        undoRedoPanel.setMaximumSize(new Dimension(200, 32));
        undoBtn = new JButton("Undo");
        redoBtn = new JButton("Redo");
        undoBtn.setEnabled(false);
        redoBtn.setEnabled(false);
        undoBtn.addActionListener(e -> undo());
        redoBtn.addActionListener(e -> redo());
        undoRedoPanel.add(undoBtn);
        undoRedoPanel.add(redoBtn);
        leftPanel.add(undoRedoPanel);
        leftPanel.add(Box.createVerticalStrut(16));

        geometryToggle = new JCheckBox("Show Geometry Controls");
        geometryToggle.setAlignmentX(0f);
        geometryToggle.setSelected(false);
        leftPanel.add(geometryToggle);

        JCheckBox solidToggle = new JCheckBox("Solid Fill");
        solidToggle.setAlignmentX(0f);
        solidToggle.setSelected(false);
        solidToggle.addActionListener(e -> {
            solidFill = solidToggle.isSelected();
            previewPanel.repaint();
        });
        leftPanel.add(solidToggle);

        leftPanel.add(Box.createVerticalStrut(16));

        // --- Cyclic status label ---
        cyclicStatusLabel = new JLabel("Static pose");
        cyclicStatusLabel.setAlignmentX(0f);
        cyclicStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        cyclicStatusLabel.setForeground(new Color(100, 100, 100));
        leftPanel.add(cyclicStatusLabel);
        leftPanel.add(Box.createVerticalStrut(6));

        // --- Animation controls (only visible when cyclic preset loaded) ---
        animControlsPanel = new JPanel();
        animControlsPanel.setLayout(new BoxLayout(animControlsPanel, BoxLayout.Y_AXIS));
        animControlsPanel.setAlignmentX(0f);
        animControlsPanel.setVisible(false);  // hidden by default

        JLabel phaseLabel = new JLabel("Animation Phase:");
        phaseLabel.setAlignmentX(0f);
        phaseLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        animControlsPanel.add(phaseLabel);
        phaseSlider = new JSlider(0, 100, 0);
        phaseSlider.setMaximumSize(new Dimension(200, 22));
        phaseSlider.setAlignmentX(0f);
        phaseSlider.addChangeListener(e -> {
            if (batchUpdating) return;
            animPhase = phaseSlider.getValue() / 100f;
            if (editingCyclic) {
                applyPhase(animPhase);
            }
        });
        animControlsPanel.add(phaseSlider);
        animControlsPanel.add(Box.createVerticalStrut(4));

        JPanel playRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        playRow.setAlignmentX(0f);
        playRow.setMaximumSize(new Dimension(200, 32));
        playPauseBtn = new JButton("Play");
        playPauseBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        playPauseBtn.addActionListener(e -> togglePlayPause());
        playRow.add(playPauseBtn);
        animControlsPanel.add(playRow);
        animControlsPanel.add(Box.createVerticalStrut(4));

        JLabel speedLabel = new JLabel("Flap Freq: 1.0 (3.2 flaps/sec)");
        speedLabel.setAlignmentX(0f);
        speedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        animControlsPanel.add(speedLabel);
        speedSlider = new JSlider(1, 30, 10);
        speedSlider.setMaximumSize(new Dimension(200, 22));
        speedSlider.setAlignmentX(0f);
        speedSlider.addChangeListener(e -> {
            animSpeed = speedSlider.getValue() / 10f;
            float flapsPerSec = animSpeed * 20f / (2f * (float) Math.PI);
            speedLabel.setText(String.format("Flap Freq: %.1f (%.1f flaps/sec)", animSpeed, flapsPerSec));
        });
        animControlsPanel.add(speedSlider);
        animControlsPanel.add(Box.createVerticalStrut(4));

        leftPanel.add(animControlsPanel);

        // Set up the animation timer using MC-equivalent math
        // In MC: flapAngle = sin(ageInTicks * flapFrequency) * flapAmplitude
        // phase = flapAngle * 0.5 + 0.5  (maps -1..+1 to 0..1)
        // MC runs at 20 ticks/sec, editor at 30 fps, so ~0.667 MC ticks per frame
        animTimer = new javax.swing.Timer(33, e -> {
            if (!animPlaying || !editingCyclic) return;
            float mcTicksPerFrame = 20f / 30f;  // ~0.667
            animElapsedTicks += mcTicksPerFrame;
            // Use same sin() formula as MC
            float flapAngle = (float) Math.sin(animElapsedTicks * animSpeed);
            animPhase = flapAngle * 0.5f + 0.5f;  // 0..1 ping-pong naturally
            batchUpdating = true;
            phaseSlider.setValue(Math.round(animPhase * 100));
            batchUpdating = false;
            applyPhase(animPhase);
        });

        leftPanel.add(Box.createVerticalGlue());

        // --- Center panel: preview ---
        previewPanel = new PreviewPanel();
        previewPanel.setMinimumSize(new Dimension(600, 300));

        // --- Right panel: sliders ---
        sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        buildSliderPanel();
        sliderScrollPane = new JScrollPane(sliderPanel);
        sliderScrollPane.setPreferredSize(new Dimension(320, 0));
        sliderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sliderScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- Bottom panel: export ---
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 4));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        JPanel exportButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton exportBtn = new JButton("Export Java Code");
        JButton exportAllBtn = new JButton("Export All Presets");
        exportButtons.add(exportBtn);
        exportButtons.add(exportAllBtn);
        bottomPanel.add(exportButtons, BorderLayout.NORTH);

        exportTextArea = new JTextArea(6, 60);
        exportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        exportTextArea.setEditable(false);
        JScrollPane exportScroll = new JScrollPane(exportTextArea);
        bottomPanel.add(exportScroll, BorderLayout.CENTER);

        // --- Layout ---
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, sliderScrollPane);
        centerSplit.setResizeWeight(0.7);  // preview gets 70% of space
        centerSplit.setDividerLocation(700);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerSplit, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // --- Keyboard shortcuts (global, works even when sliders/fields have focus) ---
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (e.isShiftDown()) {
                        redo();
                    } else {
                        undo();
                    }
                    return true;  // consumed
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    redo();
                    return true;
                }
            }
            return false;
        });

        // --- Event handlers ---
        archetypeCombo.addActionListener(e -> {
            // Stop animation when switching archetypes
            if (animTimer != null && animPlaying) {
                animPlaying = false;
                animTimer.stop();
                if (playPauseBtn != null) playPauseBtn.setText("Play");
            }
            editingCyclic = false;
            animControlsPanel.setVisible(false);
            cyclicBasePose = null; cyclicOffsetA = null; cyclicOffsetB = null;
            cyclicAnimName = null; cyclicEndpoint = null;
            updateCyclicStatusLabel();

            currentArchetype = (String) archetypeCombo.getSelectedItem();
            switch (currentArchetype) {
                case "Passerine": skeleton = buildPasserine(); break;
                case "Raptor":    skeleton = buildRaptor(); break;
                case "Waterfowl": skeleton = buildWaterfowl(); break;
            }
            updatePoseCombo();
            initGeometrySliders();
            previewPanel.repaint();
            updateExportText();
        });

        loadBtn.addActionListener(e -> loadSelectedPreset());

        resetBtn.addActionListener(e -> {
            batchUpdating = true;
            for (JointSliderGroup g : sliderGroups.values()) {
                g.setValues(0, 0, 0);
            }
            // Reset geometry to defaults
            for (Joint j : skeleton.allJoints) {
                j.resetGeometry();
            }
            for (var gEntry : sliderGroups.entrySet()) {
                Joint j = skeleton.jointMap.get(gEntry.getKey());
                if (j != null) {
                    gEntry.getValue().setGeometryFromJoint(j);
                }
            }
            batchUpdating = false;
            currentPoseName = "custom";
            previewPanel.repaint();
            updateExportText();
            captureState();
        });

        geometryToggle.addActionListener(e -> {
            showGeometry = geometryToggle.isSelected();
            for (JointSliderGroup g : sliderGroups.values()) {
                if (g.geometryPanel != null) {
                    g.geometryPanel.setVisible(showGeometry);
                }
            }
            sliderPanel.revalidate();
            sliderPanel.repaint();
        });

        exportBtn.addActionListener(e -> {
            String code = generateExportCode();
            File outFile = resolveOutputFile("exported_pose.java");
            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(code);
                JOptionPane.showMessageDialog(this,
                        "Exported to: " + outFile.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error writing: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exportAllBtn.addActionListener(e -> exportAllPresets());

        // Load default preset
        loadSelectedPreset();
    }

    void updatePoseCombo() {
        poseCombo.removeAllItems();
        List<Preset> presets = allPresets.get(currentArchetype);
        if (presets != null) {
            for (Preset p : presets) {
                poseCombo.addItem(p.name);
            }
        }
    }

    void loadSelectedPreset() {
        String poseName = (String) poseCombo.getSelectedItem();
        if (poseName == null) return;
        currentPoseName = poseName;

        List<Preset> presets = allPresets.get(currentArchetype);
        Preset found = null;
        for (Preset p : presets) {
            if (p.name.equals(poseName)) { found = p; break; }
        }
        if (found == null) return;

        // Stop animation if switching presets
        if (animTimer != null && animPlaying) {
            animPlaying = false;
            animTimer.stop();
            if (playPauseBtn != null) playPauseBtn.setText("Play");
        }

        // Set up cyclic editing state
        if (found.isCyclic()) {
            editingCyclic = true;
            animControlsPanel.setVisible(true);
            cyclicBasePose = found.basePose;
            cyclicOffsetA = found.offsetA;
            cyclicOffsetB = found.offsetB;
            cyclicAnimName = found.cyclicName;
            // Determine A or B based on phase
            boolean isA = isEndpointA(found);
            cyclicEndpoint = isA ? "A" : "B";
            animPhase = isA ? 0f : 1f;
            updateCyclicStatusLabel();
        } else {
            editingCyclic = false;
            animControlsPanel.setVisible(false);
            cyclicBasePose = null;
            cyclicOffsetA = null;
            cyclicOffsetB = null;
            cyclicAnimName = null;
            cyclicEndpoint = null;
            updateCyclicStatusLabel();
        }
        if (phaseSlider != null) {
            batchUpdating = true;
            phaseSlider.setValue(Math.round(animPhase * 100));
            batchUpdating = false;
        }

        batchUpdating = true;
        // Reset all sliders first
        for (JointSliderGroup g : sliderGroups.values()) {
            g.setValues(0, 0, 0);
        }
        // Reset geometry to archetype defaults
        for (Joint j : skeleton.allJoints) {
            j.resetGeometry();
        }
        for (var gEntry : sliderGroups.entrySet()) {
            Joint j = skeleton.jointMap.get(gEntry.getKey());
            if (j != null) {
                gEntry.getValue().setGeometryFromJoint(j);
            }
        }
        // Apply preset values (only left-side and non-mirrored joints)
        for (var entry : found.joints.entrySet()) {
            String name = entry.getKey();
            float[] v = entry.getValue();
            JointSliderGroup g = sliderGroups.get(name);
            if (g != null) {
                g.setValues(v[0], v[1], v[2]);
            }
        }
        batchUpdating = false;
        previewPanel.repaint();
        updateExportText();
        captureState();
    }

    /** True if this preset represents the "A" (phase=0) endpoint. */
    boolean isEndpointA(Preset p) {
        if (!p.isCyclic()) return false;
        // The A endpoint's combined joints match base + offsetA
        Map<String, float[]> expectedA = mergePoseOffset(p.basePose, p.offsetA);
        return mapsApproxEqual(p.joints, expectedA);
    }

    boolean mapsApproxEqual(Map<String, float[]> a, Map<String, float[]> b) {
        if (a.size() != b.size()) return false;
        for (var entry : a.entrySet()) {
            float[] bv = b.get(entry.getKey());
            if (bv == null) return false;
            float[] av = entry.getValue();
            if (Math.abs(av[0]-bv[0]) > 0.001f || Math.abs(av[1]-bv[1]) > 0.001f || Math.abs(av[2]-bv[2]) > 0.001f)
                return false;
        }
        return true;
    }

    String getEndpointNameA(String displayName) {
        // e.g. "wingbeat: wings_up" → "wings_up"
        int idx = displayName.indexOf(": ");
        return idx >= 0 ? displayName.substring(idx + 2) : displayName;
    }

    void updateCyclicStatusLabel() {
        if (cyclicStatusLabel == null) return;
        if (editingCyclic && cyclicAnimName != null) {
            cyclicStatusLabel.setText("Editing: " + cyclicAnimName + " [" + cyclicEndpoint + "]");
            cyclicStatusLabel.setForeground(new Color(0, 100, 180));
        } else {
            cyclicStatusLabel.setText("Static pose");
            cyclicStatusLabel.setForeground(new Color(100, 100, 100));
        }
    }

    void togglePlayPause() {
        if (!editingCyclic) {
            JOptionPane.showMessageDialog(this,
                    "Select a cyclic preset (e.g. \"wingbeat: wings_up\") first.",
                    "No Cyclic Preset Loaded", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        animPlaying = !animPlaying;
        if (animPlaying) {
            animElapsedTicks = 0f;  // restart from beginning
            playPauseBtn.setText("Stop");
            animTimer.start();
        } else {
            // Stop: snap back to current endpoint for editing
            playPauseBtn.setText("Play");
            animTimer.stop();
            float snapPhase = "A".equals(cyclicEndpoint) ? 0f : 1f;
            animPhase = snapPhase;
            batchUpdating = true;
            phaseSlider.setValue(Math.round(snapPhase * 100));
            batchUpdating = false;
            applyPhase(snapPhase);
        }
    }

    /**
     * Apply a blended cyclic pose at the given phase (0=offsetA, 1=offsetB).
     * Sets slider values to base + lerp(offsetA, offsetB, phase).
     */
    /** Capture current slider values as the active endpoint's offset (slider value - base). */
    void updateCurrentEndpointFromSliders() {
        if (!editingCyclic || cyclicBasePose == null) return;
        Map<String, float[]> target = "A".equals(cyclicEndpoint) ? cyclicOffsetA :
                                      "B".equals(cyclicEndpoint) ? cyclicOffsetB : null;
        if (target == null) return;
        for (var entry : sliderGroups.entrySet()) {
            String name = entry.getKey();
            JointSliderGroup g = entry.getValue();
            float[] baseV = cyclicBasePose.getOrDefault(name, new float[]{0f, 0f, 0f});
            // offset = current slider value - base
            target.put(name, new float[]{
                g.getX() - baseV[0],
                g.getY() - baseV[1],
                g.getZ() - baseV[2]
            });
        }
    }

    void applyPhase(float phase) {
        if (!editingCyclic || cyclicBasePose == null || cyclicOffsetA == null || cyclicOffsetB == null) return;

        // Don't capture here — captured on slider change instead

        // Collect all joint names across base, offsetA, offsetB
        Set<String> allJointNames = new LinkedHashSet<>();
        allJointNames.addAll(cyclicBasePose.keySet());
        allJointNames.addAll(cyclicOffsetA.keySet());
        allJointNames.addAll(cyclicOffsetB.keySet());

        batchUpdating = true;
        for (String name : allJointNames) {
            JointSliderGroup g = sliderGroups.get(name);
            if (g == null) continue;  // right-side joints are auto-mirrored
            float[] baseV = cyclicBasePose.getOrDefault(name, new float[]{0f, 0f, 0f});
            float[] offAV  = cyclicOffsetA.getOrDefault(name,  new float[]{0f, 0f, 0f});
            float[] offBV  = cyclicOffsetB.getOrDefault(name,  new float[]{0f, 0f, 0f});
            float rx = baseV[0] + offAV[0] + (offBV[0] - offAV[0]) * phase;
            float ry = baseV[1] + offAV[1] + (offBV[1] - offAV[1]) * phase;
            float rz = baseV[2] + offAV[2] + (offBV[2] - offAV[2]) * phase;
            g.setValues(rx, ry, rz);
        }
        batchUpdating = false;
        previewPanel.repaint();
        updateExportText();
    }

    void buildSliderPanel() {
        sliderPanel.removeAll();
        sliderGroups.clear();

        addSliderSection("Spine", SPINE_JOINTS, SPINE_BLUE);
        addSliderSection("Neck + Head", NECK_HEAD_JOINTS, NECK_PINK);
        addSliderSection("Left Wing (R auto-mirrors)", LEFT_WING_JOINTS, WING_GREEN);
        addSliderSection("Tail", TAIL_JOINTS, TAIL_YELLOW);
        addSliderSection("Left Leg (R auto-mirrors)", LEFT_LEG_JOINTS, LEG_ORANGE);

        // Initialise geometry sliders from skeleton defaults
        initGeometrySliders();

        sliderPanel.add(Box.createVerticalGlue());
        sliderPanel.revalidate();
    }

    void initGeometrySliders() {
        batchUpdating = true;
        for (var entry : sliderGroups.entrySet()) {
            Joint j = skeleton.jointMap.get(entry.getKey());
            if (j != null) {
                entry.getValue().setGeometryFromJoint(j);
            }
        }
        batchUpdating = false;
    }

    void addSliderSection(String title, String[] jointNames, Color colour) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        Color borderColour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 150);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderColour, 2),
                        title,
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 12),
                        borderColour),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        for (String name : jointNames) {
            JointSliderGroup group = new JointSliderGroup(name);
            sliderGroups.put(name, group);
            section.add(group.buildPanel());
            section.add(Box.createVerticalStrut(2));
            jointToSection.put(name, section);
            // Also map R_ mirror to same section
            if (name.startsWith("L_")) {
                jointToSection.put("R_" + name.substring(2), section);
            }
        }

        section.setAlignmentX(0f);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height + 200));
        allSections.add(section);
        sliderPanel.add(section);
        sliderPanel.add(Box.createVerticalStrut(4));
    }

    void updateSliderVisibility() {
        String sel = previewPanel.selectedJoint;
        if (sel == null) {
            // Nothing selected — show all sections
            for (JPanel s : allSections) s.setVisible(true);
        } else {
            // Show only the section containing the selected joint
            JPanel activeSection = jointToSection.get(sel);
            for (JPanel s : allSections) s.setVisible(s == activeSection);
        }
        sliderPanel.revalidate();
        sliderPanel.repaint();
    }

    File resolveOutputFile(String filename) {
        // Try to determine if we're in the tools directory or project root
        File toolsDir = new File("tools");
        if (new File("PoseEditor.java").exists()) {
            return new File(filename);
        } else if (toolsDir.exists()) {
            return new File(toolsDir, filename);
        }
        return new File(filename);
    }

    void exportAllPresets() {
        StringBuilder sb = new StringBuilder();
        sb.append("// All poses exported from PoseEditor\n");
        sb.append("// Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");

        for (var archEntry : allPresets.entrySet()) {
            String arch = archEntry.getKey();
            sb.append("// ").append("=".repeat(60)).append("\n");
            sb.append("// Archetype: ").append(arch).append("\n");
            sb.append("// ").append("=".repeat(60)).append("\n\n");

            for (Preset preset : archEntry.getValue()) {
                sb.append("// ").append(arch).append(" — ").append(preset.name).append("\n");
                sb.append("public static final PoseData ").append(preset.name.toUpperCase())
                        .append(" = PoseData.builder(\"").append(preset.name).append("\")\n");
                boolean hasMirror = false;
                for (var jEntry : preset.joints.entrySet()) {
                    String name = jEntry.getKey();
                    // Only write L_ joints and non-mirrored joints
                    if (name.startsWith("R_")) continue;
                    float[] v = jEntry.getValue();
                    if (v[0] != 0 || v[1] != 0 || v[2] != 0) {
                        String skelName = toSkeletonConstant(name);
                        sb.append("        .joint(BirdSkeleton.").append(skelName).append(", ")
                                .append(formatFloat(v[0])).append(", ")
                                .append(formatFloat(v[1])).append(", ")
                                .append(formatFloat(v[2])).append(")\n");
                        if (name.startsWith("L_")) hasMirror = true;
                    }
                }
                if (hasMirror) sb.append("        .mirror()\n");
                sb.append("        .build();\n\n");
            }
        }

        File outFile = resolveOutputFile("exported_all_poses.java");
        try (FileWriter fw = new FileWriter(outFile)) {
            fw.write(sb.toString());
            JOptionPane.showMessageDialog(this,
                    "Exported all presets to: " + outFile.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error writing: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Main
    // =========================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // fall back to default
            }
            new PoseEditor().setVisible(true);
        });
    }
}
