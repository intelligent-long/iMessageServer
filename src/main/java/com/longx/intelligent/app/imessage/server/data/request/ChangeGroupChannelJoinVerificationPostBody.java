package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/7 at 11:52 PM.
 */
@Validated
public class ChangeGroupChannelJoinVerificationPostBody {
    @NotNull(message = "数据不合法。")
    @NotBlank(message = "数据不合法。")
    private String groupId;
    @NotNull(message = "数据不合法。")
    private Boolean groupJoinVerificationEnabled;

    public ChangeGroupChannelJoinVerificationPostBody() {
    }

    public ChangeGroupChannelJoinVerificationPostBody(String groupId, Boolean groupJoinVerificationEnabled) {
        this.groupId = groupId;
        this.groupJoinVerificationEnabled = groupJoinVerificationEnabled;
    }

    public Boolean getGroupJoinVerificationEnabled() {
        return groupJoinVerificationEnabled;
    }

    public String getGroupId() {
        return groupId;
    }
}
