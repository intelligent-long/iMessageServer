package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.BroadcastPermission;

/**
 * Created by LONG on 2024/10/17 at 10:08 PM.
 */
public class ChangeBroadcastPermissionPostBody {
    private BroadcastPermission broadcastPermission;

    public ChangeBroadcastPermissionPostBody() {
    }

    public ChangeBroadcastPermissionPostBody(BroadcastPermission broadcastPermission) {
        this.broadcastPermission = broadcastPermission;
    }

    public BroadcastPermission getBroadcastPermission() {
        return broadcastPermission;
    }
}
