package com.longx.intelligent.app.imessage.server.data.request;

import java.util.List;

/**
 * Created by LONG on 2025/6/26 at 4:11 AM.
 */
public class RemoveGroupChannelCollectionPostBody {
    private List<String> uuids;

    public RemoveGroupChannelCollectionPostBody() {
    }

    public RemoveGroupChannelCollectionPostBody(List<String> uuids) {
        this.uuids = uuids;
    }

    public List<String> getUuids() {
        return uuids;
    }
}
