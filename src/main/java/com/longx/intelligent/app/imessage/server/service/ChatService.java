package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import com.longx.intelligent.app.imessage.server.data.MessageViewed;
import com.longx.intelligent.app.imessage.server.mapper.ChatMapper;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by LONG on 2024/5/12 at 4:53 AM.
 */
@Service
public class ChatService {
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ChatMapper chatMapper;

    public void sendChatMessageStep1(ChatMessage chatMessage, byte[] bytes){
        redisOperationService.CHAT.saveChatMessage(chatMessage);
        switch (chatMessage.getType()){
            case ChatMessage.TYPE_IMAGE -> {
                redisOperationService.CHAT.saveChatMessageImage(chatMessage, bytes);
            }
            case ChatMessage.TYPE_FILE -> {
                redisOperationService.CHAT.saveChatMessageFile(chatMessage, bytes);
            }
            case ChatMessage.TYPE_VIDEO -> {
                redisOperationService.CHAT.saveChatMessageVideo(chatMessage, bytes);
            }
            case ChatMessage.TYPE_VOICE -> {
                redisOperationService.CHAT.saveChatMessageVoice(chatMessage, bytes);
            }
        }
    }

    public void sendChatMessageStep2(ChatMessage chatMessage, byte[] bytes){
        simpMessagingTemplate.convertAndSendToUser(chatMessage.getTo(), StompDestinations.CHAT_MESSAGES_UPDATE, "");

        switch (chatMessage.getType()){
            case ChatMessage.TYPE_TEXT -> chatMapper.insertTextChatMessage(chatMessage);
            case ChatMessage.TYPE_VOICE -> chatMapper.insertVoiceChatMessage(chatMessage, bytes);
            case ChatMessage.TYPE_IMAGE -> chatMapper.insertImageChatMessage(chatMessage, bytes);
            case ChatMessage.TYPE_VIDEO -> chatMapper.insertVideoChatMessage(chatMessage, bytes);
            case ChatMessage.TYPE_FILE -> chatMapper.insertFileChatMessage(chatMessage, bytes);
            case ChatMessage.TYPE_NOTICE -> {

            }
            case ChatMessage.TYPE_UNSEND -> chatMapper.insertUnsendChatMessage(chatMessage);
            case ChatMessage.TYPE_MESSAGE_EXPIRED -> chatMapper.insertExpiredMessageChatMessage(chatMessage);
        }
    }

    public void deleteChatMessage(String receiver, String uuid){
        redisOperationService.CHAT.deleteChatMessage(receiver, uuid);
    }

    public List<ChatMessage> getAllUnviewedChatMessages(String currentUserImessageId){
        return redisOperationService.CHAT.getAllChatMessage(currentUserImessageId);
    }

    public Object[] getNewChatMessageImage(String imageId){
        return redisOperationService.CHAT.getChatMessageImage(imageId);
    }

    public Object[] getNewChatMessageFile(String fileId){
        return redisOperationService.CHAT.getChatMessageFile(fileId);
    }

    public Object[] getNewChatMessageVideo(String videoId){
        return redisOperationService.CHAT.getChatMessageVideo(videoId);
    }

    public Object[] getNewChatMessageVoice(String voiceId){
        return redisOperationService.CHAT.getChatMessageVoice(voiceId);
    }

    public MessageViewed viewMessage(String currentUserImessageId, String messageUuid){
        return redisOperationService.CHAT.viewMessage(currentUserImessageId, messageUuid);
    }

    public ChatMessage getChatMessage(String receiver, String uuid){
        return redisOperationService.CHAT.getChatMessage(receiver, uuid);
    }

    public ChatMessage findChatMessage(String receiver, String uuid){
        return chatMapper.findChatMessage(receiver, uuid);
    }

}
