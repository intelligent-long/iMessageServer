package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/9/23 at 6:18 AM.
 */
@Validated
public class CommentBroadcastPostBody {
    @NotNull(message = "Broadcast ID 不能为空")
    @NotBlank(message = "Broadcast ID 不能为空")
    private String broadcastId;
    @NotNull(message = "正文不能为空")
    @Size(max = Constants.MAX_BROADCAST_COMMENT_TEXT_LENGTH, message = "正文不能超过" + Constants.MAX_BROADCAST_COMMENT_TEXT_LENGTH + "位")
    private String text;
    private String toCommentId;

    public CommentBroadcastPostBody() {
    }

    public CommentBroadcastPostBody(String broadcastId, String text, String toCommentId) {
        this.broadcastId = broadcastId;
        this.text = text;
        this.toCommentId = toCommentId;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public String getText() {
        return text;
    }

    public String getToCommentId() {
        return toCommentId;
    }
}
