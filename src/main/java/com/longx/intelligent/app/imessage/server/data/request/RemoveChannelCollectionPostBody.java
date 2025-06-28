package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.ChannelCollectionItem;

import java.util.List;

/**
 * Created by LONG on 2025/6/26 at 4:11 AM.
 */
public class RemoveChannelCollectionPostBody {
    private List<String> uuids;

    public RemoveChannelCollectionPostBody() {
    }

    public RemoveChannelCollectionPostBody(List<String> uuids) {
        this.uuids = uuids;
    }

    public List<String> getUuids() {
        return uuids;
    }
}
