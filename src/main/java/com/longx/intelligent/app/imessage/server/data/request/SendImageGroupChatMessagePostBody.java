package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2026/2/25 at 20:52.
 */
@Validated
public class SendImageGroupChatMessagePostBody {
    @NotNull(message = "toGroupChannelId 不能为空")
    private String toGroupChannelId;
    @NotNull(message = "imageFileName 不能为空")
    private String imageFileName;

    public SendImageGroupChatMessagePostBody() {
    }

    public SendImageGroupChatMessagePostBody(String toGroupChannelId, String imageFileName) {
        this.toGroupChannelId = toGroupChannelId;
        this.imageFileName = imageFileName;
    }

    public String getToGroupChannelId() {
        return toGroupChannelId;
    }

    public String getImageFileName() {
        return imageFileName;
    }
}
