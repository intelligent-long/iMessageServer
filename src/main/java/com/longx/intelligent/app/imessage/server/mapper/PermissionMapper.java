package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.BroadcastChannelPermission;
import com.longx.intelligent.app.imessage.server.data.BroadcastPermission;
import com.longx.intelligent.app.imessage.server.data.ChatMessageAllow;
import com.longx.intelligent.app.imessage.server.data.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * Created by LONG on 2024/6/7 at 4:41 PM.
 */
@Mapper
public interface PermissionMapper {
    int insertOrUpdateUserProfileVisibility(String imessageId, boolean emailVisible, boolean sexVisible, boolean regionVisible);

    User.UserProfileVisibility findUserProfileVisibilityByImessageId(String imessageId);

    User.UserProfileVisibility findUserProfileVisibilityByImessageIdUser(String imessageIdUser);

    User.UserProfileVisibility findUserProfileVisibilityByEmail(String email);

    int insertOrUpdateWaysToFindMe(String imessageId, boolean byImessageId, boolean byEmail);

    User.WaysToFindMe findWaysToFindMeByImessageId(String imessageId);

    User.WaysToFindMe findWaysToFindMeByImessageIdUser(String imessageIdUser);

    User.WaysToFindMe findWaysToFindMeByEmail(String email);

    int insertOrUpdateAllowChatMessage(String imessageId, String channelImessageId, ChatMessageAllow chatMessageAllow);

    ChatMessageAllow findChatMessageAllow(String imessageId, String channelImessageId);

    int deleteChatMessageAllow(String imessageId, String channelImessageId);

    BroadcastChannelPermission findBroadcastChannelPermission(String imessageId);

    int insertOrUpdateBroadcastChannelPermission(String imessageId, int permission);

    int insertBroadcastChannelPermissionExcludeConnectedChannels(String imessageId, Set<String> excludeConnectedChannels);

    void deleteAllBroadcastChannelPermissionExcludeConnectedChannels(String imessageId);

    Set<String> findExcludeBroadcastChannels(String imessageId);

    int insertExcludeBroadcastChannels(String imessageId, Set<String> excludeBroadcastChannelIds);

    void deleteAllExcludeBroadcastChannels(String imessageId);

    int insertOrUpdateBroadcastPermission(String broadcastId, int permission);

    int insertBroadcastPermissionExcludeConnectedChannels(String broadcastId, Set<String> excludeConnectedChannels);

    void deleteAllBroadcastPermissionExcludeConnectedChannels(String broadcastId);

    BroadcastPermission findBroadcastPermission(String broadcastId);

    void deleteBroadcastChannelPermission(String imessageId);

    void deleteBroadcastChannelPermissionExcludeConnectedChannel(String imessageId, String channelId);

    void deleteExcludeBroadcastChannel(String imessageId, String channelId);

    void deleteALlBroadcastPermissionExcludeConnectedChannels(String imessageId, String channelId);
}
