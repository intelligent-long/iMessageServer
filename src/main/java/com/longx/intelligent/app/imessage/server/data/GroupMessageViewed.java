package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/5/16 at 6:44 PM.
 */
public class GroupMessageViewed {
    private int notViewedCount;
    private String viewedUuid;
    private String groupId;
    private String from;

    public GroupMessageViewed() {
    }

    public GroupMessageViewed(int notViewedCount, String viewedUuid, String groupId, String from) {
        this.notViewedCount = notViewedCount;
        this.viewedUuid = viewedUuid;
        this.groupId = groupId;
        this.from = from;
    }

    public int getNotViewedCount() {
        return notViewedCount;
    }

    public void setNotViewedCount(int notViewedCount) {
        this.notViewedCount = notViewedCount;
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
                "notViewedCount=" + notViewedCount +
                ", viewedUuid='" + viewedUuid + '\'' +
                ", groupId='" + groupId + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}