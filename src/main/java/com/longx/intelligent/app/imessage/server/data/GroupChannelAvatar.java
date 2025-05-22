package com.longx.intelligent.app.imessage.server.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by LONG on 2024/4/29 at 9:28 PM.
 */
public class GroupChannelAvatar implements Serializable {
    private String hash;
    private String groupChannelId;
    private String extension;
    private Date time;

    public GroupChannelAvatar() {
    }

    public GroupChannelAvatar(String hash, String groupChannelId, String extension, Date time) {
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
