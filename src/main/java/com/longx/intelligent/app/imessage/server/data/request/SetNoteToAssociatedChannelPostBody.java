package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidChannelNote;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/2 at 1:50 AM.
 */
@Validated
public class SetNoteToAssociatedChannelPostBody {
    private String channelImessageId;
    @ValidChannelNote
    private String note;

    public SetNoteToAssociatedChannelPostBody() {
    }

    public SetNoteToAssociatedChannelPostBody(String channelImessageId, String note) {
        this.channelImessageId = channelImessageId;
        this.note = note;
    }

    public String getChannelImessageId() {
        return channelImessageId;
    }

    public String getNote() {
        return note;
    }
}
