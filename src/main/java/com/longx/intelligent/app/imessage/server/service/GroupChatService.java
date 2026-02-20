package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.GroupChatMessage;
import com.longx.intelligent.app.imessage.server.data.GroupMessageViewed;
import com.longx.intelligent.app.imessage.server.mapper.GroupChatMapper;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LONG on 2025/7/15 at 4:01 PM.
 */
@Service
public class GroupChatService {
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private GroupChannelService groupChannelService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private GroupChatMapper groupChatMapper;
    @Autowired
    @Lazy
    private GroupChatService self;

    public void sendGroupChatMessageStep1(GroupChatMessage groupChatMessage, byte[] bytes){
        String toGroupId = groupChatMessage.getTo();
        String fromUserId = groupChatMessage.getFrom();
        List<String> pendingChannelIds = new ArrayList<>();
        groupChannelService.findGroupChannelById(toGroupId, fromUserId).getGroupChannelAssociations().forEach(groupChannelAssociation -> {
            pendingChannelIds.add(groupChannelAssociation.getRequester().getImessageId());
        });
        redisOperationService.GROUP_CHAT.saveGroupChatMessage(groupChatMessage, pendingChannelIds);
        switch (groupChatMessage.getType()){
            case GroupChatMessage.TYPE_IMAGE -> {
                redisOperationService.GROUP_CHAT.saveGroupChatMessageImage(groupChatMessage, bytes);
            }
            case GroupChatMessage.TYPE_FILE -> {
                redisOperationService.GROUP_CHAT.saveGroupChatMessageFile(groupChatMessage, bytes);
            }
            case GroupChatMessage.TYPE_VIDEO -> {
                redisOperationService.GROUP_CHAT.saveGroupChatMessageVideo(groupChatMessage, bytes);
            }
            case GroupChatMessage.TYPE_VOICE -> {
                redisOperationService.GROUP_CHAT.saveGroupChatMessageVoice(groupChatMessage, bytes);
            }
        }
    }

    public void sendGroupChatMessageStep2(GroupChatMessage groupChatMessage, byte[] bytes){
        groupChannelService.findGroupChannelById(groupChatMessage.getTo(), groupChatMessage.getFrom()).getGroupChannelAssociations().forEach(groupChannelAssociation -> {
            simpMessagingTemplate.convertAndSendToUser(groupChannelAssociation.getRequester().getImessageId(), StompDestinations.GROUP_CHAT_MESSAGES_UPDATE, "");
        });

        self.saveGroupChatMessageToSql(groupChatMessage, bytes);
    }


    @Async("asyncExecutor")
    protected void saveGroupChatMessageToSql(GroupChatMessage groupChatMessage, byte[] bytes) {
        switch (groupChatMessage.getType()){
            case GroupChatMessage.TYPE_TEXT -> groupChatMapper.insertTextGroupChatMessage(groupChatMessage);
            case GroupChatMessage.TYPE_VOICE -> groupChatMapper.insertVoiceGroupChatMessage(groupChatMessage, bytes);
            case GroupChatMessage.TYPE_IMAGE -> groupChatMapper.insertImageGroupChatMessage(groupChatMessage, bytes);
            case GroupChatMessage.TYPE_VIDEO -> groupChatMapper.insertVideoGroupChatMessage(groupChatMessage, bytes);
            case GroupChatMessage.TYPE_FILE -> groupChatMapper.insertFileGroupChatMessage(groupChatMessage, bytes);
            case GroupChatMessage.TYPE_NOTICE -> {

            }
            case GroupChatMessage.TYPE_UNSEND -> groupChatMapper.insertUnsendGroupChatMessage(groupChatMessage);
            case GroupChatMessage.TYPE_MESSAGE_EXPIRED -> groupChatMapper.insertExpiredMessageGroupChatMessage(groupChatMessage);
        }
    }

    public void deleteGroupChatMessage(String receiverChannel, String uuid){
        redisOperationService.GROUP_CHAT.deleteGroupChatMessage(receiverChannel, uuid);
    }

    public List<GroupChatMessage> getAllUnviewedGroupChatMessages(String currentUserImessageId){
        List<GroupChatMessage> allGroupChatMessage = new ArrayList<>();
        groupChannelService.findAllAssociatedGroupChannels(currentUserImessageId).forEach(groupChannel -> {
            List<GroupChatMessage> oneChannelGroupChatMessages = redisOperationService.GROUP_CHAT.getAllGroupChatMessage(groupChannel.getGroupChannelId());
            oneChannelGroupChatMessages.forEach(groupChatMessage -> {
                if(redisOperationService.GROUP_CHAT.getAllPendingChannelIds(groupChatMessage).contains(currentUserImessageId)){
                    allGroupChatMessage.add(groupChatMessage);
                }
            });
        });
        return allGroupChatMessage;
    }

    public GroupMessageViewed viewMessage(String messageUuid, String currentUserImessageId){
        return redisOperationService.GROUP_CHAT.viewMessage(messageUuid, currentUserImessageId);
    }

    public void viewAllMessage(String groupChannelId, String currentUserImessageId){
        redisOperationService.GROUP_CHAT.viewAllMessage(groupChannelId, currentUserImessageId);
    }

    public GroupChatMessage findGroupChatMessage(String messageUuid){
        return groupChatMapper.findGroupChatMessage(messageUuid);
    }
}
