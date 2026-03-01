package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/10 at 7:12 PM.
 */
@Validated
public class SendFileGroupChatMessagePostBody {
    @NotNull(message = "toGroupChannelId 不能为空")
    private String toGroupChannelId;
    @NotNull(message = "fileName 不能为空")
    private String fileName;

    public SendFileGroupChatMessagePostBody() {
    }

    public SendFileGroupChatMessagePostBody(String toGroupChannelId, String fileName) {
        this.toGroupChannelId = toGroupChannelId;
        this.fileName = fileName;
    }

    public String getToGroupChannelId() {
        return toGroupChannelId;
    }

    public String getFileName() {
        return fileName;
    }
}
