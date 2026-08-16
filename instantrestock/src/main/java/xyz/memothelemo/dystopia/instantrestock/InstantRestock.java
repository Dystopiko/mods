package xyz.memothelemo.dystopia.instantrestock;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.memothelemo.dystopia.common.Dystopia;
import xyz.memothelemo.dystopia.common.util.PerkConstants;

public class InstantRestock implements DedicatedServerModInitializer {
    public static final String MOD_ID = "InstantRestock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean canApplyIRForPlayer(ServerPlayer player) {
        return Dystopia.hasPerk(player, PerkConstants.INSTANT_RESTOCK);
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Loaded Instant Restock mod");
    }
}
