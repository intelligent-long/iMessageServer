package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2024/7/28 at 2:13 PM.
 */
public class Broadcast {
    private String broadcastId;
    private String imessageId;
    private String channelName;
    private String channelAvatarHash;
    private Date time;
    private Date lastEditTime;
    private String text;
    private List<BroadcastMedia> broadcastMedias;
    private boolean liked;
    private int likeCount;
    private boolean commented;
    private int commentCount;
    private BroadcastPermission broadcastPermission;

    public Broadcast() {
    }

    public Broadcast(String broadcastId, String imessageId, String channelName, String channelAvatarHash, Date time, Date lastEditTime, String text, List<BroadcastMedia> broadcastMedias, boolean liked, int likeCount, boolean commented, int commentCount, BroadcastPermission broadcastPermission) {
        this.broadcastId = broadcastId;
        this.imessageId = imessageId;
        this.channelName = channelName;
        this.channelAvatarHash = channelAvatarHash;
        this.time = time;
        this.lastEditTime = lastEditTime;
        this.text = text;
        this.broadcastMedias = broadcastMedias;
        this.liked = liked;
        this.likeCount = likeCount;
        this.commented = commented;
        this.commentCount = commentCount;
        this.broadcastPermission = broadcastPermission;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelAvatarHash() {
        return channelAvatarHash;
    }

    public Date getTime() {
        return time;
    }

    public Date getLastEditTime() {
        return lastEditTime;
    }

    public String getText() {
        return text;
    }

    public List<BroadcastMedia> getBroadcastMedias() {
        return broadcastMedias;
    }

    public void setBroadcastMedias(List<BroadcastMedia> broadcastMedias) {
        this.broadcastMedias = broadcastMedias;
    }

    public boolean isLiked() {
        return liked;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isCommented() {
        return commented;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public BroadcastPermission getBroadcastPermission() {
        return broadcastPermission;
    }

    public void setBroadcastPermission(BroadcastPermission broadcastPermission) {
        this.broadcastPermission = broadcastPermission;
    }
}
