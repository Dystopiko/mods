package xyz.memothelemo.deathchest;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import xyz.memothelemo.common.constant.MessageConstants;

public class Components {
    public static final Component HEADER = MessageConstants.DEATHCHEST_HEADER;

    public static final Component IMPRACTICAL_TO_PLACE = MessageConstants.IMPRACTICAL_TO_PLACE;
    public static final Component FAILED_TO_GET_POSITION = MessageConstants.FAILED_TO_GET_POSITION;
    public static final Component CANNOT_OPEN_CHEST = MessageConstants.CANNOT_OPEN_CHEST;
    public static final Component CLAIMED = MessageConstants.CLAIMED;

    public static Component ownedByError(String owner) {
        return MessageConstants.ownedByError(owner);
    }

    public static Component forPosition(BlockPos position) {
        return MessageConstants.forPosition(position);
    }

    public static Component destroyedByExplosion(BlockPos position) {
        return MessageConstants.destroyedByExplosion(position);
    }

    public static Component newDeathChest(BlockPos position, String dimension) {
        return MessageConstants.newDeathChest(position, dimension);
    }
}
