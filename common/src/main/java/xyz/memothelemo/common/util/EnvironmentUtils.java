package xyz.memothelemo.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class EnvironmentUtils {

    private EnvironmentUtils() {}

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
