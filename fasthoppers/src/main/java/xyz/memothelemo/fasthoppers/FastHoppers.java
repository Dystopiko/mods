package xyz.memothelemo.fasthoppers;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerPlayer;
import xyz.memothelemo.common.util.PermissionUtils;

public class FastHoppers implements ModInitializer {
	public static final String MOD_ID = "fasthoppers";

	public static boolean canWaxHoppers(ServerPlayer player) {
		return PermissionUtils.canWaxHoppers(player);
	}

	@Override
	public void onInitialize() {
	}
}