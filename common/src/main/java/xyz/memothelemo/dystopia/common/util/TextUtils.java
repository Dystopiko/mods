package xyz.memothelemo.dystopia.common.util;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import xyz.memothelemo.dystopia.common.constant.MessageConstants;

public final class TextUtils {

    private TextUtils() {}

    public static Component formatPosition(BlockPos pos) {
        return MessageConstants.forPosition(pos);
    }
}
