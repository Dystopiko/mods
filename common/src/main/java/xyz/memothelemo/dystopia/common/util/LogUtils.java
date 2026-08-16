package xyz.memothelemo.dystopia.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Utility class for managing Log4j loggers and logging levels dynamically.
 */
public final class LogUtils {
    private LogUtils() {}

    /**
     * Programmatically sets the minimum logging level for a specified logger.
     *
     * @param logger the Log4j logger to configure
     * @param level the desired minimum logging level
     */
    public static void setMinimumLogLevel(Logger logger, Level level) {
        try {
            Configurator.setLevel(logger.getName(), level);
        } catch (Exception e) {
            logger.warn("Could not set minimum log level", e);
        }
    }

    /**
     * Configures debug logging for a logger if running in a development environment.
     *
     * @param logger the Log4j logger to configure for debug logging
     */
    public static void configureDebugLogging(Logger logger) {
        if (EnvironmentUtils.isDevelopment()) {
            setMinimumLogLevel(logger, Level.DEBUG);
            logger.debug("Debug mode is enabled");
        }
    }
}
