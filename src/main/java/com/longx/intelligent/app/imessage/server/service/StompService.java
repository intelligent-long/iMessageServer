package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.Channel;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by LONG on 2024/4/4 at 7:55 PM.
 */
@Service
public class StompService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private GroupChannelService groupChannelService;

    public void sendUserInfoUpdate(String currentUserImessageId){
        simpMessagingTemplate.convertAndSendToUser(currentUserImessageId, StompDestinations.USER_PROFILE_UPDATE, "");
        channelService.findAllChannelAssociations(currentUserImessageId).forEach(channelAssociation -> {
            simpMessagingTemplate.convertAndSendToUser(channelAssociation.getChannel().getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
        });
    }

    public void sendGroupChannelUpdate(String currentUserImessageId, String groupChannelId){
//        simpMessagingTemplate.convertAndSendToUser(currentUserImessageId, StompDestinations.GROUP_CHANNEL_UPDATE, groupChannelId);
//        List<Channel> allAssociatedChannels = groupChannelService.findGroupAllAssociatedChannels(groupChannelId, currentUserImessageId);
//        allAssociatedChannels.forEach(associatedChannel -> {
//            simpMessagingTemplate.convertAndSendToUser(associatedChannel.getImessageId(), StompDestinations.GROUP_CHANNEL_UPDATE, groupChannelId);
//        });
    }
}
