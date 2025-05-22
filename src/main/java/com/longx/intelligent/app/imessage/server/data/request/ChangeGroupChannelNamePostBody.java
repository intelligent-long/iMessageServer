package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidGroupName;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/7 at 11:52 PM.
 */
@Validated
public class ChangeGroupChannelNamePostBody {

    private final String groupId;
    @ValidGroupName
    private final String newGroupName;

    public ChangeGroupChannelNamePostBody() {
        this(null, null);
    }

    public ChangeGroupChannelNamePostBody(String groupId, String newGroupName) {
        this.groupId = groupId;
        this.newGroupName = newGroupName;
    }

    public String getNewGroupName() {
        return newGroupName;
    }

    public String getGroupId() {
        return groupId;
    }
}
