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
                    mirrored.put(rName, new float[]{v[0], -v[1], -v[2]});
                }
            }
        }
        pose.putAll(mirrored);
    }

    /** Named preset: archetype + pose name + joint values. */
    static class Preset {
        final String name;
        final Map<String, float[]> joints;
        Preset(String name, Map<String, float[]> joints) {
            this.name = name;
            this.joints = joints;
        }
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
        presets.put("Waterfowl", waterfowl);

        return presets;
    }

    static Preset preset(String name, Object... args) {
        Map<String, float[]> joints = pose(args);
        mirror(joints);
        return new Preset(name, joints);
    }

    // =========================================================================
    // Preview panel
    // =========================================================================

    class PreviewPanel extends JPanel {
        final float SCALE = 14f;

        PreviewPanel() {
            setPreferredSize(new Dimension(900, 400));
            setMinimumSize(new Dimension(600, 300));
            setBackground(new Color(245, 245, 240));
        }

        int toScreenX(double worldCoord, int panelW, int panelIndex) {
            return panelIndex * panelW + panelW / 2 + (int)(worldCoord * SCALE);
        }

        int toScreenY(double worldCoord, int panelH) {
            return panelH / 2 + (int)((worldCoord - 19f) * SCALE);
        }

        void drawCuboid(Graphics2D g, Joint j, View view, int panelW, int panelH, int panelIndex) {
            double[][] corners = getCuboidCorners(j);
            int[] sx = new int[8], sy = new int[8];
            for (int i = 0; i < 8; i++) {
                double[] p2d = project(corners[i], view);
                sx[i] = toScreenX(p2d[0], panelW, panelIndex);
                sy[i] = toScreenY(p2d[1], panelH);
            }
            Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), 60);
            g.setColor(fill);
            int[][] faces = {
                    {0,1,2,3},{4,5,6,7},{0,3,7,4},{1,2,6,5},{0,1,5,4},{3,2,6,7}
            };
            for (int[] face : faces) {
                int[] fx = new int[4], fy = new int[4];
                for (int i = 0; i < 4; i++) { fx[i] = sx[face[i]]; fy[i] = sy[face[i]]; }
                g.fillPolygon(fx, fy, 4);
            }
            Color edge = new Color(j.colour.getRed()/2, j.colour.getGreen()/2, j.colour.getBlue()/2, 200);
            g.setColor(edge);
            g.setStroke(new BasicStroke(1.2f));
            int[][] edges = {
                    {0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}
            };
            for (int[] e : edges) {
                g.drawLine(sx[e[0]], sy[e[0]], sx[e[1]], sy[e[1]]);
            }
        }

        void drawJointDot(Graphics2D g, Joint j, View view, int panelW, int panelH, int panelIndex) {
            double[] p2d = project(j.worldPos, view);
            int x = toScreenX(p2d[0], panelW, panelIndex);
            int y = toScreenY(p2d[1], panelH);
            g.setColor(j.colour);
            g.fillOval(x-3, y-3, 6, 6);
            g.setColor(j.colour.darker().darker());
            g.setStroke(new BasicStroke(1f));
            g.drawOval(x-3, y-3, 6, 6);
        }

        void drawGroundPlane(Graphics2D g, int panelW, int panelH, int panelIndex) {
            int groundY = toScreenY(24.0, panelH);
            g.setColor(new Color(140, 100, 60, 100));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 4f}, 0f));
            int x0 = panelIndex * panelW + 10;
            int x1 = panelIndex * panelW + panelW - 10;
            g.drawLine(x0, groundY, x1, groundY);
        }

        void drawPanel(Graphics2D g, View view, int panelW, int panelH, int panelIndex, String label) {
            int x0 = panelIndex * panelW;
            g.setColor(new Color(245, 245, 240));
            g.fillRect(x0, 0, panelW, panelH);
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(1f));
            g.drawRect(x0, 0, panelW - 1, panelH - 1);
            g.setColor(new Color(100, 100, 100));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(label, x0 + 8, 16);
            drawGroundPlane(g, panelW, panelH, panelIndex);

            for (Joint j : skeleton.allJoints) drawCuboid(g, j, view, panelW, panelH, panelIndex);
            for (Joint j : skeleton.allJoints) drawJointDot(g, j, view, panelW, panelH, panelIndex);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int panelW = w / 3;

            // Apply current slider values and compute FK
            Map<String, float[]> poseData = getCurrentPose();
            applyPose(skeleton, poseData);
            computeFK(skeleton);

            drawPanel(g, View.FRONT, panelW, h, 0, "FRONT");
            drawPanel(g, View.SIDE,  panelW, h, 1, "SIDE");
            drawPanel(g, View.TOP,   panelW, h, 2, "TOP");

            // Overlay: archetype + pose name
            g.setColor(new Color(40, 40, 40));
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String title = currentArchetype + " — " + currentPoseName.toUpperCase().replace('_', ' ');
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, (w - fm.stringWidth(title)) / 2, h - 8);

            // Legend
            drawLegend(g, w, h);
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
    // State
    // =========================================================================

    String currentArchetype = "Passerine";
    String currentPoseName = "perched";
    Skeleton skeleton;
    Map<String, List<Preset>> allPresets;
    Map<String, JointSliderGroup> sliderGroups = new LinkedHashMap<>();
    boolean batchUpdating = false;
    boolean showGeometry = false;

    PreviewPanel previewPanel;
    JTextArea exportTextArea;
    JComboBox<String> archetypeCombo;
    JComboBox<String> poseCombo;
    JPanel sliderPanel;
    JScrollPane sliderScrollPane;
    JCheckBox geometryToggle;

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
        sb.append("public static final PoseData ").append(currentPoseName.toUpperCase())
                .append(" = PoseData.builder(\"").append(currentPoseName).append("\")\n");

        boolean hasMirror = false;
        for (var entry : sliderGroups.entrySet()) {
            String name = entry.getKey();
            JointSliderGroup g = entry.getValue();
            float x = g.getX(), y = g.getY(), z = g.getZ();
            if (x != 0 || y != 0 || z != 0) {
                // Map slider name to BirdSkeleton constant name
                String skelName = toSkeletonConstant(name);
                sb.append("        .joint(BirdSkeleton.").append(skelName).append(", ")
                        .append(formatFloat(x)).append(", ")
                        .append(formatFloat(y)).append(", ")
                        .append(formatFloat(z)).append(")\n");
                if (name.startsWith("L_")) hasMirror = true;
            }
        }
        if (hasMirror) {
            sb.append("        .mirror()\n");
        }
        sb.append("        .build();\n");

        // Geometry section
        sb.append("\n// Geometry (archetype: ").append(currentArchetype).append(")\n");
        for (Joint j : skeleton.allJoints) {
            // Skip right-side joints (mirrored from left)
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

        geometryToggle = new JCheckBox("Show Geometry Controls");
        geometryToggle.setAlignmentX(0f);
        geometryToggle.setSelected(false);
        leftPanel.add(geometryToggle);
        leftPanel.add(Box.createVerticalGlue());

        // --- Center panel: preview ---
        previewPanel = new PreviewPanel();

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
        centerSplit.setResizeWeight(1.0);
        centerSplit.setDividerLocation(900);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerSplit, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // --- Event handlers ---
        archetypeCombo.addActionListener(e -> {
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
        }

        section.setAlignmentX(0f);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height + 200));
        sliderPanel.add(section);
        sliderPanel.add(Box.createVerticalStrut(4));
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
