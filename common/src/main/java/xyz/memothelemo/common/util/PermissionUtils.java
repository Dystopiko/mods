package xyz.memothelemo.common.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;
import xyz.memothelemo.common.constant.ModPermissions;
import xyz.memothelemo.edenmc.api.EdenProvider;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

public final class PermissionUtils {

    private PermissionUtils() {}

    public static boolean checkPermission(ServerPlayer player, String permissionNode) {
        return Permissions.check(player, permissionNode);
    }

    public static boolean checkEdenPerk(ServerPlayer player, String perkNode) {
        User user = EdenProvider.get().getUser(player.getUUID());
        if (user instanceof Member member) {
            return member.getPerks().contains(perkNode);
        }
        return false;
    }

    public static boolean canUseDeathChest(ServerPlayer player) {
        boolean keepInventory = WorldUtils.isKeepInventory(player);
        boolean hasDeadChestPerk = checkEdenPerk(player, ModPermissions.DEATHCHEST);
        return !keepInventory && hasDeadChestPerk;
    }

    public static boolean canWaxHoppers(ServerPlayer player) {
        return EnvironmentUtils.isClient() || checkPermission(player, ModPermissions.FASTHOPPERS_WAX);
    }

    public static boolean canInstantRestock(ServerPlayer player) {
        return checkPermission(player, ModPermissions.INSTANTRESTOCK);
    }
}
