package com.longx.intelligent.app.imessage.server.context;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by LONG on 2026/1/26 at 12:38 PM.
 */
public class SpringContextHolder {

    private static volatile ConfigurableApplicationContext context;

    public static void setContext(ConfigurableApplicationContext ctx) {
        context = ctx;
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    /**
     * 阻塞式优雅关闭，直到 Spring 完全退出
     */
    public static int gracefulShutdownAndWait() {
        if (context != null && context.isActive()) {
            return SpringApplication.exit(context, () -> 0);
        }
        return 0;
    }
}