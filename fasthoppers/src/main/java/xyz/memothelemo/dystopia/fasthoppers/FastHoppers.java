package xyz.memothelemo.dystopia.fasthoppers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.memothelemo.dystopia.common.Dystopia;
import xyz.memothelemo.dystopia.common.util.PerkConstants;

/**
 * Main mod initializer for FastHoppers.
 */
public class FastHoppers implements ModInitializer {
    /**
     * Mod identifier string.
     */
    public static final String MOD_ID = "fasthoppers";

    /**
     * Checks if a player has permission to wax hoppers for instant item transfer.
     *
     * @param player the server player to check
     * @return {@code true} if allowed, {@code false} otherwise
     */
    public static boolean canWaxHoppers(@NonNull ServerPlayer player) {
        boolean isInClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        return isInClient || Dystopia.hasPerk(player, PerkConstants.WAX_HOPPERS);
    }

    /**
     * Initializes common mod logic.
     */
    @Override
    public void onInitialize() {
    }
}
