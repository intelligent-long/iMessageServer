package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2025/4/22 at 5:00 PM.
 */
public class GroupAvatar {
    private String hash;
    private String groupChannelId;
    private String extension;
    private Date time;

    public GroupAvatar() {
    }

    public GroupAvatar(String hash, String groupChannelId, String extension, Date time) {
        this.hash = hash;
        this.groupChannelId = groupChannelId;
        this.extension = extension;
        this.time = time;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getExtension() {
        return extension;
    }

    public Date getTime() {
        return time;
    }

    public String getHash() {
        return hash;
    }
}
