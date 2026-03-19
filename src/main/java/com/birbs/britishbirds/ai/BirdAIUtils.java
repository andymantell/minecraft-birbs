package com.birbs.britishbirds.ai;

import net.minecraft.world.phys.Vec3;

/**
 * Shared utilities for bird AI goals.
 */
public final class BirdAIUtils {

    private BirdAIUtils() {}

    /**
     * Safely normalize a direction vector. Returns Vec3.ZERO if the input
     * has zero length (source and target at same position), preventing NaN.
     */
    public static Vec3 safeDirection(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double lengthSqr = diff.lengthSqr();
        if (lengthSqr < 1.0E-8) {
            return Vec3.ZERO;
        }
        return diff.normalize();
    }
}
