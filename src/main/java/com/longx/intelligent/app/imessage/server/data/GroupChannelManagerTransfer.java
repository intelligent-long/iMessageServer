package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2025/6/20 at 9:26 AM.
 */
public record GroupChannelManagerTransfer(String type, String uuid, Channel inviter, Channel transferToChannel, GroupChannel toTransferGroupChannel, String message,
                                          Date inviterTime, Date respondTime, boolean isAccepted, boolean isViewed, boolean isExpired) {

    public static GroupChannelManagerTransfer create(String uuid, Channel inviter, Channel transferToChannel, GroupChannel toTransferGroupChannel, String message,
                                              Date inviterTime, Date respondTime, boolean isAccepted, boolean isViewed, boolean isExpired
    ){
        return new GroupChannelManagerTransfer(
                "GroupChannelManagerTransfer",
                uuid,
                inviter,
                transferToChannel,
                toTransferGroupChannel,
                message,
                inviterTime,
                respondTime,
                isAccepted,
                isViewed,
                isExpired);
    }
}