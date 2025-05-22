package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/6/4 at 5:22 PM.
 */
@Validated
public class RemoveChannelsOfTagPostBody {
    private String tagId;

    @Size(min = 1, message = "列表不能为空元素")
    private List<String> channelImessageIdList;

    public RemoveChannelsOfTagPostBody() {
    }

    public RemoveChannelsOfTagPostBody(String tagId, List<String> channelImessageIdList) {
        this.tagId = tagId;
        this.channelImessageIdList = channelImessageIdList;
    }

    public String getTagId() {
        return tagId;
    }

    public List<String> getChannelImessageIdList() {
        return channelImessageIdList;
    }
}
