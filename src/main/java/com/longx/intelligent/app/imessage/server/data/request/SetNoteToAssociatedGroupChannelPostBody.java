package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidChannelNote;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/2 at 1:50 AM.
 */
@Validated
public class SetNoteToAssociatedGroupChannelPostBody {
    private String groupChannelId;
    @ValidChannelNote
    private String note;

    public SetNoteToAssociatedGroupChannelPostBody() {
    }

    public SetNoteToAssociatedGroupChannelPostBody(String groupChannelId, String note) {
        this.groupChannelId = groupChannelId;
        this.note = note;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getNote() {
        return note;
    }
}
