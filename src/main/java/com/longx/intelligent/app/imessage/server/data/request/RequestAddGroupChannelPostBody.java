package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidGroupChannelIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidTagNameList;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Created by LONG on 2025/5/8 at 4:28 AM.
 */
public class RequestAddGroupChannelPostBody {
    @ValidGroupChannelIdUser
    private String groupChannelIdUser;

    @Size(max = 100, message = "附加信息应不超过100位")
    private String message;

    @Size(min = 1, max = 30, message = "备注应不少于1位，不超过30位")
    private String note;

    @ValidTagNameList
    private List<String> newTagNames;

    private List<String> toAddTagIds;

    private String inviteUuid;

    public RequestAddGroupChannelPostBody() {
    }

    public RequestAddGroupChannelPostBody(String groupChannelIdUser, String message, String note, List<String> newTagNames, List<String> toAddTagIds, String inviteUuid) {
        this.groupChannelIdUser = groupChannelIdUser;
        this.message = message;
        this.note = note;
        this.newTagNames = newTagNames;
        this.toAddTagIds = toAddTagIds;
        this.inviteUuid = inviteUuid;
    }

    public String getGroupChannelIdUser() {
        return groupChannelIdUser;
    }

    public String getMessage() {
        return message;
    }

    public String getNote() {
        return note;
    }

    public List<String> getNewTagNames() {
        return newTagNames;
    }

    public List<String> getToAddTagIds() {
        return toAddTagIds;
    }

    public String getInviteUuid() {
        return inviteUuid;
    }
}
