/**
 * @author mamo
 */
package org.reflections.helper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mamo
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Logs {
    private static final Logger logger;

    static {
        logger = Logger.getAnonymousLogger();
    }

    public static void error(String msg) {
        log(Level.parse("ERROR"), msg);
    }

    public static void log(final Level level, String msg) {
        StackTraceElement callee = getCallee();

        logger.logp(level, callee.getClassName(), callee.getMethodName(), msg);
    }

    private static StackTraceElement getCallee() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void debug(String msg) {
        log(Level.parse("DEBUG"), msg);
    }
}