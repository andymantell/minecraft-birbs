import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Rendering panel with 2x2 grid (front, side, top, 3D) and mouse interaction.
 * Extracted from PoseEditor during refactor — no logic changes.
 */
class PreviewPanel extends JPanel {

    final float SCALE = 14f;
    final PoseEditor editor;

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

    PreviewPanel(PoseEditor editor) {
        this.editor = editor;
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
                        editor.captureState();  // snapshot BEFORE drag starts
                        draggingAxis = handleHit;
                        draggingJoint = selectedJoint;
                        handleDragStartX = mx;
                        handleDragStartY = my;
                        dragCurX = mx;
                        dragCurY = my;
                        // Get the current rotation value for this axis
                        String sliderName = draggingJoint.startsWith("R_") ?
                                "L_" + draggingJoint.substring(2) : draggingJoint;
                        SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sliderName);
                        if (grp != null) {
                            handleDragStartValue = draggingAxis == 1 ? grp.getX() :
                                    draggingAxis == 2 ? grp.getY() : grp.getZ();
                        }
                        return;
                    }
                }

                // Hit test for joint under cursor — used both for IK drag and selection
                Map<String, float[]> poseData = editor.getCurrentPose();
                SkeletonGeometry.applyPose(editor.skeleton, poseData);
                SkeletonGeometry.computeFK(editor.skeleton);

                String hit = hitTestJoint(mx, my, cellW, cellH);

                // If clicking on a joint body (not a handle), start IK drag
                if (hit != null) {
                    editor.captureState();  // snapshot BEFORE IK starts
                    ikDragging = true;
                    // Map R_ to L_ for mirrored joints, but NOT for independent legs
                    boolean isRLeg = hit.startsWith("R_") && (hit.contains("thigh") ||
                        hit.contains("shin") || hit.contains("tarsus") || hit.contains("foot"));
                    boolean shouldMirror = hit.startsWith("R_") && !(isRLeg && !editor.mirrorLegs);
                    String ikTarget = shouldMirror ? "L_" + hit.substring(2) : hit;
                    ikJointName = ikTarget;
                    ikMirrorTarget = shouldMirror;
                    ikViewQuadrant = getQuadrant(mx, my, cellW, cellH);
                    ikChain = buildIkChain(ikTarget);
                    ikTargetScreenX = mx;
                    ikTargetScreenY = my;
                    selectedJoint = hit;
                    editor.updateSliderVisibility();
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
                editor.updateSliderVisibility();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (ikDragging) {
                    editor.updateExportText();
                    if (editor.editingCyclic) editor.updateCurrentEndpointFromSliders();
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
                    SkeletonGeometry.Joint draggedJoint = editor.skeleton.jointMap.get(ikJointName);
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
                        Map<String, float[]> finalPose = editor.getCurrentPose();
                        SkeletonGeometry.applyPose(editor.skeleton, finalPose);
                        SkeletonGeometry.computeFK(editor.skeleton);
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
                    SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sliderName);
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
        if (jointName.endsWith("_primaries") || jointName.endsWith("_hand") ||
                jointName.endsWith("_secondaries") || jointName.endsWith("_forearm") ||
                jointName.endsWith("_scapulars") || jointName.endsWith("_upper_wing")) {
            return "shoulder_mount";
        }
        if (jointName.endsWith("_foot") || jointName.endsWith("_tarsus") ||
                jointName.endsWith("_shin") || jointName.endsWith("_thigh")) {
            return "hip";
        }
        if (jointName.equals("neck_upper") || jointName.equals("neck_mid") ||
                jointName.equals("neck_lower")) {
            return "chest";
        }
        if (jointName.equals("head") || jointName.equals("lower_beak") ||
                jointName.equals("upper_beak")) {
            return "neck_lower";
        }
        if (jointName.equals("tail_fan")) {
            return "tail_base";
        }
        if (jointName.equals("tail_base")) {
            return "tail_base";
        }
        if (jointName.equals("hip") || jointName.equals("torso") ||
                jointName.equals("shoulder_mount")) {
            return "chest";
        }
        return null;
    }

    List<String> buildIkChain(String draggedJointName) {
        String root = ikChainRoot(draggedJointName);
        if (root == null) return new ArrayList<>();

        List<String> chain = new ArrayList<>();
        chain.add(draggedJointName);

        SkeletonGeometry.Joint j = editor.skeleton.jointMap.get(draggedJointName);
        while (j != null && !j.name.equals(root)) {
            if (j.parent == null) break;
            chain.add(0, j.parent.name);
            if (j.parent.name.equals(root)) break;
            j = j.parent;
        }
        if (!chain.isEmpty() && !chain.get(0).equals(root)) {
            chain.add(0, root);
        }
        return chain;
    }

    // ----------------------------------------------------------------
    // IK solver
    // ----------------------------------------------------------------

    double[] screenToWorld(int sx, int sy, int q, int cellW, int cellH, SkeletonGeometry.Joint draggedJoint) {
        float es = SCALE * zoomForQuadrant(q);
        float px = panXForQuadrant(q);
        float py = panYForQuadrant(q);
        int col = (q == 1 || q == 3) ? 1 : 0;
        int row = (q == 2 || q == 3) ? 1 : 0;

        double wA = (sx - col * cellW - cellW / 2.0 - px) / es;
        double wB = (sy - row * cellH - cellH / 2.0 - py) / es + 19.0;

        double wx = draggedJoint.worldPos[0];
        double wy = draggedJoint.worldPos[1];
        double wz = draggedJoint.worldPos[2];

        switch (q) {
            case 0 -> { wx = wA; wy = wB; }
            case 1 -> { wz = wA; wy = wB; }
            case 2 -> { wx = wA; wz = wB; }
            default -> { wx = wA; wy = wB; }
        }
        return new double[]{wx, wy, wz};
    }

    void ccdStep(SkeletonGeometry.Joint joint, double[] endEffectorPos, double[] targetPos, int q) {
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

        String sliderName = joint.name.startsWith("R_") ? "L_" + joint.name.substring(2) : joint.name;
        SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sliderName);
        if (grp == null) return;

        switch (q) {
            case 0 -> {
                double cross = ex * ty - ey * tx;
                double mag = Math.sqrt((ex*ex+ey*ey) * (tx*tx+ty*ty));
                if (mag > 0.001) {
                    delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                    joint.angleZ = clampAngle(joint.angleZ + (float) delta);
                }
            }
            case 1 -> {
                double cross = -(ez * ty - ey * tz);
                double mag = Math.sqrt((ez*ez+ey*ey) * (tz*tz+ty*ty));
                if (mag > 0.001) {
                    delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                    joint.angleX = clampAngle(joint.angleX + (float) delta);
                }
            }
            case 2 -> {
                double cross = -(ex * tz - ez * tx);
                double mag = Math.sqrt((ex*ex+ez*ez) * (tx*tx+tz*tz));
                if (mag > 0.001) {
                    delta = clampDelta(Math.asin(Math.max(-1, Math.min(1, cross / mag))) * damping, maxStep);
                    joint.angleY = clampAngle(joint.angleY + (float) delta);
                }
            }
            default -> {
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

        // Enforce joint constraints
        if (joint.name.contains("upper_wing")) {
            float limit = 1.2f;
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

    void solveIK(double[] targetWorldPos, int iterations) {
        SkeletonGeometry.Joint endEffector = editor.skeleton.jointMap.get(ikJointName);
        if (endEffector == null || ikChain.isEmpty()) return;

        Map<String, float[]> poseData = editor.getCurrentPose();
        SkeletonGeometry.applyPose(editor.skeleton, poseData);
        SkeletonGeometry.computeFK(editor.skeleton);

        // Special case: 1-joint chain
        if (ikChain.size() == 1) {
            double[] jp = endEffector.worldPos;
            double tx = targetWorldPos[0] - jp[0];
            double ty = targetWorldPos[1] - jp[1];
            double tz = targetWorldPos[2] - jp[2];
            float damp = 0.08f;
            float ms = 0.05f;
            switch (ikViewQuadrant) {
                case 0 -> {
                    double a = Math.atan2(ty, tx);
                    endEffector.angleZ = clampAngle(endEffector.angleZ - (float)(clampDelta(a * damp, ms)));
                }
                case 1 -> {
                    double a = Math.atan2(ty, tz);
                    endEffector.angleX = clampAngle(endEffector.angleX + (float)(clampDelta(a * damp, ms)));
                }
                case 2 -> {
                    double a = Math.atan2(tz, tx);
                    endEffector.angleY = clampAngle(endEffector.angleY + (float)(clampDelta(a * damp, ms)));
                }
                default -> {
                    double a = Math.atan2(ty, tx);
                    endEffector.angleZ = clampAngle(endEffector.angleZ - (float)(clampDelta(a * damp, ms)));
                }
            }
            SkeletonGeometry.computeFK(editor.skeleton);
            editor.batchUpdating = true;
            String sn = ikJointName.startsWith("R_") ? "L_" + ikJointName.substring(2) : ikJointName;
            SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sn);
            if (grp != null) {
                grp.xSlider.setValue(Math.round(endEffector.angleX * 100));
                grp.ySlider.setValue(Math.round(endEffector.angleY * 100));
                grp.zSlider.setValue(Math.round(endEffector.angleZ * 100));
            }
            editor.batchUpdating = false;
            return;
        }

        String rootName = ikChain.get(0);
        boolean structuralRoot = rootName.equals("shoulder_mount") ||
                                 rootName.equals("hip") || rootName.equals("chest");
        int endIndex = structuralRoot ? 1 : 0;

        for (int iter = 0; iter < iterations; iter++) {
            for (int i = ikChain.size() - 2; i >= endIndex; i--) {
                String jName = ikChain.get(i);
                SkeletonGeometry.Joint joint = editor.skeleton.jointMap.get(jName);
                if (joint == null) continue;

                double[] endPos = editor.skeleton.jointMap.get(ikJointName).worldPos.clone();
                ccdStep(joint, endPos, targetWorldPos, ikViewQuadrant);
                SkeletonGeometry.computeFK(editor.skeleton);
            }

            // Also adjust the end effector
            if (endEffector.parent != null) {
                double[] parentPos = endEffector.parent.worldPos;
                double[] effPos = endEffector.worldPos.clone();
                double pex = effPos[0] - parentPos[0];
                double pey = effPos[1] - parentPos[1];
                double pez = effPos[2] - parentPos[2];
                double ptx = targetWorldPos[0] - parentPos[0];
                double pty = targetWorldPos[1] - parentPos[1];
                double ptz = targetWorldPos[2] - parentPos[2];
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
            SkeletonGeometry.computeFK(editor.skeleton);
        }

        editor.batchUpdating = true;
        for (String jName : ikChain) {
            SkeletonGeometry.Joint joint = editor.skeleton.jointMap.get(jName);
            SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(jName);
            if (grp != null && joint != null) {
                grp.xSlider.setValue(Math.round(joint.angleX * 100));
                grp.ySlider.setValue(Math.round(joint.angleY * 100));
                grp.zSlider.setValue(Math.round(joint.angleZ * 100));
            }
        }
        editor.batchUpdating = false;
    }

    void syncIkSliderFields() {
        for (String jName : ikChain) {
            String sliderName = jName.startsWith("R_") ? "L_" + jName.substring(2) : jName;
            SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sliderName);
            if (grp != null) {
                grp.xField.setText(String.format("%.2f", grp.getX()));
                grp.yField.setText(String.format("%.2f", grp.getY()));
                grp.zField.setText(String.format("%.2f", grp.getZ()));
            }
        }
    }

    int hitTestHandles(int mx, int my, int cellW, int cellH) {
        if (selectedJoint == null) return 0;
        SkeletonGeometry.Joint j = editor.skeleton.jointMap.get(selectedJoint);
        if (j == null) return 0;

        int q = getQuadrant(mx, my, cellW, cellH);
        float es = SCALE * zoomForQuadrant(q);
        float px = panXForQuadrant(q);
        float py = panYForQuadrant(q);
        int col = (q == 1 || q == 3) ? 1 : 0;
        int row = (q == 2 || q == 3) ? 1 : 0;

        int jx, jy;
        if (q < 3) {
            SkeletonGeometry.View view = q == 0 ? SkeletonGeometry.View.FRONT : q == 1 ? SkeletonGeometry.View.SIDE : SkeletonGeometry.View.TOP;
            double[] p2d = SkeletonGeometry.project(j.worldPos, view);
            jx = toScreenX(p2d[0], cellW, col, row, es, px);
            jy = toScreenY(p2d[1], cellH, col, row, es, py);
        } else {
            double[] p3 = project3D(j.worldPos, cellW, cellH, col, row, es, px, py);
            jx = (int) p3[0];
            jy = (int) p3[1];
        }

        if (Math.hypot(mx - (jx + 20), my - jy) < 11) return 1;
        if (Math.hypot(mx - jx, my - (jy - 20)) < 11) return 2;
        if (Math.hypot(mx - (jx + 14), my - (jy - 14)) < 11) return 3;
        return 0;
    }

    String hitTestJoint(int mx, int my, int cellW, int cellH) {
        int q = getQuadrant(mx, my, cellW, cellH);
        float es = SCALE * zoomForQuadrant(q);
        float px = panXForQuadrant(q);
        float py = panYForQuadrant(q);
        int col = (q == 1 || q == 3) ? 1 : 0;
        int row = (q == 2 || q == 3) ? 1 : 0;

        String bestJoint = null;
        double bestArea = Double.MAX_VALUE;

        for (SkeletonGeometry.Joint j : editor.skeleton.allJoints) {
            double[][] corners = SkeletonGeometry.getCuboidCorners(j);
            int minSx = Integer.MAX_VALUE, maxSx = Integer.MIN_VALUE;
            int minSy = Integer.MAX_VALUE, maxSy = Integer.MIN_VALUE;

            for (int i = 0; i < 8; i++) {
                int sx, sy;
                if (q < 3) {
                    SkeletonGeometry.View view = q == 0 ? SkeletonGeometry.View.FRONT : q == 1 ? SkeletonGeometry.View.SIDE : SkeletonGeometry.View.TOP;
                    double[] p2d = SkeletonGeometry.project(corners[i], view);
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

    boolean isSelected(SkeletonGeometry.Joint j) {
        return selectedJoint != null && selectedJoint.equals(j.name);
    }

    // ----------------------------------------------------------------
    // Drawing methods
    // ----------------------------------------------------------------

    void drawCuboid(Graphics2D g, SkeletonGeometry.Joint j, SkeletonGeometry.View view, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
        double[][] corners = SkeletonGeometry.getCuboidCorners(j);
        int[] sx = new int[8], sy = new int[8];
        for (int i = 0; i < 8; i++) {
            double[] p2d = SkeletonGeometry.project(corners[i], view);
            sx[i] = toScreenX(p2d[0], cellW, col, row, scale, panX);
            sy[i] = toScreenY(p2d[1], cellH, col, row, scale, panY);
        }
        Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), editor.solidFill ? 200 : 60);
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

    void drawJointDot(Graphics2D g, SkeletonGeometry.Joint j, SkeletonGeometry.View view, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
        double[] p2d = SkeletonGeometry.project(j.worldPos, view);
        int x = toScreenX(p2d[0], cellW, col, row, scale, panX);
        int y = toScreenY(p2d[1], cellH, col, row, scale, panY);
        g.setColor(j.colour);
        g.fillOval(x-3, y-3, 6, 6);
        g.setColor(j.colour.darker().darker());
        g.setStroke(new BasicStroke(1f));
        g.drawOval(x-3, y-3, 6, 6);
    }

    void drawHandles(Graphics2D g, SkeletonGeometry.Joint j, int jx, int jy) {
        drawRotationHandle(g, jx, jy, new Color(220, 40, 40), "X", 20, 0, hoveredAxis == 1);
        drawRotationHandle(g, jx, jy, new Color(40, 180, 40), "Y", 0, -20, hoveredAxis == 2);
        drawRotationHandle(g, jx, jy, new Color(40, 80, 220), "Z", 14, -14, hoveredAxis == 3);
    }

    void drawRotationHandle(Graphics2D g, int cx, int cy, Color color, String axis, int offsetX, int offsetY, boolean hovered) {
        int hx = cx + offsetX;
        int hy = cy + offsetY;

        if (hovered) {
            g.setColor(new Color(255, 255, 180, 220));
        } else {
            g.setColor(new Color(255, 255, 255, 180));
        }
        g.fillOval(hx - 10, hy - 10, 20, 20);

        g.setColor(color);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int startAngle, arcAngle;
        switch (axis) {
            case "X" -> { startAngle = 45;   arcAngle = 180; }
            case "Y" -> { startAngle = -45;  arcAngle = 180; }
            default  -> { startAngle = 135;  arcAngle = 180; }
        }
        g.drawArc(hx - 8, hy - 8, 16, 16, startAngle, arcAngle);

        double endAngleRad = Math.toRadians(startAngle + arcAngle);
        int ax = hx + (int)(8 * Math.cos(endAngleRad));
        int ay = hy - (int)(8 * Math.sin(endAngleRad));
        double tanX = Math.sin(endAngleRad);
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

        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString(axis, hx + 7, hy - 7);

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

    void drawPanel(Graphics2D g, SkeletonGeometry.View view, int cellW, int cellH, int col, int row, String label, float zoom, float panX, float panY) {
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

        Shape oldClip = g.getClip();
        g.setClip(x0, y0, cellW, cellH);

        drawGroundPlane(g, cellW, cellH, col, row, es, panY);

        for (SkeletonGeometry.Joint j : editor.skeleton.allJoints) drawCuboid(g, j, view, cellW, cellH, col, row, es, panX, panY);
        for (SkeletonGeometry.Joint j : editor.skeleton.allJoints) drawJointDot(g, j, view, cellW, cellH, col, row, es, panX, panY);

        if (selectedJoint != null) {
            SkeletonGeometry.Joint sel = editor.skeleton.jointMap.get(selectedJoint);
            if (sel != null) {
                double[] p2d = SkeletonGeometry.project(sel.worldPos, view);
                int jx = toScreenX(p2d[0], cellW, col, row, es, panX);
                int jy = toScreenY(p2d[1], cellH, col, row, es, panY);
                drawHandles(g, sel, jx, jy);
            }
        }

        drawAxisIndicator(g, view, cellW, cellH, col, row);

        g.setClip(oldClip);
    }

    void drawCuboid3D(Graphics2D g, SkeletonGeometry.Joint j, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
        double[][] corners = SkeletonGeometry.getCuboidCorners(j);
        int[] sx = new int[8], sy = new int[8];
        double[] depths = new double[8];
        for (int i = 0; i < 8; i++) {
            double[] p3 = project3D(corners[i], cellW, cellH, col, row, scale, panX, panY);
            sx[i] = (int) p3[0];
            sy[i] = (int) p3[1];
            depths[i] = p3[2];
        }
        Color fill = new Color(j.colour.getRed(), j.colour.getGreen(), j.colour.getBlue(), editor.solidFill ? 200 : 60);
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

    void drawJointDot3D(Graphics2D g, SkeletonGeometry.Joint j, int cellW, int cellH, int col, int row, float scale, float panX, float panY) {
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

        Shape oldClip = g.getClip();
        g.setClip(x0, y0, cellW, cellH);

        List<SkeletonGeometry.Joint> sorted = new ArrayList<>(editor.skeleton.allJoints);
        sorted.sort((a, b) -> {
            double[] pa = project3D(a.worldPos, cellW, cellH, col, row, es, panX, panY);
            double[] pb = project3D(b.worldPos, cellW, cellH, col, row, es, panX, panY);
            return Double.compare(pb[2], pa[2]);
        });
        for (SkeletonGeometry.Joint j : sorted) drawCuboid3D(g, j, cellW, cellH, col, row, es, panX, panY);
        for (SkeletonGeometry.Joint j : sorted) drawJointDot3D(g, j, cellW, cellH, col, row, es, panX, panY);

        if (selectedJoint != null) {
            SkeletonGeometry.Joint sel = editor.skeleton.jointMap.get(selectedJoint);
            if (sel != null) {
                double[] p3 = project3D(sel.worldPos, cellW, cellH, col, row, es, panX, panY);
                int jx = (int) p3[0];
                int jy = (int) p3[1];
                drawHandles(g, sel, jx, jy);
            }
        }

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

        Map<String, float[]> poseData = editor.getCurrentPose();
        SkeletonGeometry.applyPose(editor.skeleton, poseData);
        if (editor.mirrorLegs) {
            for (int i = 0; i < SkeletonGeometry.LEFT_LEG_JOINTS.length; i++) {
                SkeletonGeometry.Joint lj = editor.skeleton.jointMap.get(SkeletonGeometry.LEFT_LEG_JOINTS[i]);
                SkeletonGeometry.Joint rj = editor.skeleton.jointMap.get(SkeletonGeometry.RIGHT_LEG_JOINTS[i]);
                if (lj != null && rj != null) {
                    rj.angleX = lj.angleX;
                    rj.angleY = -lj.angleY;
                    rj.angleZ = -lj.angleZ;
                }
            }
        }
        SkeletonGeometry.computeFK(editor.skeleton);

        drawPanel(g, SkeletonGeometry.View.FRONT, cellW, cellH, 0, 0, "FRONT (from +Z)", zoomFront, panFrontX, panFrontY);
        drawPanel(g, SkeletonGeometry.View.SIDE,  cellW, cellH, 1, 0, "SIDE (from +X)",  zoomSide,  panSideX,  panSideY);
        drawPanel(g, SkeletonGeometry.View.TOP,   cellW, cellH, 0, 1, "TOP (from -Y)",   zoomTop,   panTopX,   panTopY);
        draw3DPanel(g, cellW, cellH, 1, 1, "3D (drag to rotate)", zoom3D, pan3DX, pan3DY);

        if (ikDragging && ikJointName != null) {
            drawIkOverlay(g, cellW, cellH);
        }

        if (draggingAxis > 0 && draggingJoint != null) {
            String sliderName = draggingJoint.startsWith("R_") ?
                    "L_" + draggingJoint.substring(2) : draggingJoint;
            SliderPanel.JointSliderGroup grp = editor.sliderGroups.get(sliderName);
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

        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String title = editor.currentArchetype + " — " + editor.currentPoseName.toUpperCase().replace('_', ' ');
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (w - fm.stringWidth(title)) / 2, h - 8);

        drawLegend(g, w, h);
    }

    void drawIkOverlay(Graphics2D g, int cellW, int cellH) {
        int q = ikViewQuadrant;
        float es = SCALE * zoomForQuadrant(q);
        float px = panXForQuadrant(q);
        float py = panYForQuadrant(q);
        int col = (q == 1 || q == 3) ? 1 : 0;
        int row = (q == 2 || q == 3) ? 1 : 0;

        Shape oldClip = g.getClip();
        g.setClip(col * cellW, row * cellH, cellW, cellH);

        for (String jName : ikChain) {
            SkeletonGeometry.Joint j = editor.skeleton.jointMap.get(jName);
            if (j == null) continue;
            int jx, jy;
            if (q < 3) {
                SkeletonGeometry.View view = q == 0 ? SkeletonGeometry.View.FRONT : q == 1 ? SkeletonGeometry.View.SIDE : SkeletonGeometry.View.TOP;
                double[] p2d = SkeletonGeometry.project(j.worldPos, view);
                jx = toScreenX(p2d[0], cellW, col, row, es, px);
                jy = toScreenY(p2d[1], cellH, col, row, es, py);
            } else {
                double[] p3 = project3D(j.worldPos, cellW, cellH, col, row, es, px, py);
                jx = (int) p3[0];
                jy = (int) p3[1];
            }
            g.setColor(new Color(255, 220, 60, 120));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(jx - 8, jy - 8, 16, 16);
        }

        SkeletonGeometry.Joint endJoint = editor.skeleton.jointMap.get(ikJointName);
        if (endJoint != null) {
            int ex, ey;
            if (q < 3) {
                SkeletonGeometry.View view = q == 0 ? SkeletonGeometry.View.FRONT : q == 1 ? SkeletonGeometry.View.SIDE : SkeletonGeometry.View.TOP;
                double[] p2d = SkeletonGeometry.project(endJoint.worldPos, view);
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
                {SkeletonGeometry.SPINE_BLUE, "Spine"}, {SkeletonGeometry.NECK_PINK, "Neck"}, {SkeletonGeometry.HEAD_PINK, "Head"},
                {SkeletonGeometry.WING_GREEN, "Wings"}, {SkeletonGeometry.LEG_ORANGE, "Legs"}, {SkeletonGeometry.TAIL_YELLOW, "Tail"},
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

    void drawAxisIndicator(Graphics2D g, SkeletonGeometry.View view, int cellW, int cellH, int col, int row) {
        int cx = col * cellW + 40;
        int cy = row * cellH + cellH - 30;
        int len = 25;

        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));

        switch (view) {
            case FRONT -> {
                drawAxisArrow(g, cx, cy, cx + len, cy, Color.RED, "X");
                drawAxisArrow(g, cx, cy, cx, cy - len, Color.GREEN, "Y(-up)");
            }
            case SIDE -> {
                drawAxisArrow(g, cx, cy, cx + len, cy, Color.BLUE, "Z");
                drawAxisArrow(g, cx, cy, cx, cy - len, Color.GREEN, "Y(-up)");
            }
            case TOP -> {
                drawAxisArrow(g, cx, cy, cx + len, cy, Color.RED, "X");
                drawAxisArrow(g, cx, cy, cx, cy - len, Color.BLUE, "Z(-fwd)");
            }
        }
    }

    void drawAxisIndicator3D(Graphics2D g, int cellW, int cellH, int col, int row) {
        int cx = col * cellW + 40;
        int cy = row * cellH + cellH - 30;
        int len = 25;

        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));

        double cosY = Math.cos(camYaw), sinY = Math.sin(camYaw);
        double cosP = Math.cos(camPitch), sinP = Math.sin(camPitch);

        double xx = cosY, xz = -sinY;
        double xy2 = -xz * sinP;
        drawAxisArrow(g, cx, cy, cx + (int)(xx * len), cy + (int)(xy2 * len), Color.RED, "X");

        double yy = cosP, yz = sinP;
        drawAxisArrow(g, cx, cy, cx, cy + (int)(yy * len), Color.GREEN, "Y");

        double zx = sinY, zz = cosY;
        double zy2 = -zz * sinP;
        drawAxisArrow(g, cx, cy, cx + (int)(zx * len), cy + (int)(zy2 * len), Color.BLUE, "Z");
    }

    void drawAxisArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color, String label) {
        int dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return;
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(dy, dx);
        int ax1 = x2 - (int)(7 * Math.cos(angle - 0.4));
        int ay1 = y2 - (int)(7 * Math.sin(angle - 0.4));
        int ax2 = x2 - (int)(7 * Math.cos(angle + 0.4));
        int ay2 = y2 - (int)(7 * Math.sin(angle + 0.4));
        g.fillPolygon(new int[]{x2, ax1, ax2}, new int[]{y2, ay1, ay2}, 3);
        g.drawString(label, x2 + 3, y2 - 3);
    }
}
