package xyz.memothelemo.dystopia.common.util;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Utility class for inspecting runtime environment properties and Fabric Loader states.
 */
public final class EnvironmentUtils {
    private EnvironmentUtils() {}

    /**
     * Checks if the application is executing within a development environment.
     * @return {@code true} if running in a development environment, {@code false} otherwise
     */
    public static boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
