import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * JSON read/write for pose data.
 * Extracted from PoseEditor during refactor — no logic changes.
 */
class JsonPoseIO {

    /**
     * Resolves the path to the JSON pose file for the given archetype.
     */
    static File resolveJsonFile(String archetype) {
        String archetypeLower = archetype.toLowerCase();
        File dir = new File("src/main/resources/assets/britishbirds/poses");
        if (!dir.exists()) {
            dir = new File("../src/main/resources/assets/britishbirds/poses");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, archetypeLower + "_poses.json");
    }

    /**
     * Saves all poses for the current archetype to a JSON file.
     */
    static void saveToJson(PoseEditor editor) {
        // Save current slider state first
        if (editor.currentPoseName != null && !editor.currentPoseName.equals("custom")) {
            editor.editedPoses.put(editor.currentPoseName, editor.getCurrentPose());
        }

        String archetype = editor.currentArchetype;
        List<PosePresets.Preset> presets = editor.allPresets.get(archetype);
        if (presets == null) return;

        List<PosePresets.Preset> staticPresets = new ArrayList<>();
        Map<String, List<PosePresets.Preset>> cyclicGroups = new LinkedHashMap<>();
        for (PosePresets.Preset p : presets) {
            if (p.isCyclic()) {
                cyclicGroups.computeIfAbsent(p.cyclicName, k -> new ArrayList<>()).add(p);
            } else {
                staticPresets.add(p);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"archetype\": \"").append(archetype.toLowerCase()).append("\",\n");

        sb.append("  \"static_poses\": {\n");
        for (int i = 0; i < staticPresets.size(); i++) {
            PosePresets.Preset p = staticPresets.get(i);
            Map<String, float[]> joints = editor.editedPoses.containsKey(p.name) ? editor.editedPoses.get(p.name) : p.joints;
            sb.append("    \"").append(p.name).append("\": {\n");
            sb.append("      \"joints\": {\n");
            writeJointsJson(sb, joints, "        ");
            sb.append("      },\n");
            sb.append("      \"spring_overrides\": {}\n");
            sb.append("    }");
            if (i < staticPresets.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");

        sb.append("  \"cyclic_animations\": {\n");
        int cyclicIdx = 0;
        int cyclicCount = cyclicGroups.size();
        for (var entry : cyclicGroups.entrySet()) {
            String cyclicName = entry.getKey();
            List<PosePresets.Preset> endpoints = entry.getValue();
            Map<String, float[]> offsetA = null;
            Map<String, float[]> offsetB = null;
            for (PosePresets.Preset ep : endpoints) {
                if (PosePresets.isEndpointA(ep)) {
                    offsetA = ep.offsetA;
                } else {
                    offsetB = ep.offsetB;
                }
            }
            if (offsetA == null && !endpoints.isEmpty()) offsetA = endpoints.get(0).offsetA;
            if (offsetB == null && !endpoints.isEmpty()) offsetB = endpoints.get(0).offsetB;

            sb.append("    \"").append(cyclicName).append("\": {\n");

            sb.append("      \"offset_a\": {\n");
            sb.append("        \"joints\": {\n");
            if (offsetA != null) writeJointsJson(sb, filterLeftOnly(offsetA), "          ");
            sb.append("        }\n");
            sb.append("      },\n");

            sb.append("      \"offset_b\": {\n");
            sb.append("        \"joints\": {\n");
            if (offsetB != null) writeJointsJson(sb, filterLeftOnly(offsetB), "          ");
            sb.append("        }\n");
            sb.append("      }\n");

            sb.append("    }");
            cyclicIdx++;
            if (cyclicIdx < cyclicCount) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");

        sb.append("  \"overlay_poses\": {}\n");

        sb.append("}\n");

        File outFile = resolveJsonFile(archetype);
        try (FileWriter fw = new FileWriter(outFile)) {
            fw.write(sb.toString());
            JOptionPane.showMessageDialog(editor,
                    "Saved JSON to: " + outFile.getAbsolutePath(),
                    "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(editor,
                    "Error writing JSON: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static Map<String, float[]> filterLeftOnly(Map<String, float[]> joints) {
        Map<String, float[]> filtered = new LinkedHashMap<>();
        for (var entry : joints.entrySet()) {
            String name = entry.getKey();
            if (!name.startsWith("R_")) {
                filtered.put(name, entry.getValue());
            }
        }
        return filtered;
    }

    static void writeJointsJson(StringBuilder sb, Map<String, float[]> joints, String indent) {
        List<Map.Entry<String, float[]>> entries = new ArrayList<>();
        for (var entry : joints.entrySet()) {
            if (!entry.getKey().startsWith("R_")) {
                entries.add(entry);
            }
        }
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            float[] v = entry.getValue();
            sb.append(indent).append("\"").append(entry.getKey()).append("\": [")
                    .append(formatJsonFloat(v[0])).append(", ")
                    .append(formatJsonFloat(v[1])).append(", ")
                    .append(formatJsonFloat(v[2])).append("]");
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
    }

    static String formatJsonFloat(float v) {
        if (v == 0f) return "0.0";
        String s = String.format("%.4f", v);
        s = s.replaceAll("0+$", "");
        if (s.endsWith(".")) s += "0";
        return s;
    }

    /**
     * Loads poses from a JSON file (manual file chooser load).
     */
    static void loadFromJson(PoseEditor editor) {
        File jsonFile = resolveJsonFile(editor.currentArchetype);
        if (!jsonFile.exists()) {
            JOptionPane.showMessageDialog(editor,
                    "No JSON file found at: " + jsonFile.getAbsolutePath(),
                    "Load Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadFromJsonFile(editor, jsonFile);
    }

    static void loadFromJsonFile(PoseEditor editor, File jsonFile) { loadFromJsonFile(editor, jsonFile, false); }
    static void loadFromJsonFile(PoseEditor editor, File jsonFile, boolean silent) {
        if (!jsonFile.exists()) return;

        try {
            String content = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Map<String, float[]>> loadedStatic = new LinkedHashMap<>();
            parseJsonStaticPoses(content, loadedStatic);

            List<PosePresets.Preset> presets = editor.allPresets.get(editor.currentArchetype);
            if (presets != null) {
                for (PosePresets.Preset p : presets) {
                    if (!p.isCyclic() && loadedStatic.containsKey(p.name)) {
                        Map<String, float[]> loadedJoints = loadedStatic.get(p.name);
                        PosePresets.mirror(loadedJoints);
                        p.joints.clear();
                        p.joints.putAll(loadedJoints);
                    }
                }
            }

            editor.editedPoses.clear();
            editor.buildPoseButtons();
            editor.loadSelectedPreset();

            if (!silent) {
                JOptionPane.showMessageDialog(editor,
                        "Loaded poses from: " + jsonFile.getAbsolutePath(),
                        "Load Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            if (!silent) {
                JOptionPane.showMessageDialog(editor,
                        "Error reading JSON: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static void tryAutoLoadJson(PoseEditor editor) {
        File jsonFile = resolveJsonFile(editor.currentArchetype);
        if (jsonFile.exists()) {
            loadFromJsonFile(editor, jsonFile, true);
        }
    }

    // =========================================================================
    // Minimal JSON parsing
    // =========================================================================

    static void parseJsonStaticPoses(String json, Map<String, Map<String, float[]>> result) {
        int idx = json.indexOf("\"static_poses\"");
        if (idx < 0) return;

        idx = json.indexOf('{', idx + 14);
        if (idx < 0) return;

        int braceDepth = 1;
        int pos = idx + 1;

        while (pos < json.length() && braceDepth > 0) {
            if (braceDepth == 1) {
                int nameStart = json.indexOf('"', pos);
                if (nameStart < 0 || nameStart >= json.length()) break;
                int nameEnd = json.indexOf('"', nameStart + 1);
                if (nameEnd < 0) break;
                String poseName = json.substring(nameStart + 1, nameEnd);

                int jointsIdx = json.indexOf("\"joints\"", nameEnd);
                if (jointsIdx < 0) break;
                int jointsStart = json.indexOf('{', jointsIdx + 8);
                if (jointsStart < 0) break;

                int jointsEnd = findMatchingBrace(json, jointsStart);
                if (jointsEnd < 0) break;
                String jointsStr = json.substring(jointsStart + 1, jointsEnd);
                Map<String, float[]> joints = parseJointEntries(jointsStr);
                result.put(poseName, joints);

                int poseObjStart = json.indexOf('{', nameEnd);
                if (poseObjStart < 0) break;
                int poseObjEnd = findMatchingBrace(json, poseObjStart);
                if (poseObjEnd < 0) break;
                pos = poseObjEnd + 1;
            } else {
                pos++;
            }

            while (pos < json.length() && (json.charAt(pos) == ' ' || json.charAt(pos) == '\n' ||
                    json.charAt(pos) == '\r' || json.charAt(pos) == '\t' || json.charAt(pos) == ',')) {
                pos++;
            }
            if (pos < json.length() && json.charAt(pos) == '}') {
                break;
            }
        }
    }

    static int findMatchingBrace(String s, int openPos) {
        int depth = 1;
        for (int i = openPos + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    static Map<String, float[]> parseJointEntries(String jointsStr) {
        Map<String, float[]> result = new LinkedHashMap<>();
        int pos = 0;
        while (pos < jointsStr.length()) {
            int nameStart = jointsStr.indexOf('"', pos);
            if (nameStart < 0) break;
            int nameEnd = jointsStr.indexOf('"', nameStart + 1);
            if (nameEnd < 0) break;
            String name = jointsStr.substring(nameStart + 1, nameEnd);

            int arrStart = jointsStr.indexOf('[', nameEnd);
            if (arrStart < 0) break;
            int arrEnd = jointsStr.indexOf(']', arrStart);
            if (arrEnd < 0) break;
            String arrStr = jointsStr.substring(arrStart + 1, arrEnd);
            String[] parts = arrStr.split(",");
            if (parts.length >= 3) {
                float x = Float.parseFloat(parts[0].trim());
                float y = Float.parseFloat(parts[1].trim());
                float z = Float.parseFloat(parts[2].trim());
                result.put(name, new float[]{x, y, z});
            }
            pos = arrEnd + 1;
        }
        return result;
    }
}
