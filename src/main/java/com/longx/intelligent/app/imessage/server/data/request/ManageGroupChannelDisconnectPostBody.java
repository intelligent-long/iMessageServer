package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.Channel;
import com.longx.intelligent.app.imessage.server.data.validation.ValidGroupChannelIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageId;
import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;

import java.util.List;

/**
 * Created by LONG on 2025/6/11 at 4:49 AM.
 */
public class ManageGroupChannelDisconnectPostBody {
    @ValidImessageId
    private List<String> channelIds;

    public ManageGroupChannelDisconnectPostBody() {
    }

    public ManageGroupChannelDisconnectPostBody(List<String> channelIds) {
        this.channelIds = channelIds;
    }

    public List<String> getChannelIds() {
        return channelIds;
    }
}
