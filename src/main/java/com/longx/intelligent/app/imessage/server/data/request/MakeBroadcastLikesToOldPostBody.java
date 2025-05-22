package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/9/20 at 1:58 AM.
 */
@Validated
public class MakeBroadcastLikesToOldPostBody {
    @NotNull(message = "Like ID 不能为空")
    private List<String> likeIds;

    public MakeBroadcastLikesToOldPostBody() {
    }

    public MakeBroadcastLikesToOldPostBody(List<String> likeIds) {
        this.likeIds = likeIds;
    }

    public List<String> getLikeIds() {
        return likeIds;
    }
}
