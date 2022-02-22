package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4J {
    // add VMoption -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG to JVM
    public final static Logger LOGGER = LoggerFactory.getLogger("ProjectPlanner");

    public static void logException(String message, Exception ex) {
        LOGGER.error(message + " - " + ex.getClass().getName() + ": " + ex.getMessage());
        Throwable cause = ex.getCause();
        if (cause != null) {
            LOGGER.error("   caused by " + cause.getClass().getName() + ": " + cause.getMessage());
        }
        ex.printStackTrace();
    }
}
