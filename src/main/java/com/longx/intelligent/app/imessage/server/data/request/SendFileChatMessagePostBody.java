package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/6/10 at 7:12 PM.
 */
@Validated
public class SendFileChatMessagePostBody {
    @NotNull(message = "toImessageId 不能为空")
    private String toImessageId;
    @NotNull(message = "fileName 不能为空")
    private String fileName;

    public SendFileChatMessagePostBody() {
    }

    public SendFileChatMessagePostBody(String toImessageId, String fileName) {
        this.toImessageId = toImessageId;
        this.fileName = fileName;
    }

    public String getToImessageId() {
        return toImessageId;
    }

    public String getFileName() {
        return fileName;
    }
}
