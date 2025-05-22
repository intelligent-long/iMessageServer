package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidTagName;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/4 at 12:34 AM.
 */
@Validated
public class ChangeChannelTagNamePostBody {
    private String tagId;
    @ValidTagName
    private String name;

    public ChangeChannelTagNamePostBody() {
    }

    public ChangeChannelTagNamePostBody(String tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public String getTagId() {
        return tagId;
    }

    public String getName() {
        return name;
    }
}
