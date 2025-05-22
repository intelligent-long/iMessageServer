package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidTagNameList;

import java.util.List;

/**
 * Created by LONG on 2024/6/6 at 1:38 AM.
 */
public class SetGroupChannelTagsPostBody {
    private String groupChannelId;
    @ValidTagNameList
    private List<String> newTagNames;
    private List<String> toAddTagIds;
    private List<String> toRemoveTagIds;

    public SetGroupChannelTagsPostBody() {
    }

    public SetGroupChannelTagsPostBody(String groupChannelId, List<String> newTagNames, List<String> toAddTagIds, List<String> toRemoveTagIds) {
        this.groupChannelId = groupChannelId;
        this.newTagNames = newTagNames;
        this.toAddTagIds = toAddTagIds;
        this.toRemoveTagIds = toRemoveTagIds;
    }

    public String getGroupChannelId() {
        return groupChannelId;
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
