package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Created by LONG on 2024/4/4 at 7:58 PM.
 */
@Controller
public class SubscribeController {

    @SubscribeMapping(StompDestinations.USER_PROFILE_UPDATE)
    public Object onSubscribeUserProfileUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHANNEL_ADDITIONS_UPDATE)
    public Object onSubscribeChannelAdditionsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE)
    public Object onSubscribeChannelAdditionsNotViewCountUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHANNELS_UPDATE)
    public Object onSubscribeChannelsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHAT_MESSAGES_UPDATE)
    public Object onSubscribeChatMessagesUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHANNEL_TAGS_UPDATE)
    public Object onSubscribeChannelTagsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.RECENT_BROADCAST_MEDIAS_UPDATE)
    public Object onSubscribeRecentBroadcastMediasUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.BROADCASTS_LIKES_UPDATE)
    public Object onSubscribeBroadcastLikesUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.BROADCASTS_COMMENTS_UPDATE)
    public Object onSubscribeBroadcastCommentsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.BROADCASTS_REPLIES_UPDATE)
    public Object onSubscribeBroadcastRepliesUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNELS_UPDATE)
    public Object onSubscribeGroupChannelsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_TAGS_UPDATE)
    public Object onSubscribeGroupChannelTagsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_ADDITIONS_UPDATE)
    public Object onSubscribeGroupChannelAdditionsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE)
    public Object onSubscribeGroupChannelAdditionsNotViewCountUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_UPDATE)
    public Object onSubscribeGroupChannelDisconnectionsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE)
    public Object onSubscribeGroupChannelDisconnectionsNotViewCountUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.CHANNEL_COLLECTIONS_UPDATE)
    public Object onSubscribeChannelCollectionsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHANNEL_COLLECTIONS_UPDATE)
    public Object onSubscribeGroupChannelCollectionsUpdate(){
        return "";
    }

    @SubscribeMapping(StompDestinations.GROUP_CHAT_MESSAGES_UPDATE)
    public Object onSubscribeGroupChatMessagesUpdate(){
        return "";
    }

}
