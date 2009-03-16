package net.tetromi.log;

import java.lang.reflect.Method;

/**
 * Generic static logger that wraps the Apache Commons Logging API using reflection.
 * Include commons-logging.jar and log4j.jar on the classpath to capture log messages.
 * If Commons Logging is not found, log messages will not be displayed.
 * @author will
 * @date Mar 7, 2009 12:07:21 AM
 */
public class LOG {
    private static Object log;
    private static Method trace;
    private static Method debug;
    private static Method info;
    private static Method warn;
    private static Method error;
    private static Method trace2;
    private static Method debug2;
    private static Method info2;
    private static Method warn2;
    private static Method error2;
    static {
        final Class lf;
        final Class lg;
        try {
            lf = Class.forName("org.apache.commons.logging.LogFactory");
            lg = Class.forName("org.apache.commons.logging.Log");
            final Method method = lf.getMethod("getLog", Class.class);
            log = method.invoke(lf,LOG.class);

            trace = lg.getMethod("trace",Object.class);
            debug = lg.getMethod("debug",Object.class);
            info = lg.getMethod("info",Object.class);
            warn = lg.getMethod("warn",Object.class);
            error = lg.getMethod("error",Object.class);

            trace2 = lg.getMethod("trace",Object.class,Throwable.class);
            debug2 = lg.getMethod("debug",Object.class,Throwable.class);
            info2 = lg.getMethod("info",Object.class,Throwable.class);
            warn2 = lg.getMethod("warn",Object.class,Throwable.class);
            error2 = lg.getMethod("error",Object.class,Throwable.class);
        } catch (Exception e) {
        }
    }


    public static void trace(String msg) {
        try {
            trace.invoke(log,msg);
        } catch (Exception e) {}
    }
    public static void trace(String msg, Throwable e) {
        try {
            trace2.invoke(log,msg,e);
        } catch (Exception e1) {}
    }
    public static void debug(String msg) {
        try {
            debug.invoke(log,msg);
        } catch (Exception e) {}
    }
    public static void debug(String msg, Throwable e) {
        try {
            debug2.invoke(log,msg,e);
        } catch (Exception e1) {}
    }
    public static void info(String msg) {
        try {
            info.invoke(log,msg);
        } catch (Exception e) {}
    }
    public static void info(String msg, Throwable e) {
        try {
            info2.invoke(log,msg,e);
        } catch (Exception e1) {}
    }
    public static void warn(String msg) {
        try {
            warn.invoke(log,msg);
        } catch (Exception e) {}
    }
    public static void warn(String msg, Throwable e) {
        try {
            warn2.invoke(log,msg,e);
        } catch (Exception e1) {}
    }
    public static void error(String msg) {
        try {
            error.invoke(log,msg);
        } catch (Exception e) {}
    }
    public static void error(String msg, Throwable e) {
        try {
            error2.invoke(log,msg,e);
        } catch (Exception e1) {}
    }
}
