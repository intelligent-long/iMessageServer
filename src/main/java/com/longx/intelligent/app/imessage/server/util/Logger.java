package com.longx.intelligent.app.imessage.server.util;

import org.slf4j.LoggerFactory;

public class Logger {

    private static org.slf4j.Logger getLogger() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement e : stack) {
            if (!e.getClassName().equals(Logger.class.getName())
                    && !e.getClassName().startsWith("java.lang.Thread")) {
                return LoggerFactory.getLogger(e.getClassName());
            }
        }
        return LoggerFactory.getLogger(Logger.class);
    }

    public static void err(Object o) {
        err(null, o, null);
    }

    public static void err(Throwable t) {
        err(null, "", t);
    }

    public static void err(Class<?> clazz, Object o) {
        err(clazz, o, null);
    }

    public static void err(Class<?> clazz, Throwable t) {
        err(clazz, "", t);
    }

    public static void err(Class<?> clazz, Object o, Throwable t) {
        String text = o == null ? "null" : o.toString();

        org.slf4j.Logger logger = (clazz == null)
                ? getLogger()
                : LoggerFactory.getLogger(clazz);

        if (t != null) {
            logger.error(text, t);
        } else {
            logger.error(text);
        }
    }

    public static void info(Object o) {
        info(null, o, null);
    }

    public static void info(Throwable t) {
        info(null, "", t);
    }

    public static void info(Class<?> clazz, Object o) {
        info(clazz, o, null);
    }

    public static void info(Class<?> clazz, Throwable t) {
        info(clazz, "", t);
    }

    public static void info(Class<?> clazz, Object o, Throwable t) {
        String text = o == null ? "null" : o.toString();

        org.slf4j.Logger logger = (clazz == null)
                ? getLogger()
                : LoggerFactory.getLogger(clazz);

        if (t != null) {
            logger.info(text, t);
        } else {
            logger.info(text);
        }
    }
}