package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/5/12 at 10:53 PM.
 */
@Validated
public class SendTextChatMessagePostBody {
    private String toImessageId;
    @Size(max = 1000, message = "消息不能超过 1000 位")
    private String text;

    public SendTextChatMessagePostBody() {
    }

    public SendTextChatMessagePostBody(String toImessageId, String text) {
        this.toImessageId = toImessageId;
        this.text = text;
    }

    public String getToImessageId() {
        return toImessageId;
    }

    public String getText() {
        return text;
    }
}
