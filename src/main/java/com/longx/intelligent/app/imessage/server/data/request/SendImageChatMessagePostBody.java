package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/5/12 at 10:53 PM.
 */
@Validated
public class SendImageChatMessagePostBody {
    @NotNull(message = "toImessageId 不能为空")
    private String toImessageId;
    @NotNull(message = "imageFileName 不能为空")
    private String imageFileName;

    public SendImageChatMessagePostBody() {
    }

    public SendImageChatMessagePostBody(String toImessageId, String imageFileName) {
        this.toImessageId = toImessageId;
        this.imageFileName = imageFileName;
    }

    public String getToImessageId() {
        return toImessageId;
    }

    public String getImageFileName() {
        return imageFileName;
    }
}
