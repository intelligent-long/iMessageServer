package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2024/5/2 at 1:11 AM.
 */
public record ChannelAddition(
        String uuid, Channel requesterChannel, Channel responderChannel,
        String message, String note, List<String> newTagNames, List<String> toAddTagIds,
        Date requestTime, Date respondTime, boolean isAccepted, boolean isViewed, boolean isExpired) {
}
