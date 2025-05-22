package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/20 at 11:20 PM.
 */
@Validated
public class SendVideoChatMessagePostBody {
    @NotNull(message = "toImessageId 不能为空")
    private String toImessageId;
    @NotNull(message = "videoFileName 不能为空")
    private String videoFileName;

    public SendVideoChatMessagePostBody() {
    }

    public SendVideoChatMessagePostBody(String toImessageId, String videoFileName) {
        this.toImessageId = toImessageId;
        this.videoFileName = videoFileName;
    }

    public String getToImessageId() {
        return toImessageId;
    }

    public String getVideoFileName() {
        return videoFileName;
    }
}
