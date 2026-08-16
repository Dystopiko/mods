package xyz.memothelemo.instantrestock;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantRestock implements DedicatedServerModInitializer {
    public static final String MOD_ID = "InstantRestock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean canApplyIRForPlayer(ServerPlayer player) {
        return Permissions.check(player, "dystopia.instantrestock");
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Loaded Instant Restock mod");
    }
}
