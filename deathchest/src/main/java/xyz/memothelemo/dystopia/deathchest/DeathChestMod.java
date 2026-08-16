package xyz.memothelemo.dystopia.deathchest;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.memothelemo.dystopia.common.Dystopia;
import xyz.memothelemo.dystopia.common.util.LogUtils;
import xyz.memothelemo.dystopia.common.util.PerkConstants;
import xyz.memothelemo.dystopia.deathchest.model.DCServerData;

import java.time.Duration;
import java.time.Instant;

/**
 * Main server mod initializer for the DeathChest mod.
 */
public class DeathChestMod implements DedicatedServerModInitializer {
    /**
     * Persistent death chest server data instance.
     */
    public static DCServerData data;

    /**
     * Logger instance for the DeathChest mod.
     */
    public static Logger LOGGER = LogManager.getLogger("DeathChest");

    /**
     * Checks if a player has permission/perk to generate and access death chests.
     *
     * @param player the server player to check
     * @return {@code true} if the player has the death chest perk, {@code false} otherwise
     */
    public static boolean hasPermissions(ServerPlayer player) {
        return Dystopia.hasPerk(player, PerkConstants.DEATH_CHEST);
    }

    /**
     * Initializes server-side components and registers lifecycle hooks for data loading.
     */
    @Override
    public void onInitializeServer() {
        LogUtils.configureDebugLogging(LOGGER);

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            Instant instant = Instant.now();
            LOGGER.info("Loading persistent data");
            DeathChestMod.data = DCServerData.fromServer(server);

            Instant now = Instant.now();
            Duration elapsed = Duration.between(instant, now);
            LOGGER.info("Done loading persistent data (elapsed={}ms)", elapsed.toMillis());
        });
    }
}
