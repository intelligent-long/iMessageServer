package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/5/16 at 6:44 PM.
 */
public record GroupMessageViewed(int notViewedCount, String viewedUuid, String groupId, String from) {
}
