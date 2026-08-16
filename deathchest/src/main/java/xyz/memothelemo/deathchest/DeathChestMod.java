package xyz.memothelemo.deathchest;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configurator;
import xyz.memothelemo.deathchest.model.DCServerData;
import xyz.memothelemo.edenmc.api.EdenProvider;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeathChestMod implements DedicatedServerModInitializer {
    public static DCServerData data;
    public static Logger LOGGER = LogManager.getLogger("DeathChest");

    @SuppressWarnings("resource")
    public static boolean hasPermissions(ServerPlayer player) {
        boolean keepInventory = player.level()
            .getGameRules()
            .get(GameRules.KEEP_INVENTORY);

        boolean hasDeadChestPerk = false;
        User user = EdenProvider.get().getUser(player.getUUID());
        if (user instanceof Member member) {
            hasDeadChestPerk = member.getPerks().contains("dystopia.deathchest");
        }

        return !keepInventory && hasDeadChestPerk;
    }

    @Override
    public void onInitializeServer() {
        boolean isDebugEnabled = FabricLoader.getInstance().isDevelopmentEnvironment();
        if (isDebugEnabled) {
            setMinimumLogLevel(Level.DEBUG);
            LOGGER.debug("Debug mode is enabled");
        }

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

    private void setMinimumLogLevel(Level level) {
        try {
            Configurator.setLevel(LOGGER.getName(), level);
        } catch (Exception e) {
            LOGGER.warn("Could not set minimum log level", e);
        }
    }
}

