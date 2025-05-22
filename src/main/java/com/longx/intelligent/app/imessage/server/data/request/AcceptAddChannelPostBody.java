package com.longx.intelligent.app.imessage.server.data.request;

import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/5/8 at 1:19 AM.
 */
@Validated
public class AcceptAddChannelPostBody {

    private String uuid;

    public AcceptAddChannelPostBody() {
    }

    public AcceptAddChannelPostBody(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
