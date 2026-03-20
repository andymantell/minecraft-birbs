package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.animation.BirdSkeleton;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Computes UV texture offsets for the universal 32-cuboid bird skeleton layout.
 * Each species provides cuboid dimensions; this class packs UV rectangles into
 * a 512x512 texture sheet using a simple row-packing algorithm.
 *
 * Minecraft cuboid UV footprint for a box with dimensions (w, h, d):
 *   UV width  = 2 * (depth + width)
 *   UV height = depth + height
 */
public class BirdUVLayout {

    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private final Map<String, int[]> uvOffsets;

    private BirdUVLayout(Map<String, int[]> uvOffsets) {
        this.uvOffsets = uvOffsets;
    }

    /**
     * Compute UV layout by packing cuboid UV regions left-to-right, top-to-bottom
     * into rows of width 512.
     *
     * @param cuboidDimensions map of joint name to {width, height, depth}
     * @return a BirdUVLayout with computed (u, v) offsets per joint
     */
    public static BirdUVLayout computeLayout(Map<String, int[]> cuboidDimensions) {
        Map<String, int[]> offsets = new LinkedHashMap<>();
        int cursorX = 0;
        int cursorY = 0;
        int rowHeight = 0;

        for (Map.Entry<String, int[]> entry : cuboidDimensions.entrySet()) {
            String name = entry.getKey();
            int[] dims = entry.getValue(); // {w, h, d}
            int w = dims[0];
            int h = dims[1];
            int d = dims[2];

            int uvWidth = 2 * (d + w);
            int uvHeight = d + h;

            // Wrap to next row if this cuboid doesn't fit
            if (cursorX + uvWidth > TEXTURE_WIDTH) {
                cursorX = 0;
                cursorY += rowHeight;
                rowHeight = 0;
            }

            offsets.put(name, new int[]{cursorX, cursorY});
            cursorX += uvWidth;
            rowHeight = Math.max(rowHeight, uvHeight);
        }

        return new BirdUVLayout(offsets);
    }

    /**
     * Returns the (u, v) texture offset for a given joint name.
     * @return int array {u, v}, or null if the joint is not in this layout
     */
    public int[] getOffset(String jointName) {
        return uvOffsets.get(jointName);
    }

    /**
     * Returns the full map of joint name to (u, v) offsets.
     */
    public Map<String, int[]> getAllOffsets() {
        return uvOffsets;
    }

    public static int getTextureWidth() {
        return TEXTURE_WIDTH;
    }

    public static int getTextureHeight() {
        return TEXTURE_HEIGHT;
    }

    /**
     * Returns default cuboid dimensions for a small passerine (Robin-sized).
     * Map of joint name to {width, height, depth} in Minecraft pixels.
     */
    public static Map<String, int[]> getDefaultPasserineDimensions() {
        Map<String, int[]> dims = new LinkedHashMap<>();

        dims.put(BirdSkeleton.CHEST,          new int[]{3, 3, 3});
        dims.put(BirdSkeleton.SHOULDER_MOUNT, new int[]{2, 2, 2});
        dims.put(BirdSkeleton.TORSO,          new int[]{3, 3, 3});
        dims.put(BirdSkeleton.HIP,            new int[]{2, 2, 2});
        dims.put(BirdSkeleton.NECK_LOWER,     new int[]{2, 2, 1});
        dims.put(BirdSkeleton.NECK_MID,       new int[]{2, 2, 1});
        dims.put(BirdSkeleton.NECK_UPPER,     new int[]{2, 2, 1});
        dims.put(BirdSkeleton.HEAD,           new int[]{4, 4, 4});
        dims.put(BirdSkeleton.UPPER_BEAK,     new int[]{1, 1, 2});
        dims.put(BirdSkeleton.LOWER_BEAK,     new int[]{1, 1, 2});

        dims.put(BirdSkeleton.L_UPPER_WING,   new int[]{1, 4, 4});
        dims.put(BirdSkeleton.L_SCAPULARS,    new int[]{1, 3, 3});
        dims.put(BirdSkeleton.L_FOREARM,      new int[]{1, 3, 3});
        dims.put(BirdSkeleton.L_SECONDARIES,  new int[]{1, 3, 3});
        dims.put(BirdSkeleton.L_HAND,         new int[]{1, 3, 2});
        dims.put(BirdSkeleton.L_PRIMARIES,    new int[]{1, 3, 2});

        dims.put(BirdSkeleton.R_UPPER_WING,   new int[]{1, 4, 4});
        dims.put(BirdSkeleton.R_SCAPULARS,    new int[]{1, 3, 3});
        dims.put(BirdSkeleton.R_FOREARM,      new int[]{1, 3, 3});
        dims.put(BirdSkeleton.R_SECONDARIES,  new int[]{1, 3, 3});
        dims.put(BirdSkeleton.R_HAND,         new int[]{1, 3, 2});
        dims.put(BirdSkeleton.R_PRIMARIES,    new int[]{1, 3, 2});

        dims.put(BirdSkeleton.TAIL_BASE,      new int[]{2, 1, 2});
        dims.put(BirdSkeleton.TAIL_FAN,       new int[]{2, 1, 3});

        dims.put(BirdSkeleton.L_THIGH,        new int[]{1, 2, 1});
        dims.put(BirdSkeleton.L_SHIN,         new int[]{1, 3, 1});
        dims.put(BirdSkeleton.L_TARSUS,       new int[]{1, 2, 1});
        dims.put(BirdSkeleton.L_FOOT,         new int[]{2, 1, 2});

        dims.put(BirdSkeleton.R_THIGH,        new int[]{1, 2, 1});
        dims.put(BirdSkeleton.R_SHIN,         new int[]{1, 3, 1});
        dims.put(BirdSkeleton.R_TARSUS,       new int[]{1, 2, 1});
        dims.put(BirdSkeleton.R_FOOT,         new int[]{2, 1, 2});

        return dims;
    }
}
