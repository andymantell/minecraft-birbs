import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Slider UI: JointSliderGroup and slider panel construction.
 * Extracted from PoseEditor during refactor — no logic changes.
 */
class SliderPanel {

    // =========================================================================
    // JointSliderGroup
    // =========================================================================

    /** One joint's 3 rotation sliders + 6 geometry sliders + text fields. */
    static class JointSliderGroup {
        final String jointName;
        final JSlider xSlider, ySlider, zSlider;
        final JTextField xField, yField, zField;

        // Geometry sliders: offset X/Y/Z and size W/H/D
        final JSlider offXSlider, offYSlider, offZSlider;
        final JTextField offXField, offYField, offZField;
        final JSlider sizeWSlider, sizeHSlider, sizeDSlider;
        final JTextField sizeWField, sizeHField, sizeDField;
        JPanel geometryPanel;

        final PoseEditor editor;

        JointSliderGroup(String jointName, PoseEditor editor) {
            this.jointName = jointName;
            this.editor = editor;
            xSlider = makeRotSlider();
            ySlider = makeRotSlider();
            zSlider = makeRotSlider();
            xField = makeField("0.00");
            yField = makeField("0.00");
            zField = makeField("0.00");

            linkRotSliderAndField(xSlider, xField);
            linkRotSliderAndField(ySlider, yField);
            linkRotSliderAndField(zSlider, zField);

            offXSlider = makeGeomSlider(-100, 100, 0);
            offYSlider = makeGeomSlider(-100, 100, 0);
            offZSlider = makeGeomSlider(-100, 100, 0);
            offXField = makeField("0.0");
            offYField = makeField("0.0");
            offZField = makeField("0.0");

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
            slider.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!editor.batchUpdating) {
                        editor.captureState();
                        if (editor.editingCyclic && editor.animPlaying) {
                            editor.animPlaying = false;
                            editor.animTimer.stop();
                            if (editor.playPauseBtn != null) editor.playPauseBtn.setText("Play");
                            float snapPhase = editor.animPhase < 0.5f ? 0f : 1f;
                            editor.cyclicEndpoint = snapPhase == 0f ? "A" : "B";
                            editor.animPhase = snapPhase;
                            editor.batchUpdating = true;
                            editor.phaseSlider.setValue(Math.round(snapPhase * 100));
                            editor.batchUpdating = false;
                            editor.applyPhase(snapPhase);
                        }
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!editor.batchUpdating && editor.editingCyclic) editor.updateCurrentEndpointFromSliders();
                }
            });
            slider.addChangeListener(e -> {
                if (!editor.batchUpdating) {
                    float val = slider.getValue() / 100f;
                    field.setText(String.format("%.2f", val));
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(-(float)Math.PI, Math.min((float)Math.PI, val));
                    editor.batchUpdating = true;
                    slider.setValue(Math.round(val * 100));
                    editor.batchUpdating = false;
                    field.setText(String.format("%.2f", val));
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                    editor.captureState();
                    if (editor.editingCyclic) editor.updateCurrentEndpointFromSliders();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        void linkOffsetSliderAndField(JSlider slider, JTextField field) {
            slider.addChangeListener(e -> {
                if (!editor.batchUpdating) {
                    float val = slider.getValue() / 10f;
                    field.setText(String.format("%.1f", val));
                    applyGeometryToJoint();
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(-10f, Math.min(10f, val));
                    editor.batchUpdating = true;
                    slider.setValue(Math.round(val * 10));
                    editor.batchUpdating = false;
                    field.setText(String.format("%.1f", val));
                    applyGeometryToJoint();
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        void linkSizeSliderAndField(JSlider slider, JTextField field) {
            slider.addChangeListener(e -> {
                if (!editor.batchUpdating) {
                    float val = slider.getValue() * 0.25f;
                    field.setText(String.format("%.2f", val));
                    applyGeometryToJoint();
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                }
            });
            field.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(field.getText().trim());
                    val = Math.max(0.5f, Math.min(10f, val));
                    editor.batchUpdating = true;
                    slider.setValue(Math.round(val / 0.25f));
                    editor.batchUpdating = false;
                    field.setText(String.format("%.2f", val));
                    applyGeometryToJoint();
                    editor.previewPanel.repaint();
                    editor.updateExportText();
                } catch (NumberFormatException ex) {
                    // ignore bad input
                }
            });
        }

        void applyGeometryToJoint() {
            SkeletonGeometry.Joint j = editor.skeleton.jointMap.get(jointName);
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

        void setGeometryFromJoint(SkeletonGeometry.Joint j) {
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

            c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
            JLabel nameLabel = new JLabel(jointName);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            p.add(nameLabel, c);
            c.gridwidth = 1;

            addRow(p, c, 1, "xRot", xSlider, xField, false);
            addRow(p, c, 2, "yRot", ySlider, yField, false);
            addRow(p, c, 3, "zRot", zSlider, zField, false);

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

            geometryPanel.setVisible(editor.showGeometry);

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
    // Static helper methods for building the slider panel
    // =========================================================================

    static void buildSliderPanel(PoseEditor editor) {
        editor.sliderPanel.removeAll();
        editor.sliderGroups.clear();

        addSliderSection(editor, "Spine", SkeletonGeometry.SPINE_JOINTS, SkeletonGeometry.SPINE_BLUE);
        addSliderSection(editor, "Neck + Head", SkeletonGeometry.NECK_HEAD_JOINTS, SkeletonGeometry.NECK_PINK);
        addSliderSection(editor, "Left Wing (R auto-mirrors)", SkeletonGeometry.LEFT_WING_JOINTS, SkeletonGeometry.WING_GREEN);
        addSliderSection(editor, "Tail", SkeletonGeometry.TAIL_JOINTS, SkeletonGeometry.TAIL_YELLOW);
        addSliderSection(editor, "Left Leg", SkeletonGeometry.LEFT_LEG_JOINTS, SkeletonGeometry.LEG_ORANGE);

        // Mirror legs checkbox + R leg section
        JCheckBox mirrorLegsBox = new JCheckBox("Mirror Legs", editor.mirrorLegs);
        mirrorLegsBox.setAlignmentX(0f);
        mirrorLegsBox.setFont(new Font("SansSerif", Font.PLAIN, 11));
        mirrorLegsBox.addActionListener(e -> {
            editor.captureState();
            editor.mirrorLegs = mirrorLegsBox.isSelected();
            editor.batchUpdating = true;
            if (!editor.mirrorLegs) {
                for (int i = 0; i < SkeletonGeometry.LEFT_LEG_JOINTS.length; i++) {
                    JointSliderGroup lGrp = editor.sliderGroups.get(SkeletonGeometry.LEFT_LEG_JOINTS[i]);
                    JointSliderGroup rGrp = editor.sliderGroups.get(SkeletonGeometry.RIGHT_LEG_JOINTS[i]);
                    if (lGrp != null && rGrp != null) {
                        rGrp.setValues(lGrp.getX(), -lGrp.getY(), lGrp.getZ());
                    }
                }
            } else {
                for (int i = 0; i < SkeletonGeometry.LEFT_LEG_JOINTS.length; i++) {
                    JointSliderGroup lGrp = editor.sliderGroups.get(SkeletonGeometry.LEFT_LEG_JOINTS[i]);
                    JointSliderGroup rGrp = editor.sliderGroups.get(SkeletonGeometry.RIGHT_LEG_JOINTS[i]);
                    if (lGrp != null && rGrp != null) {
                        rGrp.setValues(lGrp.getX(), -lGrp.getY(), lGrp.getZ());
                    }
                }
            }
            editor.batchUpdating = false;
            Map<String, float[]> pose = editor.getCurrentPose();
            SkeletonGeometry.applyPose(editor.skeleton, pose);
            SkeletonGeometry.computeFK(editor.skeleton);
            editor.updateSliderVisibility();
            editor.previewPanel.repaint();
            editor.updateExportText();
        });
        editor.sliderPanel.add(mirrorLegsBox);

        addSliderSection(editor, "Right Leg", SkeletonGeometry.RIGHT_LEG_JOINTS, SkeletonGeometry.LEG_ORANGE);
        editor.rLegSection = editor.allSections.get(editor.allSections.size() - 1);
        editor.rLegSection.setVisible(!editor.mirrorLegs);

        initGeometrySliders(editor);

        editor.sliderPanel.add(Box.createVerticalGlue());
        editor.sliderPanel.revalidate();
    }

    static void initGeometrySliders(PoseEditor editor) {
        editor.batchUpdating = true;
        for (var entry : editor.sliderGroups.entrySet()) {
            SkeletonGeometry.Joint j = editor.skeleton.jointMap.get(entry.getKey());
            if (j != null) {
                entry.getValue().setGeometryFromJoint(j);
            }
        }
        editor.batchUpdating = false;
    }

    static void addSliderSection(PoseEditor editor, String title, String[] jointNames, Color colour) {
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

        JButton copyFromBtn = new JButton("\uD83D\uDCCB Copy from...");
        copyFromBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        copyFromBtn.setMargin(new Insets(1, 4, 1, 4));
        copyFromBtn.setAlignmentX(0f);
        copyFromBtn.addActionListener(e -> showCopyFromMenu(editor, copyFromBtn, jointNames));
        JPanel copyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        copyRow.setAlignmentX(0f);
        copyRow.add(copyFromBtn);
        section.add(copyRow);
        section.add(Box.createVerticalStrut(2));

        for (String name : jointNames) {
            JointSliderGroup group = new JointSliderGroup(name, editor);
            editor.sliderGroups.put(name, group);
            section.add(group.buildPanel());
            section.add(Box.createVerticalStrut(2));
            editor.jointToSection.put(name, section);
            if (name.startsWith("L_")) {
                editor.jointToSection.put("R_" + name.substring(2), section);
            }
        }

        section.setAlignmentX(0f);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height + 200));
        editor.allSections.add(section);
        editor.sliderPanel.add(section);
        editor.sliderPanel.add(Box.createVerticalStrut(4));
    }

    static void showCopyFromMenu(PoseEditor editor, JButton anchor, String[] sectionJointNames) {
        List<PosePresets.Preset> presets = editor.allPresets.get(editor.currentArchetype);
        if (presets == null || presets.isEmpty()) return;

        JPopupMenu menu = new JPopupMenu("Copy from...");
        for (PosePresets.Preset p : presets) {
            JMenuItem item = new JMenuItem(p.name);
            item.addActionListener(ev -> copyJointsFromPreset(editor, p, sectionJointNames));
            menu.add(item);
        }
        menu.show(anchor, 0, anchor.getHeight());
    }

    static void copyJointsFromPreset(PoseEditor editor, PosePresets.Preset preset, String[] sectionJointNames) {
        editor.captureState();
        editor.batchUpdating = true;
        for (String name : sectionJointNames) {
            float[] v = preset.joints.get(name);
            JointSliderGroup g = editor.sliderGroups.get(name);
            if (g != null) {
                if (v != null) {
                    g.setValues(v[0], v[1], v[2]);
                } else {
                    g.setValues(0f, 0f, 0f);
                }
            }
        }
        editor.batchUpdating = false;
        editor.previewPanel.repaint();
        editor.updateExportText();
        if (editor.editingCyclic) editor.updateCurrentEndpointFromSliders();
    }
}
