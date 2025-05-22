package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2025/5/8 at 5:09 AM.
 */

public record GroupChannelInvitation(
        String type,
        String uuid,
        Channel inviter,
        Channel invitee,
        GroupChannel groupChannelInvitedTo,
        String message,
        Date requestTime,
        Date respondTime,
        boolean isAccepted,
        boolean isViewed,
        boolean isExpired,
        Type inviteType) implements GroupChannelActivity {

    public enum Type{INVITER, INVITEE}

    public static GroupChannelInvitation create(
            String uuid,
            Channel inviter,
            Channel invitee,
            GroupChannel groupChannelInvitedTo,
            String message,
            Date requestTime,
            Date respondTime,
            boolean isAccepted,
            boolean isViewed,
            boolean isExpired,
            Type inviteType) {

        return new GroupChannelInvitation(
                "GroupChannelInvitation",
                uuid,
                inviter,
                invitee,
                groupChannelInvitedTo,
                message,
                requestTime,
                respondTime,
                isAccepted,
                isViewed,
                isExpired,
                inviteType);
    }
}