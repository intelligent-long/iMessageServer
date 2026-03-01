package com.longx.intelligent.app.imessage.server.yier;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import com.longx.intelligent.app.imessage.server.service.ChatService;
import com.longx.intelligent.app.imessage.server.service.RedisOperationService;
import com.longx.intelligent.app.imessage.server.util.Logger;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Created by LONG on 2025/4/9 at 4:39 AM.
 */
@Component
public class RedisKeyExpirationYier extends KeyExpirationEventMessageListener {
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public RedisKeyExpirationYier(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        Logger.info("过期的 Redis Key: " + expiredKey);

        if (expiredKey.startsWith("chat_message:")) {
            String[] split = expiredKey.split(":");
            String receiver = split[1];
            String uuid = split[2];
            ChatMessage chatMessage = chatService.findChatMessage(receiver, uuid);
            onChatMessageExpire(chatMessage);
        }
    }

    private void onChatMessageExpire(ChatMessage chatMessage) {
        List<ChatMessage> allChatMessage = redisOperationService.CHAT.getAllChatMessage(chatMessage.getTo());
        ChatMessage messageExpiredMessage = null;
        for (ChatMessage message : allChatMessage) {
            if(message.getType() == ChatMessage.TYPE_MESSAGE_EXPIRED && message.getExpiredMessageCount() > 0){
                messageExpiredMessage = message;
                break;
            }
        }
        if(messageExpiredMessage == null) {
            messageExpiredMessage = ChatMessage.newMessageExpired(UUID.randomUUID().toString(), chatMessage.getFrom(), chatMessage.getTo(), chatMessage.getTime(), 1);
        }else {
            messageExpiredMessage = ChatMessage.newMessageExpired(messageExpiredMessage.getUuid(), messageExpiredMessage.getFrom(), messageExpiredMessage.getTo(), chatMessage.getTime(), messageExpiredMessage.getExpiredMessageCount() + 1);
        }
        redisOperationService.CHAT.saveChatMessage(messageExpiredMessage);
        chatService.sendChatMessageStep1(messageExpiredMessage, null);
        chatService.sendChatMessageStep2(messageExpiredMessage, null);
    }
}
