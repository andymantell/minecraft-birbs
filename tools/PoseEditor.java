import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Interactive bird skeleton pose editor with 3-view preview and code export.
 *
 * <p>Usage: {@code javac *.java && java PoseEditor}
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
    // Undo/Redo
    // =========================================================================

    List<Map<String, float[]>> undoStack = new ArrayList<>();
    int undoIndex = -1;
    static final int MAX_UNDO = 100;

    void captureState() {
        Map<String, float[]> snapshot = new LinkedHashMap<>();
        for (var entry : sliderGroups.entrySet()) {
            SliderPanel.JointSliderGroup g = entry.getValue();
            snapshot.put(entry.getKey(), new float[]{g.getX(), g.getY(), g.getZ()});
        }
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
            SliderPanel.JointSliderGroup g = sliderGroups.get(entry.getKey());
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
    SkeletonGeometry.Skeleton skeleton;
    Map<String, List<PosePresets.Preset>> allPresets;
    Map<String, SliderPanel.JointSliderGroup> sliderGroups = new LinkedHashMap<>();
    boolean batchUpdating = false;
    boolean showGeometry = false;

    // --- Cyclic animation editing state ---
    boolean editingCyclic = false;
    Map<String, float[]> cyclicBasePose = null;
    Map<String, float[]> cyclicOffsetA = null;
    Map<String, float[]> cyclicOffsetB = null;
    String cyclicAnimName = null;
    String cyclicEndpoint = null;
    float animPhase = 0f;
    float animDirection = 1f;
    float animElapsedTicks = 0f;

    // --- Animation playback ---
    boolean animPlaying = false;
    float animSpeed = 1.0f;
    javax.swing.Timer animTimer;
    JSlider phaseSlider;
    JSlider speedSlider;
    JPanel animControlsPanel;
    JLabel headingLabel;
    boolean mirrorLegs = true;
    JPanel rLegSection = null;
    JButton playPauseBtn;
    JLabel cyclicStatusLabel;

    PreviewPanel previewPanel;
    JTextArea exportTextArea;
    JComboBox<String> archetypeCombo;
    JPanel poseBtnPanel;
    Map<String, JButton> poseButtons = new LinkedHashMap<>();
    JPanel sliderPanel;
    JScrollPane sliderScrollPane;
    JCheckBox geometryToggle;
    boolean solidFill = false;
    Map<String, JPanel> jointToSection = new HashMap<>();
    List<JPanel> allSections = new ArrayList<>();

    // --- Per-pose edited values (remembered across preset switches) ---
    Map<String, Map<String, float[]>> editedPoses = new HashMap<>();

    // =========================================================================
    // Build current pose from sliders
    // =========================================================================

    Map<String, float[]> getCurrentPose() {
        Map<String, float[]> pose = new LinkedHashMap<>();
        for (var entry : sliderGroups.entrySet()) {
            String name = entry.getKey();
            SliderPanel.JointSliderGroup g = entry.getValue();
            float x = g.getX(), y = g.getY(), z = g.getZ();
            if (x != 0 || y != 0 || z != 0) {
                pose.put(name, new float[]{x, y, z});
            }
            if (name.startsWith("L_")) {
                String rName = "R_" + name.substring(2);
                if (skeleton.jointMap.containsKey(rName)) {
                    boolean isLeg = name.contains("thigh") || name.contains("shin") ||
                                    name.contains("tarsus") || name.contains("foot");
                    if (isLeg && !mirrorLegs) {
                        SliderPanel.JointSliderGroup rGroup = sliderGroups.get(rName);
                        if (rGroup != null) {
                            float rx = rGroup.getX(), ry = rGroup.getY(), rz = rGroup.getZ();
                            if (rx != 0 || ry != 0 || rz != 0) {
                                pose.put(rName, new float[]{rx, ry, rz});
                            }
                        }
                    } else {
                        if (x != 0 || y != 0 || z != 0) {
                            pose.put(rName, new float[]{x, -y, -z});
                        }
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
            sb.append("public static final CyclicAnimation ").append(cyclicAnimName.toUpperCase())
                    .append(" = new CyclicAnimation(\n");
            sb.append("    \"").append(cyclicAnimName).append("\",\n");

            sb.append("    PoseData.builder(\"").append(getOffsetAName()).append("\")\n");
            boolean mirrorA = exportOffsetBlock(sb, cyclicOffsetA);
            if (mirrorA) sb.append("            .mirror()\n");
            sb.append("            .build(),\n");

            sb.append("    PoseData.builder(\"").append(getOffsetBName()).append("\")\n");
            boolean mirrorB = exportOffsetBlock(sb, cyclicOffsetB);
            if (mirrorB) sb.append("            .mirror()\n");
            sb.append("            .build()\n");
            sb.append(");\n");

            sb.append("\n// Current phase: ").append(String.format("%.2f", animPhase))
              .append("  (0=").append(getOffsetAName()).append(", 1=").append(getOffsetBName()).append(")\n");
        } else {
            sb.append("public static final PoseData ").append(currentPoseName.toUpperCase())
                    .append(" = PoseData.builder(\"").append(currentPoseName).append("\")\n");

            boolean hasMirror = false;
            for (var entry : sliderGroups.entrySet()) {
                String name = entry.getKey();
                SliderPanel.JointSliderGroup g = entry.getValue();
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
        for (SkeletonGeometry.Joint j : skeleton.allJoints) {
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

    boolean exportOffsetBlock(StringBuilder sb, Map<String, float[]> offset) {
        boolean hasMirror = false;
        for (var entry : offset.entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("R_")) continue;
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
        if (cyclicAnimName == null) return "offset_a";
        List<PosePresets.Preset> presets = allPresets.get(currentArchetype);
        if (presets == null) return "offset_a";
        for (PosePresets.Preset p : presets) {
            if (p.isCyclic() && p.cyclicName.equals(cyclicAnimName)) {
                if (PosePresets.mapsApproxEqual(p.joints, PosePresets.mergePoseOffset(p.basePose, p.offsetA))) {
                    return p.endpointName;
                }
            }
        }
        return "offset_a";
    }

    String getOffsetBName() {
        if (cyclicAnimName == null) return "offset_b";
        List<PosePresets.Preset> presets = allPresets.get(currentArchetype);
        if (presets == null) return "offset_b";
        for (PosePresets.Preset p : presets) {
            if (p.isCyclic() && p.cyclicName.equals(cyclicAnimName)) {
                if (PosePresets.mapsApproxEqual(p.joints, PosePresets.mergePoseOffset(p.basePose, p.offsetB))) {
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

    File resolveOutputFile(String filename) {
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

            for (PosePresets.Preset preset : archEntry.getValue()) {
                sb.append("// ").append(arch).append(" — ").append(preset.name).append("\n");
                sb.append("public static final PoseData ").append(preset.name.toUpperCase())
                        .append(" = PoseData.builder(\"").append(preset.name).append("\")\n");
                boolean hasMirror = false;
                for (var jEntry : preset.joints.entrySet()) {
                    String name = jEntry.getKey();
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
    // UI construction
    // =========================================================================

    PoseEditor() {
        super("British Birds — Pose Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        allPresets = PosePresets.buildPresets();
        skeleton = SkeletonGeometry.buildPasserine();

        // --- Left panel: controls ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        leftPanel.setPreferredSize(new Dimension(220, 0));

        // Heading showing current archetype + pose
        // Heading moved to top bar (see below)

        JLabel archLabel = new JLabel("Archetype:");
        archLabel.setAlignmentX(0f);
        leftPanel.add(archLabel);
        archetypeCombo = new JComboBox<>(new String[]{"Passerine", "Raptor", "Waterfowl"});
        archetypeCombo.setMaximumSize(new Dimension(200, 28));
        archetypeCombo.setAlignmentX(0f);
        leftPanel.add(archetypeCombo);
        leftPanel.add(Box.createVerticalStrut(8));

        JLabel poseLabel = new JLabel("Poses:");
        poseLabel.setAlignmentX(0f);
        leftPanel.add(poseLabel);
        poseBtnPanel = new JPanel();
        poseBtnPanel.setAlignmentX(0f);
        poseBtnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        leftPanel.add(poseBtnPanel);
        leftPanel.add(Box.createVerticalStrut(8));

        JPanel resetBtnPanel = new JPanel();
        resetBtnPanel.setLayout(new BoxLayout(resetBtnPanel, BoxLayout.Y_AXIS));
        resetBtnPanel.setAlignmentX(0f);
        JButton resetBtn = new JButton("Zero All");
        JButton resetToPresetBtn = new JButton("Revert this Pose");
        JButton revertToSavedBtn = new JButton("Revert all Poses");
        resetBtn.setAlignmentX(0f);
        resetToPresetBtn.setAlignmentX(0f);
        revertToSavedBtn.setAlignmentX(0f);
        resetBtn.setToolTipText("Set all joints to zero rotation");
        resetToPresetBtn.setToolTipText("Reset this pose to its original hardcoded default");
        revertToSavedBtn.setToolTipText("Reload all poses from the JSON file on disk");
        resetBtnPanel.add(resetBtn);
        resetBtnPanel.add(Box.createVerticalStrut(2));
        resetBtnPanel.add(resetToPresetBtn);
        resetBtnPanel.add(Box.createVerticalStrut(2));
        resetBtnPanel.add(revertToSavedBtn);
        leftPanel.add(resetBtnPanel);
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

        // --- Animation controls ---
        animControlsPanel = new JPanel();
        animControlsPanel.setLayout(new BoxLayout(animControlsPanel, BoxLayout.Y_AXIS));
        animControlsPanel.setAlignmentX(0f);
        animControlsPanel.setVisible(false);

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

        animTimer = new javax.swing.Timer(33, e -> {
            if (!animPlaying || !editingCyclic) return;
            float mcTicksPerFrame = 20f / 30f;
            animElapsedTicks += mcTicksPerFrame;
            float flapAngle = (float) Math.sin(animElapsedTicks * animSpeed);
            animPhase = flapAngle * 0.5f + 0.5f;
            batchUpdating = true;
            phaseSlider.setValue(Math.round(animPhase * 100));
            batchUpdating = false;
            applyPhase(animPhase);
        });

        leftPanel.add(Box.createVerticalGlue());

        // --- Center panel: preview ---
        previewPanel = new PreviewPanel(this);
        previewPanel.setMinimumSize(new Dimension(600, 300));

        // --- Right panel: sliders ---
        sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        SliderPanel.buildSliderPanel(this);
        sliderScrollPane = new JScrollPane(sliderPanel);
        sliderScrollPane.setPreferredSize(new Dimension(320, 0));
        sliderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sliderScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- Bottom panel: export ---
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 4));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        JPanel exportButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveJsonBtn = new JButton("\uD83D\uDCBE Save");
        JButton loadJsonBtn = new JButton("\uD83D\uDCC2 Load");
        saveJsonBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        loadJsonBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        exportButtons.add(saveJsonBtn);
        exportButtons.add(loadJsonBtn);
        bottomPanel.add(exportButtons, BorderLayout.CENTER);

        // --- Layout ---
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, sliderScrollPane);
        centerSplit.setResizeWeight(0.7);
        centerSplit.setDividerLocation(700);

        // --- Top heading bar (full-width, tall, unmissable) ---
        headingLabel = new JLabel("  Passerine \u2014 perched") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(60, 90, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(220, 230, 245));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawString(getText().trim(), 12, 22);
            }
        };
        headingLabel.setPreferredSize(new Dimension(0, 32));
        headingLabel.setMinimumSize(new Dimension(0, 32));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headingLabel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerSplit, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // --- Keyboard shortcuts ---
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (e.isShiftDown()) {
                        redo();
                    } else {
                        undo();
                    }
                    return true;
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
            if (animTimer != null && animPlaying) {
                animPlaying = false;
                animTimer.stop();
                if (playPauseBtn != null) playPauseBtn.setText("Play");
            }
            editedPoses.clear();
            currentPoseName = null;
            editingCyclic = false;
            animControlsPanel.setVisible(false);
            cyclicBasePose = null; cyclicOffsetA = null; cyclicOffsetB = null;
            cyclicAnimName = null; cyclicEndpoint = null;
            updateCyclicStatusLabel();

            currentArchetype = (String) archetypeCombo.getSelectedItem();
            switch (currentArchetype) {
                case "Passerine": skeleton = SkeletonGeometry.buildPasserine(); break;
                case "Raptor":    skeleton = SkeletonGeometry.buildRaptor(); break;
                case "Waterfowl": skeleton = SkeletonGeometry.buildWaterfowl(); break;
            }
            buildPoseButtons();
            SliderPanel.initGeometrySliders(this);
            JsonPoseIO.tryAutoLoadJson(this);
            if (editedPoses.isEmpty()) {
                loadSelectedPreset();
            }
            previewPanel.repaint();
            updateExportText();
        });

        resetBtn.addActionListener(e -> {
            batchUpdating = true;
            for (SliderPanel.JointSliderGroup g : sliderGroups.values()) {
                g.setValues(0, 0, 0);
            }
            for (SkeletonGeometry.Joint j : skeleton.allJoints) {
                j.resetGeometry();
            }
            for (var gEntry : sliderGroups.entrySet()) {
                SkeletonGeometry.Joint j = skeleton.jointMap.get(gEntry.getKey());
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

        resetToPresetBtn.addActionListener(e -> {
            if (currentPoseName == null || currentPoseName.equals("custom")) return;
            editedPoses.remove(currentPoseName);
            String pose = currentPoseName;
            currentPoseName = null;
            loadPresetByName(pose, true);
        });

        revertToSavedBtn.addActionListener(e -> {
            captureState();
            editedPoses.clear();
            JsonPoseIO.tryAutoLoadJson(this);
            buildPoseButtons();
            if (currentPoseName != null) {
                String pose = currentPoseName;
                currentPoseName = null;
                loadPresetByName(pose, true);
            } else {
                loadSelectedPreset();
            }
        });

        geometryToggle.addActionListener(e -> {
            showGeometry = geometryToggle.isSelected();
            for (SliderPanel.JointSliderGroup g : sliderGroups.values()) {
                if (g.geometryPanel != null) {
                    g.geometryPanel.setVisible(showGeometry);
                }
            }
            sliderPanel.revalidate();
            sliderPanel.repaint();
        });

        saveJsonBtn.addActionListener(e -> JsonPoseIO.saveToJson(this));
        loadJsonBtn.addActionListener(e -> JsonPoseIO.loadFromJson(this));

        // Build pose buttons and load default preset
        buildPoseButtons();
        JsonPoseIO.tryAutoLoadJson(this);
        if (editedPoses.isEmpty()) {
            loadSelectedPreset();
        }
    }

    // =========================================================================
    // Pose button management
    // =========================================================================

    void buildPoseButtons() {
        if (poseBtnPanel == null) return;
        poseBtnPanel.removeAll();
        poseButtons.clear();

        List<PosePresets.Preset> presets = allPresets.get(currentArchetype);
        if (presets == null) { poseBtnPanel.revalidate(); poseBtnPanel.repaint(); return; }

        Font btnFont = new Font("SansSerif", Font.PLAIN, 11);

        List<PosePresets.Preset> staticPresets = new ArrayList<>();
        List<PosePresets.Preset> cyclicPresetsList = new ArrayList<>();
        for (PosePresets.Preset p : presets) {
            if (p.isCyclic()) cyclicPresetsList.add(p);
            else staticPresets.add(p);
        }

        poseBtnPanel.setLayout(new BoxLayout(poseBtnPanel, BoxLayout.Y_AXIS));

        if (!staticPresets.isEmpty()) {
            JLabel lbl = new JLabel("Poses:");
            lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            lbl.setAlignmentX(0f);
            poseBtnPanel.add(lbl);
            for (PosePresets.Preset p : staticPresets) {
                JButton btn = makePresetButton(p, btnFont);
                btn.setAlignmentX(0f);
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                poseBtnPanel.add(btn);
                poseButtons.put(p.name, btn);
            }
        }

        if (!cyclicPresetsList.isEmpty()) {
            poseBtnPanel.add(Box.createVerticalStrut(4));
            JLabel lbl = new JLabel("Cyclic:");
            lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            lbl.setAlignmentX(0f);
            poseBtnPanel.add(lbl);
            Map<String, List<PosePresets.Preset>> groups = new LinkedHashMap<>();
            for (PosePresets.Preset p : cyclicPresetsList) {
                String groupName = p.cyclicName != null ? p.cyclicName : p.name;
                groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(p);
            }
            for (var entry : groups.entrySet()) {
                // Cyclic pairs still go side by side (they're related)
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1));
                row.setAlignmentX(0f);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                for (PosePresets.Preset p : entry.getValue()) {
                    JButton btn = makePresetButton(p, btnFont);
                    row.add(btn);
                    poseButtons.put(p.name, btn);
                }
                poseBtnPanel.add(row);
            }
        }

        poseBtnPanel.revalidate();
        poseBtnPanel.repaint();
        updateActivePoseButton();
    }

    JButton makePresetButton(PosePresets.Preset p, Font f) {
        String label = PosePresets.shortenPresetName(p.name);
        JButton btn = new JButton(label);
        btn.setFont(f);
        btn.setMargin(new Insets(1, 4, 1, 4));
        btn.setToolTipText(p.name);
        btn.addActionListener(e -> loadPresetByName(p.name));
        return btn;
    }

    void updateActivePoseButton() {
        for (var entry : poseButtons.entrySet()) {
            boolean active = entry.getKey().equals(currentPoseName);
            JButton btn = entry.getValue();
            String label = PosePresets.shortenPresetName(entry.getKey());
            btn.setText(active ? "\u25B8 " + label : label);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            if (active) {
                btn.setBackground(new Color(180, 210, 245));
            } else {
                btn.setBackground(null);
            }
        }
        if (poseBtnPanel != null) poseBtnPanel.repaint();
        updateHeading();
    }

    void updateHeading() {
        if (headingLabel != null) {
            String poseName = currentPoseName != null ? currentPoseName.replace('_', ' ') : "none";
            headingLabel.setText(currentArchetype + " — " + poseName);
        }
    }

    // =========================================================================
    // Preset loading
    // =========================================================================

    void loadPresetByName(String poseName) {
        loadPresetByName(poseName, false);
    }

    void loadPresetByName(String poseName, boolean forcePreset) {
        if (poseName == null) return;

        if (currentPoseName != null && !currentPoseName.equals("custom")) {
            editedPoses.put(currentPoseName, getCurrentPose());
        }

        currentPoseName = poseName;

        List<PosePresets.Preset> presets = allPresets.get(currentArchetype);
        PosePresets.Preset found = null;
        for (PosePresets.Preset p : presets) {
            if (p.name.equals(poseName)) { found = p; break; }
        }
        if (found == null) return;

        if (animTimer != null && animPlaying) {
            animPlaying = false;
            animTimer.stop();
            if (playPauseBtn != null) playPauseBtn.setText("Play");
        }

        if (found.isCyclic()) {
            editingCyclic = true;
            animControlsPanel.setVisible(true);
            cyclicBasePose = found.basePose;
            cyclicOffsetA = found.offsetA;
            cyclicOffsetB = found.offsetB;
            cyclicAnimName = found.cyclicName;
            boolean isA = PosePresets.isEndpointA(found);
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

        Map<String, float[]> saved = forcePreset ? null : editedPoses.get(poseName);

        batchUpdating = true;
        for (SliderPanel.JointSliderGroup g : sliderGroups.values()) {
            g.setValues(0, 0, 0);
        }
        for (SkeletonGeometry.Joint j : skeleton.allJoints) {
            j.resetGeometry();
        }
        for (var gEntry : sliderGroups.entrySet()) {
            SkeletonGeometry.Joint j = skeleton.jointMap.get(gEntry.getKey());
            if (j != null) {
                gEntry.getValue().setGeometryFromJoint(j);
            }
        }
        if (saved != null) {
            for (var entry : saved.entrySet()) {
                SliderPanel.JointSliderGroup g = sliderGroups.get(entry.getKey());
                if (g != null) {
                    float[] v = entry.getValue();
                    g.setValues(v[0], v[1], v[2]);
                }
            }
        } else {
            for (var entry : found.joints.entrySet()) {
                String name = entry.getKey();
                float[] v = entry.getValue();
                SliderPanel.JointSliderGroup g = sliderGroups.get(name);
                if (g != null) {
                    g.setValues(v[0], v[1], v[2]);
                }
            }
        }
        batchUpdating = false;
        previewPanel.repaint();
        updateExportText();
        captureState();
        updateActivePoseButton();
    }

    void loadSelectedPreset() {
        List<PosePresets.Preset> presets = allPresets.get(currentArchetype);
        if (presets != null && !presets.isEmpty()) {
            loadPresetByName(presets.get(0).name);
        }
    }

    // =========================================================================
    // Cyclic animation helpers
    // =========================================================================

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
            animElapsedTicks = 0f;
            playPauseBtn.setText("Stop");
            animTimer.start();
        } else {
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

    void updateCurrentEndpointFromSliders() {
        if (!editingCyclic || cyclicBasePose == null) return;
        Map<String, float[]> target = "A".equals(cyclicEndpoint) ? cyclicOffsetA :
                                      "B".equals(cyclicEndpoint) ? cyclicOffsetB : null;
        if (target == null) return;
        for (var entry : sliderGroups.entrySet()) {
            String name = entry.getKey();
            SliderPanel.JointSliderGroup g = entry.getValue();
            float[] baseV = cyclicBasePose.getOrDefault(name, new float[]{0f, 0f, 0f});
            target.put(name, new float[]{
                g.getX() - baseV[0],
                g.getY() - baseV[1],
                g.getZ() - baseV[2]
            });
        }
    }

    void applyPhase(float phase) {
        if (!editingCyclic || cyclicBasePose == null || cyclicOffsetA == null || cyclicOffsetB == null) return;

        Set<String> allJointNames = new LinkedHashSet<>();
        allJointNames.addAll(cyclicBasePose.keySet());
        allJointNames.addAll(cyclicOffsetA.keySet());
        allJointNames.addAll(cyclicOffsetB.keySet());

        batchUpdating = true;
        for (String name : allJointNames) {
            SliderPanel.JointSliderGroup g = sliderGroups.get(name);
            if (g == null) continue;
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

    // =========================================================================
    // Slider visibility
    // =========================================================================

    void updateSliderVisibility() {
        String sel = previewPanel.selectedJoint;

        if (rLegSection != null) {
            rLegSection.setVisible(!mirrorLegs);
        }

        if (sel == null) {
            for (JPanel s : allSections) {
                if (s == rLegSection) continue;
                s.setVisible(true);
            }
        } else {
            JPanel activeSection = jointToSection.get(sel);
            if (activeSection == rLegSection && mirrorLegs) {
                String lName = sel.startsWith("R_") ? "L_" + sel.substring(2) : sel;
                activeSection = jointToSection.get(lName);
            }
            for (JPanel s : allSections) {
                if (s == rLegSection) continue;
                s.setVisible(s == activeSection);
            }
            if (!mirrorLegs && rLegSection != null) {
                boolean isLeg = sel.contains("thigh") || sel.contains("shin") ||
                                sel.contains("tarsus") || sel.contains("foot");
                if (isLeg) rLegSection.setVisible(true);
            }
        }
        sliderPanel.revalidate();
        sliderPanel.repaint();
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
