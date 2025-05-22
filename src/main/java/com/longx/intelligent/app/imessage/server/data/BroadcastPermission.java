package com.longx.intelligent.app.imessage.server.data;

import java.util.Set;

/**
 * Created by LONG on 2024/10/17 at 4:21 PM.
 */
public class BroadcastPermission {
    public static final int PUBLIC = 0;
    public static final int PRIVATE = 1;
    public static final int CONNECTED_CHANNEL_CIRCLE = 2;

    private String broadcastId;
    private int permission;
    private Set<String> excludeConnectedChannels;

    public BroadcastPermission() {
    }

    public BroadcastPermission(String broadcastId, int permission, Set<String> excludeConnectedChannels) {
        this.broadcastId = broadcastId;
        this.permission = permission;
        this.excludeConnectedChannels = excludeConnectedChannels;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public int getPermission() {
        return permission;
    }

    public Set<String> getExcludeConnectedChannels() {
        return excludeConnectedChannels;
    }
}
