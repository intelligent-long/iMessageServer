package com.longx.intelligent.app.imessage.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by LONG on 2024/3/31 at 8:39 PM.
 */
@Component
@PropertySource(value = "classpath:application.yml")
@ConfigurationProperties(prefix = "imessage")
public class ImessageConfig {
    private int lowestAllowClientVersion;
    private int currentClientVersion;

    public void setLowestAllowClientVersion(int lowestAllowClientVersion) {
        this.lowestAllowClientVersion = lowestAllowClientVersion;
    }

    public int getLowestAllowClientVersion() {
        return lowestAllowClientVersion;
    }

    public int getCurrentClientVersion() {
        return currentClientVersion;
    }

    public void setCurrentClientVersion(int currentClientVersion) {
        this.currentClientVersion = currentClientVersion;
    }
}
