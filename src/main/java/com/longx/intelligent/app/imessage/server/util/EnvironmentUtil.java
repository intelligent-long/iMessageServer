package com.longx.intelligent.app.imessage.server.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by LONG on 2024/12/1 at 9:13 PM.
 */
public class EnvironmentUtil {

    public static String getApplicationDirectory() {
        String path = EnvironmentUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        Objects.requireNonNull(path);
        if (path.startsWith("nested:")) {
            path = path.substring("nested:".length());
        }
        if (path.contains("!")) {
            path = path.substring(0, path.indexOf("!"));
        }
        return new File(path).getParentFile().getAbsolutePath();
    }

    public enum System{Mac, Windows, Other}

    public static System determineSystem(){
        String osName = java.lang.System.getProperty("os.name").toLowerCase();
        if(osName.contains("mac")){
            return System.Mac;
        }else if(osName.contains("win")){
            return System.Windows;
        }else {
            return System.Other;
        }
    }

}
