package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.Channel;
import com.longx.intelligent.app.imessage.server.data.GroupChannel;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2025/5/14 at 1:59 AM.
 */
@Validated
public class InviteJoinGroupChannelPostBody {
    private String message;
    @NotNull
    private Channel invitee;
    @NotNull
    private GroupChannel groupChannelInvitedTo;

    public InviteJoinGroupChannelPostBody() {
    }

    public InviteJoinGroupChannelPostBody(String message, Channel invitee, GroupChannel groupChannelInvitedTo) {
        this.message = message;
        this.invitee = invitee;
        this.groupChannelInvitedTo = groupChannelInvitedTo;
    }

    public String getMessage() {
        return message;
    }

    public Channel getInvitee() {
        return invitee;
    }

    public GroupChannel getGroupChannelInvitedTo() {
        return groupChannelInvitedTo;
    }
}
