package xyz.memothelemo.fasthoppers;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class FastHoppers implements ModInitializer {
	public static final String MOD_ID = "fasthoppers";

	public static boolean canWaxHoppers(ServerPlayer player) {
		// Always enable if we're running this in a client.
		boolean isInClient = FabricLoader
			.getInstance()
			.getEnvironmentType() == EnvType.CLIENT;

		return isInClient || Permissions.check(player, "fasthoppers.wax");
	}

	@Override
	public void onInitialize() {
	}
}