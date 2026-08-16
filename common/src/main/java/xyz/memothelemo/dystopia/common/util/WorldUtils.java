package xyz.memothelemo.dystopia.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Utility functions for world manipulations, item entity spawning, and block entity lookups.
 */
public final class WorldUtils {
    private WorldUtils() {}

    /**
     * Spawns an item stack entity in the world at the given block position with downward velocity.
     *
     * @param level the level in which to drop the item stack
     * @param blockPos the block position where the item entity should spawn
     * @param item the item stack to drop
     */
    public static void dropItemStack(Level level, BlockPos blockPos, ItemStack item) {
        double spawnX = blockPos.getX();
        double spawnY = blockPos.getY() - 0.125F;
        double spawnZ = blockPos.getZ();

        ItemEntity droppedItem = new ItemEntity(level, spawnX, spawnY, spawnZ, item);
        droppedItem.setDeltaMovement(0, -1, 0);
        level.addFreshEntity(droppedItem);
    }

    /**
     * Retrieves a {@link HopperBlockEntity} at the specified position if the block and entity match.
     *
     * @param level the world level to query
     * @param pos the block position to inspect
     * @return the {@link HopperBlockEntity} instance if present and valid, or {@code null} otherwise
     */
    public static @Nullable HopperBlockEntity getHopperAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasBlockEntity()) {
            return null;
        }

        BlockEntity entity = level.getBlockEntity(pos);
        Block block = state.getBlock();
        if (!(entity instanceof HopperBlockEntity hopper && block instanceof HopperBlock)) {
            return null;
        }

        return hopper;
    }
}
