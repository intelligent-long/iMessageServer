package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidGroupChannelIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/3 at 9:43 PM.
 */
@Validated
public class ChangeGroupChannelIdUserPostBody {
    private final String groupChannelId;
    @ValidGroupChannelIdUser
    private final String newGroupChannelIdUser;

    public ChangeGroupChannelIdUserPostBody() {
        this(null, null);
    }

    public ChangeGroupChannelIdUserPostBody(String groupChannelId, String newGroupChannelIdUser) {
        this.groupChannelId = groupChannelId;
        this.newGroupChannelIdUser = newGroupChannelIdUser;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getNewGroupChannelIdUser() {
        return newGroupChannelIdUser;
    }
}
