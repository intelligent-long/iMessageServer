package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import com.longx.intelligent.app.imessage.server.data.GroupChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by LONG on 2025/7/15 at 4:02 PM.
 */
@Mapper
public interface GroupChatMapper {

    void insertTextGroupChatMessage(GroupChatMessage groupChatMessage);

    void insertImageGroupChatMessage(GroupChatMessage groupChatMessage, byte[] image);

    void insertFileGroupChatMessage(GroupChatMessage groupChatMessage, byte[] file);

    void insertVideoGroupChatMessage(GroupChatMessage groupChatMessage, byte[] video);

    void insertVoiceGroupChatMessage(GroupChatMessage groupChatMessage, byte[] voice);

    void insertUnsendGroupChatMessage(GroupChatMessage groupChatMessage);

    void insertExpiredMessageGroupChatMessage(GroupChatMessage groupChatMessage);

    GroupChatMessage findGroupChatMessage(String messageUuid);
}
