package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2025/7/11 at 11:24 PM.
 */
@Validated
public class SendTextGroupChatMessagePostBody {
    private String toGroupId;
    @Size(max = 1000, message = "消息不能超过 1000 位")
    private String text;

    public SendTextGroupChatMessagePostBody() {
    }

    public SendTextGroupChatMessagePostBody(String toGroupId, String text) {
        this.toGroupId = toGroupId;
        this.text = text;
    }

    public String getToGroupId() {
        return toGroupId;
    }

    public String getText() {
        return text;
    }
}
