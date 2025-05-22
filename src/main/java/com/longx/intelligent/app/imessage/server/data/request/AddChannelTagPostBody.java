package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidTagName;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/3 at 4:38 PM.
 */
@Validated
public class AddChannelTagPostBody {
    @ValidTagName
    private String name;

    public AddChannelTagPostBody() {
    }

    public AddChannelTagPostBody(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
