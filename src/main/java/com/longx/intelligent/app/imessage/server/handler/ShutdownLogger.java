package com.longx.intelligent.app.imessage.server.handler;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Created by LONG on 2026/1/26 at 12:49 PM.
 */
@Component
public class ShutdownLogger {

    @PreDestroy
    public void onShutdown() {
        System.out.println("Spring 已完成所有资源释放");
    }
}
