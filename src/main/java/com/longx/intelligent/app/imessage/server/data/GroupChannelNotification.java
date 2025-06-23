package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2025/6/14 at 7:09 AM.
 */
public class GroupChannelNotification {
    public enum Type{ACTIVE_DISCONNECT, PASSIVE_DISCONNECT, INVITE_TRANSFER_MANAGER, ACCEPTED_TRANSFER_MANAGER}
    private String uuid;
    private Type type;
    private String groupChannelId;
    private String channelId;
    private Boolean passive;
    private String byWhom;
    private Date time;
    private boolean isViewed;

    public GroupChannelNotification() {
    }

    public GroupChannelNotification(String uuid, Type type, String groupChannelId, String channelId, Boolean passive, String byWhom, Date time, boolean isViewed) {
        this.uuid = uuid;
        this.type = type;
        this.groupChannelId = groupChannelId;
        this.channelId = channelId;
        this.passive = passive;
        this.byWhom = byWhom;
        this.time = time;
        this.isViewed = isViewed;
    }

    public String getUuid() {
        return uuid;
    }

    public Type getType() {
        return type;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public Boolean isPassive() {
        return passive;
    }

    public String getByWhom() {
        return byWhom;
    }

    public Date getTime() {
        return time;
    }

    public boolean isViewed() {
        return isViewed;
    }

    @Override
    public String toString() {
        return "GroupChannelNotification{" +
                "uuid='" + uuid + '\'' +
                ", type=" + type +
                ", groupChannelId='" + groupChannelId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", passive=" + passive +
                ", byWhom='" + byWhom + '\'' +
                ", time=" + time +
                ", isViewed=" + isViewed +
                '}';
    }
}
