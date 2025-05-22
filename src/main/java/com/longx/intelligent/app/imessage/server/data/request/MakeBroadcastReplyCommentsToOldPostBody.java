package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/9/20 at 1:58 AM.
 */
@Validated
public class MakeBroadcastReplyCommentsToOldPostBody {
    @NotNull(message = "Comment ID 不能为空")
    private List<String> replyCommentIds;

    public MakeBroadcastReplyCommentsToOldPostBody() {
    }

    public MakeBroadcastReplyCommentsToOldPostBody(List<String> replyCommentIds) {
        this.replyCommentIds = replyCommentIds;
    }

    public List<String> getReplyCommentIds() {
        return replyCommentIds;
    }
}
