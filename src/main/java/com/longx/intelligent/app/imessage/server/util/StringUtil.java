package com.longx.intelligent.app.imessage.server.util;

/**
 * Created by LONG on 2024/4/4 at 1:22 AM.
 */
public class StringUtil {
    public static Boolean containsIgnoreCase(Object target, String fragment) {
        if(target == null || fragment == null) throw new IllegalArgumentException("containsIgnoreCase 不能用于 null 参数");
        return target.toString().toUpperCase().contains(fragment.toUpperCase());
    }
}
