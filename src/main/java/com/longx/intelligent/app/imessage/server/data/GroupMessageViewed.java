package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/5/16 at 6:44 PM.
 */
public class GroupMessageViewed {
    int totalNotViewedCount;
    int currentNotViewedCount;
    private String viewedUuid;
    private String groupId;
    private String from;

    public GroupMessageViewed() {
    }

    public GroupMessageViewed(int totalNotViewedCount, int currentNotViewedCount, String viewedUuid, String groupId, String from) {
        this.totalNotViewedCount = totalNotViewedCount;
        this.currentNotViewedCount = currentNotViewedCount;
        this.viewedUuid = viewedUuid;
        this.groupId = groupId;
        this.from = from;
    }

    public int getTotalNotViewedCount() {
        return totalNotViewedCount;
    }

    public void setTotalNotViewedCount(int totalNotViewedCount) {
        this.totalNotViewedCount = totalNotViewedCount;
    }

    public int getCurrentNotViewedCount() {
        return currentNotViewedCount;
    }

    public void setCurrentNotViewedCount(int currentNotViewedCount) {
        this.currentNotViewedCount = currentNotViewedCount;
    }

    public String getViewedUuid() {
        return viewedUuid;
    }

    public void setViewedUuid(String viewedUuid) {
        this.viewedUuid = viewedUuid;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "GroupMessageViewed{" +
                "totalNotViewedCount=" + totalNotViewedCount +
                ", currentNotViewedCount=" + currentNotViewedCount +
                ", viewedUuid='" + viewedUuid + '\'' +
                ", groupId='" + groupId + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}