package com.longx.intelligent.app.imessage.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.longx.intelligent.app.imessage.server.context.SpringContextHolder;
import com.longx.intelligent.app.imessage.server.ui.LaunchUi;
import com.longx.intelligent.app.imessage.server.ui.LogUi;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import com.longx.intelligent.app.imessage.server.util.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.io.File;
import java.util.Map;

@SpringBootApplication
@EnableRedisIndexedHttpSession(maxInactiveIntervalInSeconds = 30 * 24 * 60 * 60)
@EnableWebSocket
@EnableWebSocketMessageBroker
@EnableScheduling
public class IMessageServerApplication {

    public static void main(String[] args) {
        String configFile = getConfigArg(args);
        if (configFile != null) {
            startFromConfig(configFile, args);
            return;
        }
        System.setProperty("apple.awt.UIElement", "true");
        FlatLaf.setUseNativeWindowDecorations(false);
        FlatLightLaf.setup();
        startWithGui(args);
    }

    private static void startFromConfig(String configFile, String[] args) {
        try {
            Map<String, String> props =
                    JsonUtil.loadObjectFromJsonFile(
                            new File(configFile),
                            new TypeReference<>() {}
                    );
            props.forEach(System::setProperty);
            new SpringApplicationBuilder(IMessageServerApplication.class)
                    .profiles("cli")
                    .properties(Map.of("spring.config.name", "gui-application"))
                    .run(args);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.err("配置文件加载失败: " + configFile);
        }
    }

    private static void startWithGui(String[] args) {
        LaunchUi launchUi = LaunchUi.getInstance();
        launchUi.setLaunchAction(() -> {
            launchUi.getProperties().forEach(System::setProperty);
            launchUi.close();
            LogUi.getInstance().show();
            new Thread(() -> {
                ConfigurableApplicationContext context =
                        new SpringApplicationBuilder(IMessageServerApplication.class)
                                .properties(Map.of("spring.config.name", "gui-application"))
                                .profiles("gui")
                                .run(args);
                SpringContextHolder.setContext(context);
            }).start();
        });
        launchUi.show();
    }

    private static String getConfigArg(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-cli".equalsIgnoreCase(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}
