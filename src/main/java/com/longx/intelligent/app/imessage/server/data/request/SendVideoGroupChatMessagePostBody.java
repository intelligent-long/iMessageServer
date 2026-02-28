package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;

/**
 * Created by LONG on 2026/2/28 at 23:21.
 */
public class SendVideoGroupChatMessagePostBody {

    @NotNull(message = "toGroupChannelId 不能为空")
    private String toGroupChannelId;
    @NotNull(message = "videoFileName 不能为空")
    private String videoFileName;

    public SendVideoGroupChatMessagePostBody() {
    }

    public SendVideoGroupChatMessagePostBody(String toGroupChannelId, String videoFileName) {
        this.toGroupChannelId = toGroupChannelId;
        this.videoFileName = videoFileName;
    }

    public String getToGroupChannelId() {
        return toGroupChannelId;
    }

    public String getVideoFileName() {
        return videoFileName;
    }
}
