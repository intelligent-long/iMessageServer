package com.longx.intelligent.app.imessage.server.data;

import java.util.List;

/**
 * Created by LONG on 2024/6/3 at 5:34 PM.
 */
public class ChannelTag {
    private String tagId;
    private String imessageId;
    private String name;
    private int order;
    private List<String> channelImessageIdList;

    public ChannelTag() {
    }

    public ChannelTag(String tagId, String imessageId, String name, int order, List<String> channelImessageIdList) {
        this.tagId = tagId;
        this.imessageId = imessageId;
        this.name = name;
        this.order = order;
        this.channelImessageIdList = channelImessageIdList;
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

    public List<String> getChannelImessageIdList() {
        return channelImessageIdList;
    }
}
