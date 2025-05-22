package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidTagNameList;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2025/4/14 at 1:46 PM.
 */
@Validated
public class CreateGroupChannelPostBody {

    private String name;

    @Size(min = 1, max = 30, message = "频道备注应不少于1位，不超过30位")
    private String note;

    @ValidTagNameList
    private List<String> newTagNames;

    private List<String> toAddTagIds;

    public CreateGroupChannelPostBody() {
    }

    public CreateGroupChannelPostBody(String name, String note, List<String> newTagNames, List<String> toAddTagIds) {
        this.name = name;
        this.note = note;
        this.newTagNames = newTagNames;
        this.toAddTagIds = toAddTagIds;
    }

    public String getName() {
        return name;
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
