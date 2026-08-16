package xyz.memothelemo.deathchest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlacementSolver {
    /**
     * Finds the nearest block position within {@link #SEARCH_RADIUS} in
     * unit blocks from the provided origin position where a block can
     * be placed.
     */
    public static @Nullable BlockPos findNearestForBlock(
        @NonNull Level level,
        @NonNull BlockPos origin
    ) {
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        // Try the exact origin position, maybe we can place it there unless its bottom is not a block.
        if (level.getBlockState(origin).isAir() && !level.getBlockState(origin.below()).isAir()) {
            return origin;
        }

        // The origin column is the closest possible position by definition,
        // so check it first before entering the delta loop.
        BlockPos best = findPlaceablePosition(level, originX, originZ);
        int bestScore = best != null
            ? distanceSquared(best.getX(), best.getY(), best.getZ(), originX, originY, originZ)
            : Integer.MAX_VALUE;

        for (int[] delta : PRECOMPUTED_DELTAS) {
            int dx = delta[0];
            int dz = delta[1];

            int xzDist = dx * dx + dz * dz;
            if (xzDist >= bestScore) break;

            BlockPos attempt = findPlaceablePosition(level, originX + dx, originZ + dz);
            if (attempt == null) continue;

            int score = distanceSquared(
                attempt.getX(), attempt.getY(), attempt.getZ(),
                originX, originY, originZ
            );

            if (score < bestScore) {
                bestScore = score;
                best = attempt;
            }
        }

        return best;
    }

    /**
     * Finds the nearest block position within {@link #SEARCH_RADIUS} in
     * unit blocks of {@code player} where a block can be placed.
     */
    public static @Nullable BlockPos findNearestForBlock(@NonNull ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        Level level = player.level();
        return findNearestForBlock(level, origin);
    }

    /**
     * Returns the surface position at {@code (x, z)} if a block can be placed
     * there, or {@code null} otherwise.
     *
     * <p>A position is placeable when:
     * <ul>
     *   <li>The surface height is within the world's valid build range.</li>
     *   <li>The block at surface height is air (block fits).</li>
     * </ul>
     */
    private static @Nullable BlockPos findPlaceablePosition(Level level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (surfaceY < level.getMinY() || surfaceY > level.getMaxY()) {
            return null;
        }

        BlockPos result = new BlockPos(x, surfaceY, z);
        return level.getBlockState(result).isAir() ? result : null;
    }

    /** Horizontal search radius in blocks. */
    private static final int SEARCH_RADIUS = 8;

    /**
     * Offsets in x and z axes of every block within {@link #SEARCH_RADIUS},
     * sorted by distance in ascending order so the nearest column is
     * visited first.
     *
     * <p>Stored as {@code int[2]} arrays ({@code [dx, dz]}) to avoid the
     * autoboxing cost of a generic pair type.
     *
     * <p>The origin {@code (0, 0)} is excluded as it is checked separately
     * before the loop in {@link #findNearestForBlock}.
     */
    private static final List<int[]> PRECOMPUTED_DELTAS = buildPrecomputedDeltas();

    /**
     * Builds and sorts the delta list once at class load time so the cost
     * is paid once rather than on every player death.
     *
     * <p>A point {@code (dx, dz)} is included only when
     * {@code dx² + dz² ≤ r²}, giving a true circular search area.
     */
    private static List<int[]> buildPrecomputedDeltas() {
        int radiusSquared = SEARCH_RADIUS * SEARCH_RADIUS;
        List<int[]> deltas = new ArrayList<>();

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (dx * dx + dz * dz <= radiusSquared) {
                    deltas.add(new int[]{ dx, dz });
                }
            }
        }

        deltas.sort(Comparator.comparingInt(d -> d[0] * d[0] + d[1] * d[1]));
        return deltas;
    }

    /**
     * Returns the distance squared between two points in 3D space
     * using raw coordinates by using the Pythagorean formula.
     *
     * <p>{@link Math#sqrt} is intentionally omitted. Comparisons remain
     * correct without it, and skipping the square root avoids unnecessary
     * floating-point cost.
     */
    private static int distanceSquared(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        int dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }
}
