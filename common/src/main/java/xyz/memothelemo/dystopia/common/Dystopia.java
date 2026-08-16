package xyz.memothelemo.dystopia.common;

import net.minecraft.server.level.ServerPlayer;
import xyz.memothelemo.edenmc.api.EdenProvider;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

/**
 * Core utility class providing integration with the Dystopia server and Eden system.
 */
public final class Dystopia {
    private Dystopia() {}

    /**
     * Checks whether a player has a specific perk based on their cached
     * enabled perks set by the Eden system upon logging in to Dystopia.
     *
     * @param player the player to check for the perk
     * @param perkId the specified perk identifier
     * @return {@code true} if the player possesses the perk, {@code false} otherwise
     */
    public static boolean hasPerk(ServerPlayer player, String perkId) {
        User user = EdenProvider.get().getUser(player.getUUID());
        if (user instanceof Member member) {
            return member.getPerks().contains(perkId);
        }
        return false;
    }
}
