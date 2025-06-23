package com.longx.intelligent.app.imessage.server.util;

/**
 * Created by LONG on 2024/1/12 at 6:15 PM.
 */
public class ErrorLogger {

    public static void log(Object o){
        log(o, null);
    }

    public static void log(Throwable t){
        log("", t);
    }

    public static void log(Object o, Throwable t){
        String text = o == null ? "null" : o.toString();
        if(t != null) {
            t.printStackTrace();
        }else {
            System.err.println(text);
        }
    }
}
