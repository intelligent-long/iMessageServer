package com.longx.intelligent.app.imessage.server.data;

import java.util.List;

/**
 * Created by LONG on 2025/4/19 at 4:27 AM.
 */
public class GroupChannelTag {
    private String tagId;
    private String imessageId;
    private String name;
    private int order;
    private List<String> groupChannelIdList;

    public GroupChannelTag() {
    }

    public GroupChannelTag(String tagId, String imessageId, String name, int order, List<String> groupChannelIdList) {
        this.tagId = tagId;
        this.imessageId = imessageId;
        this.name = name;
        this.order = order;
        this.groupChannelIdList = groupChannelIdList;
    }

    public String getTagId() {
        return tagId;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public List<String> getGroupChannelIdList() {
        return groupChannelIdList;
    }
}
