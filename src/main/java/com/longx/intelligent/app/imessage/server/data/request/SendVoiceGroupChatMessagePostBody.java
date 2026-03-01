package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/27 at 1:59 AM.
 */
@Validated
public class SendVoiceGroupChatMessagePostBody {
    @NotNull(message = "toGroupChannelId 不能为空")
    private String toGroupChannelId;

    public SendVoiceGroupChatMessagePostBody() {
    }

    public SendVoiceGroupChatMessagePostBody(String toGroupChannelId) {
        this.toGroupChannelId = toGroupChannelId;
    }

    public String getToGroupChannelId() {
        return toGroupChannelId;
    }
}
