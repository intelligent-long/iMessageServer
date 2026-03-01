package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.util.Logger;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Created by LONG on 2026/1/26 at 12:49 PM.
 */
@Component
public class ShutdownLogger {

    @PreDestroy
    public void onShutdown() {
        Logger.info("Spring 已完成所有资源释放");
    }
}
