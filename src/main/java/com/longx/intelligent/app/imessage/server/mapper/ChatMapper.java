package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by LONG on 2024/5/13 at 12:38 AM.
 */
@Mapper
public interface ChatMapper {

    void insertTextChatMessage(ChatMessage chatMessage);

    void insertImageChatMessage(ChatMessage chatMessage, byte[] image);

    void insertFileChatMessage(ChatMessage chatMessage, byte[] file);

    void insertVideoChatMessage(ChatMessage chatMessage, byte[] video);

    void insertVoiceChatMessage(ChatMessage chatMessage, byte[] voice);

    void insertUnsendChatMessage(ChatMessage chatMessage);

    void insertExpiredMessageChatMessage(ChatMessage chatMessage);

    ChatMessage findChatMessage(String receiver, String uuid);
}
