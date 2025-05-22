package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/6/4 at 5:22 PM.
 */
@Validated
public class AddGroupChannelsToTagPostBody {
    private String tagId;

    @Size(min = 1, message = "列表不能为空元素")
    private List<String> groupChannelIdList;

    public AddGroupChannelsToTagPostBody() {
    }

    public AddGroupChannelsToTagPostBody(String tagId, List<String> groupChannelIdList) {
        this.tagId = tagId;
        this.groupChannelIdList = groupChannelIdList;
    }

    public String getTagId() {
        return tagId;
    }

    public List<String> getGroupChannelIdList() {
        return groupChannelIdList;
    }
}
