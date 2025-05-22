package com.longx.intelligent.app.imessage.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2025/5/8 at 5:09 AM.
 */
public record GroupChannelAddition(String type, String uuid, Channel requesterChannel, GroupChannel responderGroupChannel,
                                   String message, @JsonIgnore String note, @JsonIgnore List<String> newTagNames, @JsonIgnore List<String> toAddTagIds,
                                   Date requestTime, Date respondTime, boolean isAccepted, boolean isViewed, boolean isExpired, String inviteUuid) implements GroupChannelActivity {

    public static GroupChannelAddition create(String uuid, Channel requesterChannel, GroupChannel responderGroupChannel, String message,
                                              String note, List<String> newTagNames, List<String> toAddTagIds, Date requestTime,
                                              Date respondTime, boolean isAccepted, boolean isViewed, boolean isExpired, String inviteUuid
    ){
        return new GroupChannelAddition(
                "GroupChannelAddition",
                uuid,
                requesterChannel,
                responderGroupChannel,
                message,
                note,
                newTagNames,
                toAddTagIds,
                requestTime,
                respondTime,
                isAccepted,
                isViewed,
                isExpired,
                inviteUuid);
    }
}