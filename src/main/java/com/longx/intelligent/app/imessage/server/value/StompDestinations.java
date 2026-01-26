package com.longx.intelligent.app.imessage.server.value;

/**
 * Created by LONG on 2024/3/31 at 7:56 PM.
 */
public class StompDestinations {
    public static final String PREFIX_TOPIC = "/topic";
    public static final String PREFIX_QUEUE = "/queue";
    public static final String PREFIX_APP = "/app";
    public static final String PREFIX_USER = "/user";

    public static final String USER_PROFILE_UPDATE = PREFIX_QUEUE + "/user_profile_update";
    public static final String CHANNEL_ADDITIONS_UPDATE = PREFIX_QUEUE + "/channel_additions_update";
    public static final String CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE = PREFIX_QUEUE + "/channel_additions_not_view_count_update";
    public static final String CHANNELS_UPDATE = PREFIX_QUEUE + "/channels_update";
    public static final String CHAT_MESSAGES_UPDATE = PREFIX_QUEUE + "/chat_messages_update";
    public static final String GROUP_CHAT_MESSAGES_UPDATE = PREFIX_QUEUE + "/group_chat_messages_update";
    public static final String CHANNEL_TAGS_UPDATE = PREFIX_QUEUE + "/channel_tags_update";
    public static final String BROADCASTS_NEWS_UPDATE = PREFIX_QUEUE + "/broadcasts_news_update";
    public static final String RECENT_BROADCAST_MEDIAS_UPDATE = PREFIX_QUEUE + "/recent_broadcast_medias_update";
    public static final String BROADCASTS_LIKES_UPDATE = PREFIX_QUEUE + "/broadcasts_likes_update";
    public static final String BROADCASTS_COMMENTS_UPDATE = PREFIX_QUEUE + "/broadcasts_comments_update";
    public static final String BROADCASTS_REPLIES_UPDATE = PREFIX_QUEUE + "/broadcasts_replies_update";
    public static final String GROUP_CHANNELS_UPDATE = PREFIX_QUEUE + "/group_channels_update";
    public static final String GROUP_CHANNEL_UPDATE = PREFIX_QUEUE + "/group_channel_update";
    public static final String GROUP_CHANNEL_TAGS_UPDATE = PREFIX_QUEUE + "/group_channel_tags_update";
    public static final String GROUP_CHANNEL_ADDITIONS_UPDATE = PREFIX_QUEUE + "/group_channel_additions_update";
    public static final String GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE = PREFIX_QUEUE + "/group_channel_additions_not_view_count_update";
    public static final String GROUP_CHANNEL_NOTIFICATIONS_UPDATE = PREFIX_QUEUE + "/group_channel_notifications_update";
    public static final String GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE = PREFIX_QUEUE + "/group_channel_notifications_not_view_count_update";
//    public static final String GROUP_CHANNEL_MANAGER_TRANSFER_UPDATE = PREFIX_QUEUE + "/group_channel_manager_transfer_update";
//    public static final String GROUP_CHANNEL_MANAGER_TRANSFER_NOT_VIEW_COUNT_UPDATE = PREFIX_QUEUE + "/group_channel_manager_transfer_not_view_count_update";
    public static final String CHANNEL_COLLECTIONS_UPDATE = PREFIX_QUEUE + "/channel_collections_update";
    public static final String GROUP_CHANNEL_COLLECTIONS_UPDATE = PREFIX_QUEUE + "/group_channel_collections_update";
}
