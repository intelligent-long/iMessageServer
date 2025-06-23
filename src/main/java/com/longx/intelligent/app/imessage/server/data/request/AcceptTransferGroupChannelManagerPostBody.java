package com.longx.intelligent.app.imessage.server.data.request;

/**
 * Created by LONG on 2025/6/22 at 12:13 AM.
 */
public class AcceptTransferGroupChannelManagerPostBody {

    private String uuid;

    public AcceptTransferGroupChannelManagerPostBody() {
    }

    public AcceptTransferGroupChannelManagerPostBody(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
