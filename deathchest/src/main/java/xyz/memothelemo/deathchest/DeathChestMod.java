package xyz.memothelemo.deathchest;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.memothelemo.common.util.LogUtils;
import xyz.memothelemo.common.util.PermissionUtils;
import xyz.memothelemo.deathchest.model.DCServerData;

import java.time.Duration;
import java.time.Instant;

public class DeathChestMod implements DedicatedServerModInitializer {
    public static DCServerData data;
    public static Logger LOGGER = LogManager.getLogger("DeathChest");

    public static boolean hasPermissions(ServerPlayer player) {
        return PermissionUtils.canUseDeathChest(player);
    }

    @Override
    public void onInitializeServer() {
        LogUtils.configureDebugLogging(LOGGER);

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            // Chunks are not **fully** loaded by the time the server recently started.
            Instant instant = Instant.now();
            LOGGER.info("Loading persistent data");
            DeathChestMod.data = DCServerData.fromServer(server);

            Instant now = Instant.now();
            Duration elapsed = Duration.between(instant, now);
            LOGGER.info("Done loading persistent data (elapsed={}ms)", elapsed.toMillis());
        });
    }
}
