/**
 * @author mamo
 */
package org.reflections.helper;

/**
 *
 */
//todo mamo >> something
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Logs {

    public static void error(String msg) {
        StackTraceElement calleeStackElement = Thread.currentThread().getStackTrace()[0];

        System.out.println("msg = " + msg);
    }
}