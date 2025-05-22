package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/27 at 1:59 AM.
 */
@Validated
public class SendVoiceChatMessagePostBody {
    @NotNull(message = "toImessageId 不能为空")
    private String toImessageId;

    public SendVoiceChatMessagePostBody() {
    }

    public SendVoiceChatMessagePostBody(String toImessageId) {
        this.toImessageId = toImessageId;
    }

    public String getToImessageId() {
        return toImessageId;
    }
}
