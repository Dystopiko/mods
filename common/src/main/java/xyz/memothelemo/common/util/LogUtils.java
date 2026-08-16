package xyz.memothelemo.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public final class LogUtils {

    private LogUtils() {}

    public static void setMinimumLogLevel(Logger logger, Level level) {
        try {
            Configurator.setLevel(logger.getName(), level);
        } catch (Exception e) {
            logger.warn("Could not set minimum log level", e);
        }
    }

    public static void configureDebugLogging(Logger logger) {
        if (EnvironmentUtils.isDevelopment()) {
            setMinimumLogLevel(logger, Level.DEBUG);
            logger.debug("Debug mode is enabled");
        }
    }
}
