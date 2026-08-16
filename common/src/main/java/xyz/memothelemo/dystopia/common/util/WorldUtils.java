package xyz.memothelemo.dystopia.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public final class WorldUtils {

    private WorldUtils() {}

    public static boolean isKeepInventory(ServerPlayer player) {
        return isKeepInventory(player.level());
    }

    public static boolean isKeepInventory(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY);
        }
        return false;
    }

    public static boolean isKeepInventory(ServerLevel level) {
        return level.getGameRules().get(GameRules.KEEP_INVENTORY);
    }

    public static void dropItemStack(Level level, BlockPos blockPos, ItemStack item) {
        double spawnX = blockPos.getX();
        double spawnY = blockPos.getY() - 0.125F;
        double spawnZ = blockPos.getZ();

        ItemEntity droppedItem = new ItemEntity(level, spawnX, spawnY, spawnZ, item);
        droppedItem.setDeltaMovement(0, -1, 0);
        level.addFreshEntity(droppedItem);
    }

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
