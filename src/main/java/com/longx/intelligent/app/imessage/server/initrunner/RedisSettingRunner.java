package com.longx.intelligent.app.imessage.server.initrunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by LONG on 2025/4/9 at 4:29 AM.
 */
@Component
public class RedisSettingRunner implements CommandLineRunner {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(String... args) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        Properties props = connection.getConfig("notify-keyspace-events");
        String notifyOptions = props.getProperty("notify-keyspace-events");

        if (notifyOptions == null || !notifyOptions.contains("E") || !notifyOptions.contains("x")) {
            connection.setConfig("notify-keyspace-events", "Ex");
//            Logger.info("已设置 notify-keyspace-events 为 Ex");
        } else {
//            Logger.info("当前 notify-keyspace-events: " + notifyOptions);
        }
    }
}

