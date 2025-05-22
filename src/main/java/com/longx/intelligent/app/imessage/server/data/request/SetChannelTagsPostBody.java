package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidTagNameList;

import java.util.List;

/**
 * Created by LONG on 2024/6/6 at 1:38 AM.
 */
public class SetChannelTagsPostBody {
    private String channelImessageId;
    @ValidTagNameList
    private List<String> newTagNames;
    private List<String> toAddTagIds;
    private List<String> toRemoveTagIds;

    public SetChannelTagsPostBody() {
    }

    public SetChannelTagsPostBody(String channelImessageId, List<String> newTagNames, List<String> toAddTagIds, List<String> toRemoveTagIds) {
        this.channelImessageId = channelImessageId;
        this.newTagNames = newTagNames;
        this.toAddTagIds = toAddTagIds;
        this.toRemoveTagIds = toRemoveTagIds;
    }

    public String getChannelImessageId() {
        return channelImessageId;
    }

    public List<String> getNewTagNames() {
        return newTagNames;
    }

    public List<String> getToAddTagIds() {
        return toAddTagIds;
    }

    public List<String> getToRemoveTagIds() {
        return toRemoveTagIds;
    }
}
