package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidTagNameList;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/5/1 at 11:38 PM.
 */
@Validated
public class RequestAddChannelPostBody {
    @ValidImessageIdUser
    private String imessageIdUser;

    @Size(max = 100, message = "附加信息应不超过100位")
    private String message;

    @Size(min = 1, max = 30, message = "备注应不少于1位，不超过30位")
    private String note;

    @ValidTagNameList
    private List<String> newTagNames;

    private List<String> toAddTagIds;

    public RequestAddChannelPostBody() {
    }

    public RequestAddChannelPostBody(String imessageIdUser, String message, String note, List<String> newTagNames, List<String> toAddTagIds) {
        this.imessageIdUser = imessageIdUser;
        this.message = message;
        this.note = note;
        this.newTagNames = newTagNames;
        this.toAddTagIds = toAddTagIds;
    }

    public String getImessageIdUser() {
        return imessageIdUser;
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
}
