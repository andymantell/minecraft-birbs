import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates 512x512 bird textures for the universal 32-cuboid skeleton layout.
 *
 * Uses a "virtual body painting" approach: defines a continuous colour field
 * for the whole bird body, then samples per-cuboid-face UV regions from it
 * to ensure seam colour continuity between adjacent cuboids.
 *
 * Minecraft cuboid UV mapping: for a box at texOffs(u,v) with dimensions (w,h,d):
 *   Top face:    (u+d,     v,       w, d)
 *   Bottom face: (u+d+w,   v,       w, d)
 *   Left face:   (u,       v+d,     d, h)
 *   Front face:  (u+d,     v+d,     w, h)
 *   Right face:  (u+d+w,   v+d,     d, h)
 *   Back face:   (u+2d+w,  v+d,     w, h)
 */
public class TextureGenerator {

    static final int SIZE = 512;

    // =========================================================================
    // Joint name constants (mirror BirdSkeleton)
    // =========================================================================
    static final String CHEST          = "chest";
    static final String SHOULDER_MOUNT = "shoulder_mount";
    static final String TORSO          = "torso";
    static final String HIP            = "hip";
    static final String NECK_LOWER     = "neck_lower";
    static final String NECK_MID       = "neck_mid";
    static final String NECK_UPPER     = "neck_upper";
    static final String HEAD           = "head";
    static final String UPPER_BEAK     = "upper_beak";
    static final String LOWER_BEAK     = "lower_beak";
    static final String L_UPPER_WING   = "L_upper_wing";
    static final String L_SCAPULARS    = "L_scapulars";
    static final String L_FOREARM      = "L_forearm";
    static final String L_SECONDARIES  = "L_secondaries";
    static final String L_HAND         = "L_hand";
    static final String L_PRIMARIES    = "L_primaries";
    static final String R_UPPER_WING   = "R_upper_wing";
    static final String R_SCAPULARS    = "R_scapulars";
    static final String R_FOREARM      = "R_forearm";
    static final String R_SECONDARIES  = "R_secondaries";
    static final String R_HAND         = "R_hand";
    static final String R_PRIMARIES    = "R_primaries";
    static final String TAIL_BASE      = "tail_base";
    static final String TAIL_FAN       = "tail_fan";
    static final String L_THIGH        = "L_thigh";
    static final String L_SHIN         = "L_shin";
    static final String L_TARSUS       = "L_tarsus";
    static final String L_FOOT         = "L_foot";
    static final String R_THIGH        = "R_thigh";
    static final String R_SHIN         = "R_shin";
    static final String R_TARSUS       = "R_tarsus";
    static final String R_FOOT         = "R_foot";

    // =========================================================================
    // UV layout computation (mirrors BirdUVLayout logic, standalone for tool)
    // =========================================================================

    /**
     * Row-packing algorithm: pack cuboid UV faces left-to-right, top-to-bottom.
     * Returns map of joint name -> {u, v} texture offset.
     */
    static Map<String, int[]> computeLayout(Map<String, int[]> cuboidDimensions) {
        Map<String, int[]> offsets = new LinkedHashMap<>();
        int cursorX = 0;
        int cursorY = 0;
        int rowHeight = 0;

        for (Map.Entry<String, int[]> entry : cuboidDimensions.entrySet()) {
            String name = entry.getKey();
            int[] dims = entry.getValue();
            int w = dims[0], h = dims[1], d = dims[2];
            int uvWidth = 2 * (d + w);
            int uvHeight = d + h;

            if (cursorX + uvWidth > SIZE) {
                cursorX = 0;
                cursorY += rowHeight;
                rowHeight = 0;
            }

            offsets.put(name, new int[]{cursorX, cursorY});
            cursorX += uvWidth;
            rowHeight = Math.max(rowHeight, uvHeight);
        }
        return offsets;
    }

    static Map<String, int[]> defaultPasserineDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();
        dims.put(CHEST,          new int[]{3, 3, 4});
        dims.put(SHOULDER_MOUNT, new int[]{2, 2, 2});
        dims.put(TORSO,          new int[]{3, 3, 4});
        dims.put(HIP,            new int[]{2, 2, 3});
        dims.put(NECK_LOWER,     new int[]{2, 1, 1});
        dims.put(NECK_MID,       new int[]{2, 1, 1});
        dims.put(NECK_UPPER,     new int[]{2, 1, 1});
        dims.put(HEAD,           new int[]{4, 4, 4});
        dims.put(UPPER_BEAK,     new int[]{1, 1, 2});
        dims.put(LOWER_BEAK,     new int[]{1, 1, 2});
        dims.put(L_UPPER_WING,   new int[]{4, 1, 4});
        dims.put(L_SCAPULARS,    new int[]{3, 1, 3});
        dims.put(L_FOREARM,      new int[]{3, 1, 3});
        dims.put(L_SECONDARIES,  new int[]{3, 1, 3});
        dims.put(L_HAND,         new int[]{3, 1, 2});
        dims.put(L_PRIMARIES,    new int[]{3, 1, 2});
        dims.put(R_UPPER_WING,   new int[]{4, 1, 4});
        dims.put(R_SCAPULARS,    new int[]{3, 1, 3});
        dims.put(R_FOREARM,      new int[]{3, 1, 3});
        dims.put(R_SECONDARIES,  new int[]{3, 1, 3});
        dims.put(R_HAND,         new int[]{3, 1, 2});
        dims.put(R_PRIMARIES,    new int[]{3, 1, 2});
        dims.put(TAIL_BASE,      new int[]{2, 1, 2});
        dims.put(TAIL_FAN,       new int[]{2, 1, 4});
        dims.put(L_THIGH,        new int[]{1, 1, 1});
        dims.put(L_SHIN,         new int[]{1, 2, 1});
        dims.put(L_TARSUS,       new int[]{1, 2, 1});
        dims.put(L_FOOT,         new int[]{2, 1, 2});
        dims.put(R_THIGH,        new int[]{1, 1, 1});
        dims.put(R_SHIN,         new int[]{1, 2, 1});
        dims.put(R_TARSUS,       new int[]{1, 2, 1});
        dims.put(R_FOOT,         new int[]{2, 1, 2});
        return dims;
    }

    // Barn owl / peregrine / mallard need larger dimensions for some parts
    static Map<String, int[]> barnOwlDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();
        dims.put(CHEST,          new int[]{5, 5, 6});
        dims.put(SHOULDER_MOUNT, new int[]{3, 3, 3});
        dims.put(TORSO,          new int[]{4, 4, 5});
        dims.put(HIP,            new int[]{4, 3, 5});
        dims.put(NECK_LOWER,     new int[]{2, 2, 1});
        dims.put(NECK_MID,       new int[]{2, 2, 1});
        dims.put(NECK_UPPER,     new int[]{2, 2, 1});
        dims.put(HEAD,           new int[]{5, 5, 5});
        dims.put(UPPER_BEAK,     new int[]{1, 1, 1});
        dims.put(LOWER_BEAK,     new int[]{1, 1, 1});
        dims.put(L_UPPER_WING,   new int[]{6, 2, 6});
        dims.put(L_SCAPULARS,    new int[]{5, 2, 4});
        dims.put(L_FOREARM,      new int[]{5, 2, 5});
        dims.put(L_SECONDARIES,  new int[]{5, 2, 4});
        dims.put(L_HAND,         new int[]{5, 1, 3});
        dims.put(L_PRIMARIES,    new int[]{5, 1, 3});
        dims.put(R_UPPER_WING,   new int[]{6, 2, 6});
        dims.put(R_SCAPULARS,    new int[]{5, 2, 4});
        dims.put(R_FOREARM,      new int[]{5, 2, 5});
        dims.put(R_SECONDARIES,  new int[]{5, 2, 4});
        dims.put(R_HAND,         new int[]{5, 1, 3});
        dims.put(R_PRIMARIES,    new int[]{5, 1, 3});
        dims.put(TAIL_BASE,      new int[]{3, 1, 3});
        dims.put(TAIL_FAN,       new int[]{4, 1, 4});
        dims.put(L_THIGH,        new int[]{1, 1, 1});
        dims.put(L_SHIN,         new int[]{1, 3, 1});
        dims.put(L_TARSUS,       new int[]{1, 2, 1});
        dims.put(L_FOOT,         new int[]{2, 1, 2});
        dims.put(R_THIGH,        new int[]{1, 1, 1});
        dims.put(R_SHIN,         new int[]{1, 3, 1});
        dims.put(R_TARSUS,       new int[]{1, 2, 1});
        dims.put(R_FOOT,         new int[]{2, 1, 2});
        return dims;
    }

    static Map<String, int[]> peregrineDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();
        dims.put(CHEST,          new int[]{6, 5, 6});
        dims.put(SHOULDER_MOUNT, new int[]{3, 3, 3});
        dims.put(TORSO,          new int[]{4, 4, 6});
        dims.put(HIP,            new int[]{3, 3, 4});
        dims.put(NECK_LOWER,     new int[]{2, 2, 1});
        dims.put(NECK_MID,       new int[]{2, 2, 1});
        dims.put(NECK_UPPER,     new int[]{2, 2, 1});
        dims.put(HEAD,           new int[]{3, 3, 3});
        dims.put(UPPER_BEAK,     new int[]{1, 1, 2});
        dims.put(LOWER_BEAK,     new int[]{1, 1, 2});
        dims.put(L_UPPER_WING,   new int[]{5, 1, 5});
        dims.put(L_SCAPULARS,    new int[]{4, 1, 3});
        dims.put(L_FOREARM,      new int[]{4, 1, 4});
        dims.put(L_SECONDARIES,  new int[]{4, 1, 3});
        dims.put(L_HAND,         new int[]{5, 1, 3});
        dims.put(L_PRIMARIES,    new int[]{5, 1, 2});
        dims.put(R_UPPER_WING,   new int[]{5, 1, 5});
        dims.put(R_SCAPULARS,    new int[]{4, 1, 3});
        dims.put(R_FOREARM,      new int[]{4, 1, 4});
        dims.put(R_SECONDARIES,  new int[]{4, 1, 3});
        dims.put(R_HAND,         new int[]{5, 1, 3});
        dims.put(R_PRIMARIES,    new int[]{5, 1, 2});
        dims.put(TAIL_BASE,      new int[]{3, 1, 4});
        dims.put(TAIL_FAN,       new int[]{2, 1, 4});
        dims.put(L_THIGH,        new int[]{1, 2, 1});
        dims.put(L_SHIN,         new int[]{1, 3, 1});
        dims.put(L_TARSUS,       new int[]{1, 2, 1});
        dims.put(L_FOOT,         new int[]{2, 1, 2});
        dims.put(R_THIGH,        new int[]{1, 2, 1});
        dims.put(R_SHIN,         new int[]{1, 3, 1});
        dims.put(R_TARSUS,       new int[]{1, 2, 1});
        dims.put(R_FOOT,         new int[]{2, 1, 2});
        return dims;
    }

    static Map<String, int[]> mallardDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();
        dims.put(CHEST,          new int[]{5, 5, 6});
        dims.put(SHOULDER_MOUNT, new int[]{3, 3, 3});
        dims.put(TORSO,          new int[]{5, 5, 6});
        dims.put(HIP,            new int[]{3, 3, 4});
        dims.put(NECK_LOWER,     new int[]{3, 2, 2});
        dims.put(NECK_MID,       new int[]{3, 2, 2});
        dims.put(NECK_UPPER,     new int[]{3, 2, 2});
        dims.put(HEAD,           new int[]{4, 4, 4});
        dims.put(UPPER_BEAK,     new int[]{3, 1, 3});
        dims.put(LOWER_BEAK,     new int[]{3, 1, 3});
        dims.put(L_UPPER_WING,   new int[]{5, 1, 6});
        dims.put(L_SCAPULARS,    new int[]{4, 1, 4});
        dims.put(L_FOREARM,      new int[]{4, 1, 5});
        dims.put(L_SECONDARIES,  new int[]{4, 1, 4});
        dims.put(L_HAND,         new int[]{4, 1, 3});
        dims.put(L_PRIMARIES,    new int[]{4, 1, 2});
        dims.put(R_UPPER_WING,   new int[]{5, 1, 6});
        dims.put(R_SCAPULARS,    new int[]{4, 1, 4});
        dims.put(R_FOREARM,      new int[]{4, 1, 5});
        dims.put(R_SECONDARIES,  new int[]{4, 1, 4});
        dims.put(R_HAND,         new int[]{4, 1, 3});
        dims.put(R_PRIMARIES,    new int[]{4, 1, 2});
        dims.put(TAIL_BASE,      new int[]{4, 1, 4});
        dims.put(TAIL_FAN,       new int[]{3, 1, 4});
        dims.put(L_THIGH,        new int[]{1, 1, 1});
        dims.put(L_SHIN,         new int[]{1, 2, 1});
        dims.put(L_TARSUS,       new int[]{1, 2, 1});
        dims.put(L_FOOT,         new int[]{3, 1, 3});
        dims.put(R_THIGH,        new int[]{1, 1, 1});
        dims.put(R_SHIN,         new int[]{1, 2, 1});
        dims.put(R_TARSUS,       new int[]{1, 2, 1});
        dims.put(R_FOOT,         new int[]{3, 1, 3});
        return dims;
    }

    // =========================================================================
    // Body region classification for virtual body painting
    // =========================================================================

    /** Which body region a cuboid belongs to, for colour lookup. */
    enum BodyRegion {
        BACK, BELLY, HEAD_TOP, HEAD_SIDE, THROAT, WING, WING_TIP, TAIL, LEG, FOOT, BEAK, NECK, RUMP
    }

    static BodyRegion getBodyRegion(String joint) {
        return switch (joint) {
            case CHEST, TORSO -> BodyRegion.BACK;  // top=back, front=belly, sides=flanks
            case SHOULDER_MOUNT -> BodyRegion.BACK;
            case HIP -> BodyRegion.RUMP;
            case NECK_LOWER, NECK_MID, NECK_UPPER -> BodyRegion.NECK;
            case HEAD -> BodyRegion.HEAD_TOP;
            case UPPER_BEAK, LOWER_BEAK -> BodyRegion.BEAK;
            case L_UPPER_WING, L_SCAPULARS, R_UPPER_WING, R_SCAPULARS -> BodyRegion.WING;
            case L_FOREARM, L_SECONDARIES, R_FOREARM, R_SECONDARIES -> BodyRegion.WING;
            case L_HAND, L_PRIMARIES, R_HAND, R_PRIMARIES -> BodyRegion.WING_TIP;
            case TAIL_BASE, TAIL_FAN -> BodyRegion.TAIL;
            case L_THIGH, L_SHIN, L_TARSUS, R_THIGH, R_SHIN, R_TARSUS -> BodyRegion.LEG;
            case L_FOOT, R_FOOT -> BodyRegion.FOOT;
            default -> BodyRegion.BACK;
        };
    }

    // =========================================================================
    // Species colour palettes — defines continuous body colour for each species
    // =========================================================================

    /**
     * A species palette maps body region + face direction to a colour.
     * Face directions: top, bottom, front, back, left, right.
     */
    static class BodyPalette {
        Color back, belly, flanks, headTop, headSide, headFront, throat;
        Color neckBack, neckFront;
        Color wingUpper, wingLower, wingTip;
        Color tail, tailUnder, rump;
        Color leg, foot, beak;
        Color eye;

        // Extra colours for species-specific detail
        Color accent1, accent2, accent3;

        Color getTop(BodyRegion region)    { return getForFace(region, "top"); }
        Color getBottom(BodyRegion region) { return getForFace(region, "bottom"); }
        Color getFront(BodyRegion region)  { return getForFace(region, "front"); }
        Color getBack(BodyRegion region)   { return getForFace(region, "back"); }
        Color getLeft(BodyRegion region)   { return getForFace(region, "left"); }
        Color getRight(BodyRegion region)  { return getForFace(region, "right"); }

        Color getForFace(BodyRegion region, String face) {
            return switch (region) {
                case BACK -> switch (face) {
                    case "top" -> back;
                    case "bottom" -> belly;
                    case "front" -> belly;
                    case "back" -> back;
                    default -> flanks;
                };
                case BELLY -> belly;
                case RUMP -> switch (face) {
                    case "top" -> rump != null ? rump : back;
                    case "bottom" -> belly;
                    default -> flanks;
                };
                case NECK -> switch (face) {
                    case "top", "back" -> neckBack != null ? neckBack : back;
                    case "front", "bottom" -> neckFront != null ? neckFront : belly;
                    default -> flanks;
                };
                case HEAD_TOP -> switch (face) {
                    case "top" -> headTop;
                    case "bottom" -> throat;
                    case "front" -> headFront != null ? headFront : headTop;
                    case "back" -> headTop;
                    default -> headSide;
                };
                case HEAD_SIDE -> headSide;
                case THROAT -> throat;
                case WING -> switch (face) {
                    case "top" -> wingUpper;
                    case "bottom" -> wingLower;
                    default -> wingUpper;
                };
                case WING_TIP -> switch (face) {
                    case "top" -> wingTip != null ? wingTip : wingUpper;
                    case "bottom" -> wingLower;
                    default -> wingTip != null ? wingTip : wingUpper;
                };
                case TAIL -> switch (face) {
                    case "bottom" -> tailUnder != null ? tailUnder : tail;
                    default -> tail;
                };
                case LEG -> leg;
                case FOOT -> foot;
                case BEAK -> beak;
            };
        }
    }

    // =========================================================================
    // Main entry point
    // =========================================================================

    public static void main(String[] args) throws Exception {
        String basePath = "src/main/resources/assets/britishbirds/textures/entity";

        generateRobin(basePath + "/robin/robin.png");
        generateRobinJuvenile(basePath + "/robin/robin_baby.png");
        generateBlueTit(basePath + "/blue_tit/blue_tit.png");
        generateBarnOwlMale(basePath + "/barn_owl/barn_owl_male.png");
        generateBarnOwlFemale(basePath + "/barn_owl/barn_owl_female.png");
        generatePeregrineAdult(basePath + "/peregrine_falcon/peregrine_adult.png");
        generatePeregrineJuvenile(basePath + "/peregrine_falcon/peregrine_juvenile.png");
        generateMallardMale(basePath + "/mallard/mallard_male.png");
        generateMallardFemale(basePath + "/mallard/mallard_female.png");
        generateMallardDuckling(basePath + "/mallard/mallard_duckling.png");

        // Spawn egg textures (16x16) — unchanged
        String itemPath = "src/main/resources/assets/britishbirds/textures/item";
        generateSpawnEgg(itemPath + "/robin_spawn_egg.png",
                new Color(0x6B, 0x6B, 0x3A), new Color(0xD4, 0x60, 0x2A));
        generateSpawnEgg(itemPath + "/blue_tit_spawn_egg.png",
                new Color(0xFF, 0xE0, 0x20), new Color(0x2A, 0x7A, 0xC4));
        generateSpawnEgg(itemPath + "/barn_owl_spawn_egg.png",
                new Color(0xC4, 0xA0, 0x55), new Color(0xF0, 0xE8, 0xD0));
        generateSpawnEgg(itemPath + "/peregrine_falcon_spawn_egg.png",
                new Color(0x4A, 0x55, 0x68), new Color(0xE8, 0xE0, 0xD5));
        generateSpawnEgg(itemPath + "/mallard_spawn_egg.png",
                new Color(0x2D, 0x6B, 0x33), new Color(0x8B, 0x45, 0x13));

        System.out.println("All textures generated!");
    }

    // =========================================================================
    // Unified body-paint generator
    // =========================================================================

    /**
     * Paint all cuboid UV regions for a bird using a virtual body painting approach.
     * For each cuboid, determine its body region, look up colours from the palette,
     * and fill each face with the appropriate colour including gradients.
     */
    static void paintBird(BufferedImage img, Graphics2D g,
                          Map<String, int[]> dims, Map<String, int[]> layout,
                          BodyPalette palette) {
        for (Map.Entry<String, int[]> entry : layout.entrySet()) {
            String joint = entry.getKey();
            int[] uv = entry.getValue();
            int[] d = dims.get(joint);
            if (d == null) continue;

            int u = uv[0], v = uv[1];
            int w = d[0], h = d[1], depth = d[2];

            BodyRegion region = getBodyRegion(joint);

            Color top    = palette.getTop(region);
            Color bottom = palette.getBottom(region);
            Color front  = palette.getFront(region);
            Color back   = palette.getBack(region);
            Color left   = palette.getLeft(region);
            Color right  = palette.getRight(region);

            fillBox(g, u, v, w, h, depth, top, bottom, front, back, left, right);
        }
    }

    /**
     * Add detail pass: eyes on left and right side faces of the HEAD cuboid.
     *
     * UV face layout for a cuboid at texOffs(u,v) with dims (w,h,d):
     *   Left face:  (u,       v+d, d, h)
     *   Right face: (u+d+w,   v+d, d, h)
     *
     * Eyes are placed in the upper third of the side face, toward the front
     * (higher x on left face = closer to front, lower x on right face = closer to front).
     * Each eye is 1-2 dark pixels with a highlight pixel for realism.
     */
    static void addEyes(BufferedImage img, Map<String, int[]> layout, Map<String, int[]> dims,
                        Color eyeColor) {
        int[] headUV = layout.get(HEAD);
        int[] headDims = dims.get(HEAD);
        if (headUV == null || headDims == null) return;

        int u = headUV[0], v = headUV[1];
        int w = headDims[0], h = headDims[1], d = headDims[2];

        // Eye Y position: upper third of the side face (which starts at v+d, height h)
        int eyeY = v + d + Math.max(1, h / 4);

        // Eye highlight colour (white glint)
        Color highlight = new Color(0xEE, 0xEE, 0xEE);

        // Left side face starts at (u, v+d), width=d, height=h
        // Place eye toward the front edge (higher x within the face)
        int leftFaceStartX = u;
        int leftEyeX = leftFaceStartX + Math.max(0, d - d / 3 - 1);
        safeSetRGB(img, leftEyeX, eyeY, eyeColor);
        if (d > 2) safeSetRGB(img, leftEyeX - 1, eyeY, eyeColor);
        // Highlight pixel above-right of eye
        safeSetRGB(img, leftEyeX, eyeY - 1, highlight);

        // Right side face starts at (u+d+w, v+d), width=d, height=h
        // Place eye toward the front edge (lower x within the face)
        int rightFaceStartX = u + d + w;
        int rightEyeX = rightFaceStartX + Math.max(0, d / 3);
        safeSetRGB(img, rightEyeX, eyeY, eyeColor);
        if (d > 2) safeSetRGB(img, rightEyeX + 1, eyeY, eyeColor);
        // Highlight pixel above-left of eye
        safeSetRGB(img, rightEyeX, eyeY - 1, highlight);
    }

    /**
     * Explicitly paint ALL 6 faces of a beak cuboid with the given colour.
     * Small cuboids (1x1x2 etc.) can be missed by gradient/speckle passes,
     * so we force-fill every face to ensure the beak is fully coloured.
     */
    static void paintBeakCuboid(Graphics2D g, Map<String, int[]> layout, Map<String, int[]> dims,
                                String joint, Color beakColor) {
        int[] uv = layout.get(joint);
        int[] d = dims.get(joint);
        if (uv == null || d == null) return;

        int u = uv[0], v = uv[1];
        int w = d[0], h = d[1], depth = d[2];

        fillBox(g, u, v, w, h, depth, beakColor, beakColor, beakColor, beakColor, beakColor, beakColor);
    }

    /**
     * Paint both beak cuboids with a single colour, ensuring full coverage.
     */
    static void paintBeaks(Graphics2D g, Map<String, int[]> layout, Map<String, int[]> dims,
                           Color beakColor) {
        paintBeakCuboid(g, layout, dims, UPPER_BEAK, beakColor);
        paintBeakCuboid(g, layout, dims, LOWER_BEAK, beakColor);
    }

    /**
     * Ensure head-to-neck colour continuity: paint the back face of the head
     * and the top/front of neck_upper to share a compatible transition colour.
     */
    static void blendHeadNeck(Graphics2D g, BufferedImage img,
                              Map<String, int[]> layout, Map<String, int[]> dims,
                              Color headBackColor, Color neckTopColor) {
        // Head back face: at (u + 2*d + w, v + d) with size (w, h)
        int[] headUV = layout.get(HEAD);
        int[] headDims = dims.get(HEAD);
        if (headUV != null && headDims != null) {
            int u = headUV[0], v = headUV[1];
            int w = headDims[0], h = headDims[1], d = headDims[2];
            // Paint the lower portion of head back face with a blend toward neck colour
            int blendRows = Math.max(1, h / 3);
            for (int row = 0; row < blendRows; row++) {
                float ratio = (float)(row + 1) / blendRows;
                Color blended = blendColors(headBackColor, neckTopColor, ratio);
                int py = v + d + h - blendRows + row;
                for (int px = u + 2 * d + w; px < u + 2 * d + 2 * w; px++) {
                    safeSetRGB(img, px, py, blended);
                }
            }
        }

        // Neck upper top face: at (u+d, v, w, d)
        int[] neckUV = layout.get(NECK_UPPER);
        int[] neckDims = dims.get(NECK_UPPER);
        if (neckUV != null && neckDims != null) {
            int u = neckUV[0], v = neckUV[1];
            int w = neckDims[0], h = neckDims[1], d = neckDims[2];
            // Fill top face with the blend colour
            fillFace(g, u + d, v, w, d, neckTopColor);
        }
    }

    static Color blendColors(Color a, Color b, float ratio) {
        int r = (int)(a.getRed() + ratio * (b.getRed() - a.getRed()));
        int gr = (int)(a.getGreen() + ratio * (b.getGreen() - a.getGreen()));
        int bl = (int)(a.getBlue() + ratio * (b.getBlue() - a.getBlue()));
        return new Color(clamp(r), clamp(gr), clamp(bl));
    }

    // ---- Species-specific face markings ----

    /**
     * Blue tit: dark eye stripe through the eye on both side faces of the head.
     */
    static void addBlueTitEyeStripe(BufferedImage img, Map<String, int[]> layout,
                                     Map<String, int[]> dims, Color stripeColor) {
        int[] headUV = layout.get(HEAD);
        int[] headDims = dims.get(HEAD);
        if (headUV == null || headDims == null) return;

        int u = headUV[0], v = headUV[1];
        int w = headDims[0], h = headDims[1], d = headDims[2];

        // Stripe runs horizontally through the eye level on each side face
        int eyeY = v + d + Math.max(1, h / 4);

        // Left side face: full-width stripe at eye level
        for (int x = u; x < u + d; x++) {
            safeSetRGB(img, x, eyeY, stripeColor);
            if (h > 3) safeSetRGB(img, x, eyeY + 1, stripeColor);
        }

        // Right side face
        for (int x = u + d + w; x < u + d + w + d; x++) {
            safeSetRGB(img, x, eyeY, stripeColor);
            if (h > 3) safeSetRGB(img, x, eyeY + 1, stripeColor);
        }
    }

    /**
     * Peregrine: moustachial stripe below the eye on both side faces.
     */
    static void addPeregrineMoustache(BufferedImage img, Map<String, int[]> layout,
                                       Map<String, int[]> dims, Color stripeColor) {
        int[] headUV = layout.get(HEAD);
        int[] headDims = dims.get(HEAD);
        if (headUV == null || headDims == null) return;

        int u = headUV[0], v = headUV[1];
        int w = headDims[0], h = headDims[1], d = headDims[2];

        // Moustachial stripe: below eye, in lower half of side face
        int stripeY = v + d + Math.max(1, h / 2);

        // Left side face: stripe from center to front edge
        for (int x = u + d / 3; x < u + d; x++) {
            safeSetRGB(img, x, stripeY, stripeColor);
            if (h > 3) safeSetRGB(img, x, stripeY + 1, stripeColor);
        }

        // Right side face: stripe from front edge to center
        for (int x = u + d + w; x < u + d + w + d - d / 3; x++) {
            safeSetRGB(img, x, stripeY, stripeColor);
            if (h > 3) safeSetRGB(img, x, stripeY + 1, stripeColor);
        }
    }

    /**
     * Barn owl: heart-shaped facial disc. Cream front face with golden-brown
     * rim painted on the side faces near the front edge.
     */
    static void addBarnOwlFacialDisc(Graphics2D g, BufferedImage img,
                                      Map<String, int[]> layout, Map<String, int[]> dims,
                                      Color discCenter, Color discRim) {
        int[] headUV = layout.get(HEAD);
        int[] headDims = dims.get(HEAD);
        if (headUV == null || headDims == null) return;

        int u = headUV[0], v = headUV[1];
        int w = headDims[0], h = headDims[1], d = headDims[2];

        // Paint front face with cream disc centre
        fillFace(g, u + d, v + d, w, h, discCenter);

        // Paint disc rim on sides: a column near the front edge of each side face
        // Left side face: rightmost column (nearest to front face)
        for (int y = v + d; y < v + d + h; y++) {
            safeSetRGB(img, u + d - 1, y, discRim);
            if (d > 2) safeSetRGB(img, u + d - 2, y, discRim);
        }

        // Right side face: leftmost column (nearest to front face)
        for (int y = v + d; y < v + d + h; y++) {
            safeSetRGB(img, u + d + w, y, discRim);
            if (d > 2) safeSetRGB(img, u + d + w + 1, y, discRim);
        }

        // Top edge of front face: darker rim
        for (int x = u + d; x < u + d + w; x++) {
            safeSetRGB(img, x, v + d, discRim);
        }

        // Bottom edge of front face: V-shape for heart
        for (int x = u + d; x < u + d + w; x++) {
            safeSetRGB(img, x, v + d + h - 1, discRim);
        }
    }

    static void addWingBarring(BufferedImage img, Map<String, int[]> layout, Map<String, int[]> dims,
                               String joint, Color barColor, int spacing) {
        int[] uv = layout.get(joint);
        int[] d = dims.get(joint);
        if (uv == null || d == null) return;

        int u = uv[0], v = uv[1];
        int w = d[0], h = d[1], depth = d[2];

        // Bar across the front face area: (u+depth, v+depth, w, h)
        for (int y = v + depth; y < v + depth + h; y++) {
            if ((y - v - depth) % spacing == 0) {
                for (int x = u + depth; x < u + depth + w; x++) {
                    safeSetRGB(img, x, y, barColor);
                }
            }
        }
    }

    static void addSpecklesOnCuboid(BufferedImage img, Map<String, int[]> layout,
                                     Map<String, int[]> dims, String joint,
                                     Color dark, Color light) {
        int[] uv = layout.get(joint);
        int[] d = dims.get(joint);
        if (uv == null || d == null) return;

        int u = uv[0], v = uv[1];
        int w = d[0], h = d[1], depth = d[2];
        int uvW = 2 * (depth + w);
        int uvH = depth + h;

        addSpeckles(img, u, v, uvW, uvH, dark, light);
    }

    static void addGradientOnTop(BufferedImage img, Map<String, int[]> layout,
                                  Map<String, int[]> dims, String joint,
                                  Color from, Color to) {
        int[] uv = layout.get(joint);
        int[] d = dims.get(joint);
        if (uv == null || d == null) return;

        int u = uv[0], v = uv[1];
        int w = d[0], depth = d[2];

        // Top face: (u+d, v, w, d)
        addGradientHorizontal(img, u + depth, v, w, depth, from, to);
    }

    static void addGradientOnFront(BufferedImage img, Map<String, int[]> layout,
                                    Map<String, int[]> dims, String joint,
                                    Color from, Color to) {
        int[] uv = layout.get(joint);
        int[] d = dims.get(joint);
        if (uv == null || d == null) return;

        int u = uv[0], v = uv[1];
        int w = d[0], h = d[1], depth = d[2];

        // Front face: (u+d, v+d, w, h)
        addGradientVertical(img, u + depth, v + depth, w, h, from, to);
    }

    // =========================================================================
    // === ROBIN ===
    // =========================================================================

    static BodyPalette robinPalette() {
        BodyPalette p = new BodyPalette();
        p.back      = new Color(0x6B, 0x6B, 0x3A);  // olive brown
        p.belly     = new Color(0xF0, 0xED, 0xE0);  // off-white
        p.flanks    = new Color(0x6B, 0x6B, 0x3A);
        p.headTop   = new Color(0x6B, 0x6B, 0x3A);
        p.headSide  = new Color(0x6B, 0x6B, 0x3A);
        p.headFront = new Color(0xD4, 0x60, 0x2A);  // orange-red face
        p.throat    = new Color(0xD4, 0x60, 0x2A);
        p.neckBack  = new Color(0x6B, 0x6B, 0x3A);
        p.neckFront = new Color(0xD4, 0x60, 0x2A);
        p.wingUpper = new Color(0x4A, 0x3B, 0x2A);  // dark brown
        p.wingLower = new Color(0x5A, 0x4B, 0x3A);
        p.wingTip   = new Color(0x4A, 0x3B, 0x2A);
        p.tail      = new Color(0x4A, 0x3B, 0x2A);
        p.tailUnder = new Color(0x5A, 0x4B, 0x3A);
        p.rump      = new Color(0x6B, 0x6B, 0x3A);
        p.leg       = new Color(0xC4, 0xA8, 0x82);  // pink-brown
        p.foot      = new Color(0xC4, 0xA8, 0x82);
        p.beak      = new Color(0x33, 0x22, 0x11);  // near black
        p.eye       = new Color(0x11, 0x11, 0x11);
        p.accent1   = new Color(0xC0, 0x50, 0x20);  // deep orange
        p.accent2   = new Color(0x8B, 0x9D, 0xAF);  // blue-grey border
        p.accent3   = new Color(0x7B, 0x7B, 0x48);  // light olive
        return p;
    }

    static void generateRobin(String path) throws Exception {
        Map<String, int[]> dims = defaultPasserineDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = robinPalette();
        paintBird(img, g, dims, layout, p);

        // Detail: orange breast gradient on chest front
        addGradientOnFront(img, layout, dims, CHEST, p.throat, p.accent1);
        addGradientOnFront(img, layout, dims, TORSO, p.accent1, p.belly);

        // Gradient on back (top face)
        addGradientOnTop(img, layout, dims, CHEST, p.accent3, p.back);

        // Wing detail: olive fringes
        addWingBarring(img, layout, dims, L_UPPER_WING, new Color(0xC4, 0xA8, 0x82), 3);
        addWingBarring(img, layout, dims, R_UPPER_WING, new Color(0xC4, 0xA8, 0x82), 3);

        // Explicitly paint beaks
        paintBeaks(g, layout, dims, p.beak);

        // Head-neck colour continuity
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);

        // Eyes
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateRobinJuvenile(String path) throws Exception {
        Map<String, int[]> dims = defaultPasserineDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = new BodyPalette();
        Color speckledBrown = new Color(0x8B, 0x73, 0x55);
        Color goldenBuff = new Color(0xA8, 0x90, 0x70);
        p.back = speckledBrown; p.belly = goldenBuff; p.flanks = speckledBrown;
        p.headTop = speckledBrown; p.headSide = speckledBrown; p.headFront = speckledBrown;
        p.throat = goldenBuff; p.neckBack = speckledBrown; p.neckFront = goldenBuff;
        p.wingUpper = new Color(0x6B, 0x5B, 0x3A); p.wingLower = speckledBrown;
        p.wingTip = new Color(0x6B, 0x5B, 0x3A);
        p.tail = new Color(0x6B, 0x5B, 0x3A); p.tailUnder = speckledBrown;
        p.rump = speckledBrown;
        p.leg = new Color(0xC4, 0xA8, 0x82); p.foot = new Color(0xC4, 0xA8, 0x82);
        p.beak = new Color(0x33, 0x22, 0x11);
        p.eye = new Color(0x11, 0x11, 0x11);

        paintBird(img, g, dims, layout, p);

        // Speckles on body cuboids
        Color darkSpeckle = new Color(0x6B, 0x5B, 0x3A);
        addSpecklesOnCuboid(img, layout, dims, CHEST, darkSpeckle, goldenBuff);
        addSpecklesOnCuboid(img, layout, dims, TORSO, darkSpeckle, goldenBuff);
        addSpecklesOnCuboid(img, layout, dims, HEAD, darkSpeckle, goldenBuff);

        paintBeaks(g, layout, dims, p.beak);
        blendHeadNeck(g, img, layout, dims, speckledBrown, speckledBrown);
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === BLUE TIT ===
    // =========================================================================

    static void generateBlueTit(String path) throws Exception {
        Map<String, int[]> dims = defaultPasserineDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = new BodyPalette();
        p.back      = new Color(0x8B, 0xB0, 0x30);  // olive-green back
        p.belly     = new Color(0xFF, 0xE0, 0x20);  // bright yellow
        p.flanks    = new Color(0x8B, 0xB0, 0x30);
        p.headTop   = new Color(0x2A, 0x7A, 0xC4);  // blue cap
        p.headSide  = new Color(0xF5, 0xF5, 0xF0);  // white cheeks
        p.headFront = new Color(0xF5, 0xF5, 0xF0);  // white face
        p.throat    = new Color(0x1A, 0x2A, 0x44);  // dark chin
        p.neckBack  = new Color(0x8B, 0xB0, 0x30);
        p.neckFront = new Color(0xFF, 0xE0, 0x20);
        p.wingUpper = new Color(0x4A, 0x8A, 0xC0);  // blue wing
        p.wingLower = new Color(0x5A, 0x9A, 0xD0);
        p.wingTip   = new Color(0x4A, 0x8A, 0xC0);
        p.tail      = new Color(0x7A, 0x8A, 0x9A);  // grey-blue
        p.tailUnder = new Color(0x8A, 0x9A, 0xAA);
        p.rump      = new Color(0x8B, 0xB0, 0x30);
        p.leg       = new Color(0x5A, 0x6A, 0x7A);  // blue-grey
        p.foot      = new Color(0x5A, 0x6A, 0x7A);
        p.beak      = new Color(0x22, 0x22, 0x33);
        p.eye       = new Color(0x11, 0x11, 0x11);
        p.accent1   = new Color(0x1A, 0x2A, 0x44);  // dark eye stripe
        p.accent2   = new Color(0xE0, 0xE5, 0xF0);  // white wing bar

        paintBird(img, g, dims, layout, p);

        // Dark belly stripe on chest front
        addGradientOnFront(img, layout, dims, CHEST, p.belly, new Color(0xF0, 0xD0, 0x10));

        // Wing bar detail
        addWingBarring(img, layout, dims, L_UPPER_WING, p.accent2, 3);
        addWingBarring(img, layout, dims, R_UPPER_WING, p.accent2, 3);

        // Back gradient
        addGradientOnTop(img, layout, dims, CHEST, p.back, new Color(0x7B, 0xA0, 0x28));

        // Explicitly paint beaks
        paintBeaks(g, layout, dims, p.beak);

        // Head-neck colour continuity
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);

        // Blue tit eye stripe through the eye
        addBlueTitEyeStripe(img, layout, dims, p.accent1);

        // Eyes (painted after stripe so they sit on top)
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === BARN OWL ===
    // =========================================================================

    static BodyPalette barnOwlMalePalette() {
        BodyPalette p = new BodyPalette();
        p.back      = new Color(0xC4, 0xA0, 0x55);  // golden buff
        p.belly     = new Color(0xF5, 0xF0, 0xE0);  // white
        p.flanks    = new Color(0xC4, 0xA0, 0x55);
        p.headTop   = new Color(0xC4, 0xA0, 0x55);
        p.headSide  = new Color(0xC4, 0xA0, 0x55);
        p.headFront = new Color(0xF0, 0xE8, 0xD0);  // cream face disc
        p.throat    = new Color(0xF0, 0xE8, 0xD0);
        p.neckBack  = new Color(0xC4, 0xA0, 0x55);
        p.neckFront = new Color(0xF0, 0xE8, 0xD0);
        p.wingUpper = new Color(0xC4, 0xA0, 0x55);
        p.wingLower = new Color(0xD4, 0xB0, 0x65);
        p.wingTip   = new Color(0xC4, 0xA0, 0x55);
        p.tail      = new Color(0xC4, 0xA0, 0x55);
        p.tailUnder = new Color(0xD4, 0xB0, 0x65);
        p.rump      = new Color(0xC4, 0xA0, 0x55);
        p.leg       = new Color(0xB0, 0xA0, 0x90);  // grey-pink
        p.foot      = new Color(0xB0, 0xA0, 0x90);
        p.beak      = new Color(0xD4, 0xC0, 0xA0);  // pale horn
        p.eye       = new Color(0x11, 0x11, 0x11);
        p.accent1   = new Color(0xA0, 0x85, 0x60);  // buff facial disc rim
        p.accent2   = new Color(0xB8, 0x8A, 0x48);  // tawny orange
        p.accent3   = new Color(0x4A, 0x3B, 0x2A);  // dark spots
        return p;
    }

    static void generateBarnOwlMale(String path) throws Exception {
        Map<String, int[]> dims = barnOwlDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = barnOwlMalePalette();
        paintBird(img, g, dims, layout, p);

        // Dorsal spots on back
        addSpecklesOnCuboid(img, layout, dims, CHEST, p.accent3, p.accent2);
        addSpecklesOnCuboid(img, layout, dims, TORSO, p.accent3, p.accent2);

        // Wing barring
        addWingBarring(img, layout, dims, L_UPPER_WING, p.accent2, 2);
        addWingBarring(img, layout, dims, L_FOREARM, p.accent2, 2);
        addWingBarring(img, layout, dims, R_UPPER_WING, p.accent2, 2);
        addWingBarring(img, layout, dims, R_FOREARM, p.accent2, 2);

        // Gradient on back
        addGradientOnTop(img, layout, dims, CHEST, new Color(0xD4, 0xB0, 0x65), p.accent2);

        // Explicitly paint beaks
        paintBeaks(g, layout, dims, p.beak);

        // Head-neck colour continuity
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);

        // Face: heart-shaped facial disc
        addBarnOwlFacialDisc(g, img, layout, dims, p.headFront, p.accent1);

        // Eyes (after facial disc so they sit on top)
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateBarnOwlFemale(String path) throws Exception {
        Map<String, int[]> dims = barnOwlDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = barnOwlMalePalette();
        // Female has buffier breast with spots
        p.belly = new Color(0xE8, 0xDD, 0xC8);

        paintBird(img, g, dims, layout, p);

        // More spots on female
        addSpecklesOnCuboid(img, layout, dims, CHEST, p.accent3, new Color(0xD4, 0xB0, 0x65));
        addSpecklesOnCuboid(img, layout, dims, TORSO, p.accent3, new Color(0xD4, 0xB0, 0x65));
        addSpecklesOnCuboid(img, layout, dims, HIP, p.accent3, p.belly);

        // Wing barring
        addWingBarring(img, layout, dims, L_UPPER_WING, p.accent2, 2);
        addWingBarring(img, layout, dims, L_FOREARM, p.accent2, 2);
        addWingBarring(img, layout, dims, R_UPPER_WING, p.accent2, 2);
        addWingBarring(img, layout, dims, R_FOREARM, p.accent2, 2);

        addGradientOnTop(img, layout, dims, CHEST, new Color(0xD4, 0xB0, 0x65), p.accent2);

        paintBeaks(g, layout, dims, p.beak);
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);
        addBarnOwlFacialDisc(g, img, layout, dims, p.headFront, p.accent1);
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === PEREGRINE FALCON ===
    // =========================================================================

    static BodyPalette peregrinePalette() {
        BodyPalette p = new BodyPalette();
        p.back      = new Color(0x4A, 0x55, 0x68);  // slate grey
        p.belly     = new Color(0xE8, 0xE0, 0xD5);  // pale barred
        p.flanks    = new Color(0x5A, 0x65, 0x78);
        p.headTop   = new Color(0x1A, 0x1A, 0x22);  // black helmet
        p.headSide  = new Color(0x1A, 0x1A, 0x22);
        p.headFront = new Color(0x1A, 0x1A, 0x22);
        p.throat    = new Color(0xF0, 0xEC, 0xE5);  // white throat
        p.neckBack  = new Color(0x4A, 0x55, 0x68);
        p.neckFront = new Color(0xF0, 0xEC, 0xE5);
        p.wingUpper = new Color(0x4A, 0x55, 0x68);
        p.wingLower = new Color(0x5A, 0x65, 0x78);
        p.wingTip   = new Color(0x3A, 0x42, 0x52);  // darker tips
        p.tail      = new Color(0x4A, 0x55, 0x68);
        p.tailUnder = new Color(0x5A, 0x65, 0x78);
        p.rump      = new Color(0x4A, 0x55, 0x68);
        p.leg       = new Color(0xDA, 0xA5, 0x20);  // yellow
        p.foot      = new Color(0xDA, 0xA5, 0x20);
        p.beak      = new Color(0x2A, 0x2A, 0x33);
        p.eye       = new Color(0x11, 0x11, 0x11);
        p.accent1   = new Color(0x55, 0x4A, 0x3E);  // dark barring
        p.accent2   = new Color(0xDA, 0xA5, 0x20);  // yellow cere
        return p;
    }

    static void generatePeregrineAdult(String path) throws Exception {
        Map<String, int[]> dims = peregrineDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = peregrinePalette();
        paintBird(img, g, dims, layout, p);

        // Barring on breast/belly
        addWingBarring(img, layout, dims, CHEST, p.accent1, 2);
        addWingBarring(img, layout, dims, TORSO, p.accent1, 2);

        // Gradient on back
        addGradientOnTop(img, layout, dims, CHEST, new Color(0x5A, 0x65, 0x78), new Color(0x3A, 0x42, 0x52));

        // Wing tip darkening
        addGradientOnTop(img, layout, dims, L_HAND, p.wingUpper, p.wingTip);
        addGradientOnTop(img, layout, dims, R_HAND, p.wingUpper, p.wingTip);

        // Explicitly paint beaks
        paintBeaks(g, layout, dims, p.beak);

        // Head-neck colour continuity
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);

        // Moustachial stripe below eye
        addPeregrineMoustache(img, layout, dims, new Color(0x11, 0x11, 0x11));

        // Eyes (after moustache so they sit on top)
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generatePeregrineJuvenile(String path) throws Exception {
        Map<String, int[]> dims = peregrineDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = new BodyPalette();
        Color brown = new Color(0x6B, 0x55, 0x3A);
        Color buffCream = new Color(0xE0, 0xD0, 0xB5);
        Color darkHead = new Color(0x3A, 0x2E, 0x22);
        p.back = brown; p.belly = buffCream; p.flanks = brown;
        p.headTop = darkHead; p.headSide = darkHead; p.headFront = darkHead;
        p.throat = new Color(0xD0, 0xC0, 0xA5);
        p.neckBack = brown; p.neckFront = buffCream;
        p.wingUpper = brown; p.wingLower = brown; p.wingTip = brown;
        p.tail = brown; p.tailUnder = brown; p.rump = brown;
        p.leg = new Color(0x7A, 0x85, 0x90);
        p.foot = new Color(0x7A, 0x85, 0x90);
        p.beak = new Color(0x2A, 0x2A, 0x33);
        p.eye = new Color(0x11, 0x11, 0x11);
        p.accent1 = new Color(0x4A, 0x3B, 0x2A);  // dark streaks

        paintBird(img, g, dims, layout, p);

        // Vertical streaking (juvenile has streaks, not bars)
        addWingBarring(img, layout, dims, CHEST, p.accent1, 2);
        addWingBarring(img, layout, dims, TORSO, p.accent1, 2);

        paintBeaks(g, layout, dims, p.beak);
        blendHeadNeck(g, img, layout, dims, darkHead, brown);
        addPeregrineMoustache(img, layout, dims, new Color(0x22, 0x1A, 0x11));
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === MALLARD ===
    // =========================================================================

    static void generateMallardMale(String path) throws Exception {
        Map<String, int[]> dims = mallardDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        BodyPalette p = new BodyPalette();
        p.back      = new Color(0x8A, 0x7E, 0x72);  // grey-brown
        p.belly     = new Color(0xB0, 0xAA, 0x9E);  // grey flanks
        p.flanks    = new Color(0xB0, 0xAA, 0x9E);
        p.headTop   = new Color(0x2D, 0x6B, 0x33);  // iridescent green
        p.headSide  = new Color(0x2D, 0x6B, 0x33);
        p.headFront = new Color(0x2D, 0x6B, 0x33);
        p.throat    = new Color(0x2D, 0x6B, 0x33);
        p.neckBack  = new Color(0x2D, 0x6B, 0x33);
        p.neckFront = new Color(0xF0, 0xF0, 0xF0);  // white ring
        p.wingUpper = new Color(0x8A, 0x7E, 0x72);
        p.wingLower = new Color(0x9A, 0x8E, 0x82);
        p.wingTip   = new Color(0x8A, 0x7E, 0x72);
        p.tail      = new Color(0xB0, 0xAA, 0x9E);
        p.tailUnder = new Color(0xC0, 0xBA, 0xAE);
        p.rump      = new Color(0x1A, 0x1A, 0x1A);  // black rump
        p.leg       = new Color(0xFF, 0x8C, 0x00);  // orange
        p.foot      = new Color(0xFF, 0x8C, 0x00);
        p.beak      = new Color(0xDA, 0xA5, 0x20);  // yellow bill
        p.eye       = new Color(0x11, 0x11, 0x11);
        p.accent1   = new Color(0x8B, 0x45, 0x13);  // chestnut breast
        p.accent2   = new Color(0x6A, 0x5A, 0xCD);  // speculum purple-blue

        paintBird(img, g, dims, layout, p);

        // Chestnut breast on chest front
        {
            int[] uv = layout.get(CHEST);
            int[] d = dims.get(CHEST);
            if (uv != null && d != null) {
                int u = uv[0], v = uv[1];
                int w = d[0], h = d[1], depth = d[2];
                // Front face: chestnut
                fillFace(g, u + depth, v + depth, w, h, p.accent1);
                // Gradient: chestnut to grey
                addGradientVertical(img, u + depth, v + depth, w, h,
                        p.accent1, new Color(0x7B, 0x35, 0x08));
            }
        }

        // Speculum on secondaries
        {
            for (String sec : new String[]{L_SECONDARIES, R_SECONDARIES}) {
                int[] uv = layout.get(sec);
                int[] d = dims.get(sec);
                if (uv != null && d != null) {
                    int u = uv[0], v = uv[1];
                    int w = d[0], h = d[1], depth = d[2];
                    // Paint speculum on top face
                    fillFace(g, u + depth, v, w, depth, p.accent2);
                    // White borders
                    for (int x = u + depth; x < u + depth + w; x++) {
                        safeSetRGB(img, x, v, new Color(0xF0, 0xF0, 0xF0));
                        safeSetRGB(img, x, v + depth - 1, new Color(0xF0, 0xF0, 0xF0));
                    }
                }
            }
        }

        // Vermiculation on flanks
        addSpecklesOnCuboid(img, layout, dims, TORSO,
                new Color(0x9A, 0x8E, 0x82), new Color(0xC0, 0xBA, 0xAE));

        addGradientOnTop(img, layout, dims, CHEST, p.back, new Color(0x7A, 0x6E, 0x62));

        // Explicitly paint beaks (yellow bill)
        paintBeaks(g, layout, dims, p.beak);

        // Head-neck colour continuity (green head to white neck ring)
        blendHeadNeck(g, img, layout, dims, p.headTop, p.neckBack);

        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardFemale(String path) throws Exception {
        Map<String, int[]> dims = mallardDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color mottledBrown = new Color(0x8B, 0x73, 0x55);
        Color buffEdge = new Color(0xC4, 0xAA, 0x82);

        BodyPalette p = new BodyPalette();
        p.back = mottledBrown; p.belly = mottledBrown; p.flanks = mottledBrown;
        p.headTop = new Color(0x4A, 0x3B, 0x2A);  // dark crown
        p.headSide = new Color(0xD0, 0xBB, 0x95);  // buff face
        p.headFront = new Color(0xD0, 0xBB, 0x95);
        p.throat = new Color(0xD0, 0xBB, 0x95);
        p.neckBack = mottledBrown; p.neckFront = mottledBrown;
        p.wingUpper = mottledBrown; p.wingLower = mottledBrown;
        p.wingTip = mottledBrown;
        p.tail = mottledBrown; p.tailUnder = mottledBrown;
        p.rump = mottledBrown;
        p.leg = new Color(0xE0, 0x80, 0x20);  // orange
        p.foot = new Color(0xE0, 0x80, 0x20);
        p.beak = new Color(0xC0, 0x80, 0x40);  // mottled orange
        p.eye = new Color(0x11, 0x11, 0x11);

        paintBird(img, g, dims, layout, p);

        // Overall mottled speckles
        Color darkBrown = new Color(0x5A, 0x48, 0x30);
        addSpecklesOnCuboid(img, layout, dims, CHEST, darkBrown, buffEdge);
        addSpecklesOnCuboid(img, layout, dims, TORSO, darkBrown, buffEdge);
        addSpecklesOnCuboid(img, layout, dims, HIP, darkBrown, buffEdge);

        // Speculum on secondaries
        Color speculum = new Color(0x6A, 0x5A, 0xCD);
        for (String sec : new String[]{L_SECONDARIES, R_SECONDARIES}) {
            int[] uv = layout.get(sec);
            int[] d = dims.get(sec);
            if (uv != null && d != null) {
                fillFace(g, uv[0] + d[2], uv[1], d[0], d[2], speculum);
            }
        }

        paintBeaks(g, layout, dims, p.beak);
        blendHeadNeck(g, img, layout, dims, p.headTop, mottledBrown);
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardDuckling(String path) throws Exception {
        Map<String, int[]> dims = mallardDimensions();
        Map<String, int[]> layout = computeLayout(dims);
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color yellow = new Color(0xFF, 0xD7, 0x00);
        Color darkBack = new Color(0x3A, 0x2E, 0x22);

        BodyPalette p = new BodyPalette();
        p.back = darkBack; p.belly = yellow; p.flanks = yellow;
        p.headTop = darkBack; p.headSide = yellow; p.headFront = yellow;
        p.throat = yellow;
        p.neckBack = darkBack; p.neckFront = yellow;
        p.wingUpper = darkBack; p.wingLower = darkBack; p.wingTip = darkBack;
        p.tail = darkBack; p.tailUnder = darkBack;
        p.rump = darkBack;
        p.leg = new Color(0x55, 0x55, 0x55);
        p.foot = new Color(0x55, 0x55, 0x55);
        p.beak = new Color(0x55, 0x55, 0x55);
        p.eye = new Color(0x11, 0x11, 0x11);

        paintBird(img, g, dims, layout, p);
        paintBeaks(g, layout, dims, p.beak);
        blendHeadNeck(g, img, layout, dims, darkBack, darkBack);
        addEyes(img, layout, dims, p.eye);

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === SPAWN EGG TEXTURES (16x16) — unchanged ===
    // =========================================================================

    static void generateSpawnEgg(String path, Color base, Color spots) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, 16, 16);
        g.setComposite(AlphaComposite.SrcOver);

        int[][] eggRows = {
            {-1, -1}, {6, 9}, {5, 10}, {4, 11}, {4, 11},
            {3, 12}, {3, 12}, {3, 12}, {3, 12}, {3, 12},
            {4, 11}, {4, 11}, {5, 10}, {5, 10}, {6, 9}, {-1, -1},
        };

        for (int y = 0; y < 16; y++) {
            if (eggRows[y][0] == -1) continue;
            for (int x = eggRows[y][0]; x < eggRows[y][1]; x++) {
                img.setRGB(x, y, base.getRGB());
            }
        }

        int[][] spotPositions = {
            {6, 3}, {9, 3}, {5, 5}, {8, 5}, {10, 5},
            {7, 6}, {4, 7}, {10, 7}, {6, 8}, {9, 9},
            {5, 10}, {8, 10}, {11, 10}, {7, 11}, {4, 12}, {9, 12},
        };
        for (int[] pos : spotPositions) {
            int x = pos[0], y = pos[1];
            if (y < 16 && eggRows[y][0] != -1 && x >= eggRows[y][0] && x < eggRows[y][1]) {
                img.setRGB(x, y, spots.getRGB());
            }
        }

        Color highlight = new Color(
                Math.min(255, base.getRed() + 40),
                Math.min(255, base.getGreen() + 40),
                Math.min(255, base.getBlue() + 40));
        int[][] highlightPixels = {{6, 2}, {7, 2}, {5, 3}, {6, 3}, {5, 4}, {4, 5}};
        for (int[] pos : highlightPixels) {
            int x = pos[0], y = pos[1];
            if (y < 16 && eggRows[y][0] != -1 && x >= eggRows[y][0] && x < eggRows[y][1]) {
                img.setRGB(x, y, highlight.getRGB());
            }
        }

        Color shadow = new Color(
                Math.max(0, base.getRed() - 30),
                Math.max(0, base.getGreen() - 30),
                Math.max(0, base.getBlue() - 30));
        int[][] shadowPixels = {{10, 10}, {10, 11}, {9, 12}, {9, 13}, {8, 13}};
        for (int[] pos : shadowPixels) {
            int x = pos[0], y = pos[1];
            if (y < 16 && eggRows[y][0] != -1 && x >= eggRows[y][0] && x < eggRows[y][1]) {
                img.setRGB(x, y, shadow.getRGB());
            }
        }

        g.dispose();
        fillTransparentPixels(img);
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // =========================================================================
    // === HELPER METHODS ===
    // =========================================================================

    static BufferedImage createImage() {
        return new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    static void clearImage(Graphics2D g) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);
    }

    /**
     * Fill a cuboid UV region. For a box at texOffs(u,v) with dims (w,h,d):
     * Top:    (u+d,   v,     w, d)
     * Bottom: (u+d+w, v,     w, d)
     * Left:   (u,     v+d,   d, h)
     * Front:  (u+d,   v+d,   w, h)
     * Right:  (u+d+w, v+d,   d, h)
     * Back:   (u+2d+w,v+d,   w, h)
     */
    static void fillBox(Graphics2D g, int u, int v, int w, int h, int d,
                         Color top, Color bottom, Color front, Color back, Color left, Color right) {
        g.setColor(top);
        g.fillRect(u + d, v, w, d);
        g.setColor(bottom);
        g.fillRect(u + d + w, v, w, d);
        g.setColor(left);
        g.fillRect(u, v + d, d, h);
        g.setColor(front);
        g.fillRect(u + d, v + d, w, h);
        g.setColor(right);
        g.fillRect(u + d + w, v + d, d, h);
        g.setColor(back);
        g.fillRect(u + 2 * d + w, v + d, w, h);
    }

    static void fillFace(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    static void addSpeckles(BufferedImage img, int startX, int startY, int w, int h,
                            Color dark, Color light) {
        for (int y = startY; y < startY + h && y < SIZE; y++) {
            for (int x = startX; x < startX + w && x < SIZE; x++) {
                if ((x + y) % 3 == 0) {
                    img.setRGB(x, y, dark.getRGB());
                } else if ((x + y) % 5 == 0) {
                    img.setRGB(x, y, light.getRGB());
                }
            }
        }
    }

    static void addGradientHorizontal(BufferedImage img, int startX, int startY, int w, int h,
                                       Color from, Color to) {
        for (int x = startX; x < startX + w && x < SIZE; x++) {
            float ratio = (float)(x - startX) / Math.max(1, w - 1);
            int r = (int)(from.getRed() + ratio * (to.getRed() - from.getRed()));
            int gr = (int)(from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
            int b = (int)(from.getBlue() + ratio * (to.getBlue() - from.getBlue()));
            Color c = new Color(clamp(r), clamp(gr), clamp(b));
            for (int y = startY; y < startY + h && y < SIZE; y++) {
                img.setRGB(x, y, c.getRGB());
            }
        }
    }

    static void addGradientVertical(BufferedImage img, int startX, int startY, int w, int h,
                                     Color from, Color to) {
        for (int y = startY; y < startY + h && y < SIZE; y++) {
            float ratio = (float)(y - startY) / Math.max(1, h - 1);
            int r = (int)(from.getRed() + ratio * (to.getRed() - from.getRed()));
            int gr = (int)(from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
            int b = (int)(from.getBlue() + ratio * (to.getBlue() - from.getBlue()));
            Color c = new Color(clamp(r), clamp(gr), clamp(b));
            for (int x = startX; x < startX + w && x < SIZE; x++) {
                img.setRGB(x, y, c.getRGB());
            }
        }
    }

    static void safeSetRGB(BufferedImage img, int x, int y, Color c) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            img.setRGB(x, y, c.getRGB());
        }
    }

    static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    /**
     * Fill any remaining transparent pixels with the nearest opaque colour.
     * This prevents invisible cuboid faces when UV regions map to unpainted areas.
     * Uses a simple approach: find the most common body colour and fill all
     * transparent pixels with it.
     */
    static void fillTransparentPixels(BufferedImage img) {
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        // First pass: find the most common opaque colour (the body base colour)
        java.util.HashMap<Integer, Integer> colourCounts = new java.util.HashMap<>();
        for (int y = 0; y < imgH; y++) {
            for (int x = 0; x < imgW; x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha > 0) {
                    int rgb = argb | 0xFF000000; // force full alpha for counting
                    colourCounts.merge(rgb, 1, Integer::sum);
                }
            }
        }

        // Find the most common colour
        int fallbackColour = 0xFF808080; // grey default
        int maxCount = 0;
        for (var entry : colourCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                fallbackColour = entry.getKey();
            }
        }

        // Second pass: fill transparent pixels
        for (int y = 0; y < imgH; y++) {
            for (int x = 0; x < imgW; x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha == 0) {
                    img.setRGB(x, y, fallbackColour);
                }
            }
        }
    }

    static void save(BufferedImage img, String path) throws Exception {
        File file = new File(path);
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);
    }
}
