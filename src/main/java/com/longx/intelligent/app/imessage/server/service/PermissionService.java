package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.mapper.PermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by LONG on 2024/6/7 at 4:41 PM.
 */
@Service
public class PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    @Lazy
    private ChannelService channelService;
    @Autowired
    @Lazy
    private BroadcastService broadcastService;

    public boolean insertOrUpdateUserProfileVisibility(String imessageId, boolean emailVisible, boolean sexVisible, boolean regionVisible){
        return permissionMapper.insertOrUpdateUserProfileVisibility(imessageId, emailVisible, sexVisible, regionVisible) > 0;
    }

    public User.UserProfileVisibility findUserProfileVisibilityByImessageId(String imessageId){
        User.UserProfileVisibility userProfileVisibility = permissionMapper.findUserProfileVisibilityByImessageId(imessageId);
        if(userProfileVisibility == null) return new User.UserProfileVisibility(true, true, true);
        return userProfileVisibility;
    }

    public User.UserProfileVisibility findUserProfileVisibilityByImessageIdUser(String imessageIdUser){
        User.UserProfileVisibility userProfileVisibility = permissionMapper.findUserProfileVisibilityByImessageIdUser(imessageIdUser);
        if(userProfileVisibility == null) return new User.UserProfileVisibility(true, true, true);
        return userProfileVisibility;
    }

    public User.UserProfileVisibility findUserProfileVisibilityByEmail(String imessageIdEmail){
        User.UserProfileVisibility userProfileVisibility = permissionMapper.findUserProfileVisibilityByEmail(imessageIdEmail);
        if(userProfileVisibility == null) return new User.UserProfileVisibility(true, true, true);
        return userProfileVisibility;
    }

    public boolean insertOrUpdateWaysToFindMe(String imessageId, boolean byImessageId, boolean byEmail){
        return permissionMapper.insertOrUpdateWaysToFindMe(imessageId, byImessageId, byEmail) > 0;
    }

    public User.WaysToFindMe findWaysToFindMeByImessageId(String imessageId){
        User.WaysToFindMe waysToFindMe = permissionMapper.findWaysToFindMeByImessageId(imessageId);
        if(waysToFindMe == null) return new User.WaysToFindMe(true, true);
        return waysToFindMe;
    }

    public User.WaysToFindMe findWaysToFindMeByImessageIdUser(String imessageIdUser){
        User.WaysToFindMe waysToFindMe = permissionMapper.findWaysToFindMeByImessageIdUser(imessageIdUser);
        if(waysToFindMe == null) return new User.WaysToFindMe(true, true);
        return waysToFindMe;
    }

    public User.WaysToFindMe findWaysToFindMeByEmail(String email){
        User.WaysToFindMe waysToFindMe = permissionMapper.findWaysToFindMeByEmail(email);
        if(waysToFindMe == null) return new User.WaysToFindMe(true, true);
        return waysToFindMe;
    }

    public boolean insertOrUpdateAllowChatMessage(String imessageId, String channelImessageId, ChatMessageAllow chatMessageAllow){
        return permissionMapper.insertOrUpdateAllowChatMessage(imessageId, channelImessageId, chatMessageAllow) > 0;
    }

    public ChatMessageAllow findChatMessageAllow(String imessageId, String channelImessageId){
        return permissionMapper.findChatMessageAllow(imessageId, channelImessageId);
    }

    public boolean deleteChatMessageAllow(String imessageId, String channelImessageId){
        if(findChatMessageAllow(imessageId, channelImessageId) == null) return true;
        return permissionMapper.deleteChatMessageAllow(imessageId, channelImessageId) > 0;
    }

    public BroadcastChannelPermission findBroadcastChannelPermission(String imessageId){
        return permissionMapper.findBroadcastChannelPermission(imessageId);
    }

    public boolean insertOrUpdateBroadcastChannelPermission(String imessageId, int permission){
        return permissionMapper.insertOrUpdateBroadcastChannelPermission(imessageId, permission) > 0;
    }

    public boolean insertBroadcastChannelPermissionExcludeConnectedChannels(String imessageId, Set<String> excludeConnectedChannels){
        if(excludeConnectedChannels.isEmpty()) return true;
        List<ChannelAssociation> channelAssociations = channelService.findAllChannelAssociations(imessageId);
        Set<String> channels = new HashSet<>();
        channelAssociations.forEach(channelAssociation -> channels.add(channelAssociation.getChannelImessageId()));
        for (String excludeConnectedChannel : excludeConnectedChannels) {
            if(!channels.contains(excludeConnectedChannel)) return false;
        }
        return permissionMapper.insertBroadcastChannelPermissionExcludeConnectedChannels(imessageId, excludeConnectedChannels) > 0;
    }

    public void deleteAllBroadcastChannelPermissionExcludeConnectedChannels(String imessageId){
        permissionMapper.deleteAllBroadcastChannelPermissionExcludeConnectedChannels(imessageId);
    }

    public boolean updateAllBroadcastChannelPermissionExcludeConnectedChannels(String imessageId, Set<String> excludeConnectedChannels){
        deleteAllBroadcastChannelPermissionExcludeConnectedChannels(imessageId);
        return insertBroadcastChannelPermissionExcludeConnectedChannels(imessageId, excludeConnectedChannels);
    }

    public Set<String> findExcludeBroadcastChannels(String imessageId){
        return permissionMapper.findExcludeBroadcastChannels(imessageId);
    }

    public boolean insertExcludeBroadcastChannels(String imessageId, Set<String> excludeBroadcastChannelIds){
        if(excludeBroadcastChannelIds.isEmpty()) return true;
        List<ChannelAssociation> channelAssociations = channelService.findAllChannelAssociations(imessageId);
        Set<String> channels = new HashSet<>();
        channelAssociations.forEach(channelAssociation -> channels.add(channelAssociation.getChannelImessageId()));
        for (String excludeBroadcastChannelId : excludeBroadcastChannelIds) {
            if(!channels.contains(excludeBroadcastChannelId)) return false;
        }
        return permissionMapper.insertExcludeBroadcastChannels(imessageId, excludeBroadcastChannelIds) > 0;
    }

    public void deleteAllExcludeBroadcastChannels(String imessageId){
        permissionMapper.deleteAllExcludeBroadcastChannels(imessageId);
    }

    public boolean updateAllExcludeBroadcastChannels(String imessageId, Set<String> excludeBroadcastChannelIds){
        deleteAllExcludeBroadcastChannels(imessageId);
        return insertExcludeBroadcastChannels(imessageId, excludeBroadcastChannelIds);
    }

    public boolean insertOrUpdateBroadcastPermission(String broadcastId, String currentUserImessageId, int permission){
        if(broadcastService.findBroadcast(broadcastId, currentUserImessageId) == null) return false;
        return permissionMapper.insertOrUpdateBroadcastPermission(broadcastId, permission) > 0;
    }

    public boolean insertBroadcastPermissionExcludeConnectedChannels(String broadcastId, String currentUserImessageId, Set<String> excludeConnectedChannels){
        if(excludeConnectedChannels.isEmpty()) return true;
        if(broadcastService.findBroadcast(broadcastId, currentUserImessageId) == null) return false;
        List<ChannelAssociation> channelAssociations = channelService.findAllChannelAssociations(currentUserImessageId);
        Set<String> channels = new HashSet<>();
        channelAssociations.forEach(channelAssociation -> channels.add(channelAssociation.getChannelImessageId()));
        for (String excludeConnectedChannel : excludeConnectedChannels) {
            if(!channels.contains(excludeConnectedChannel)) return false;
        }
        return permissionMapper.insertBroadcastPermissionExcludeConnectedChannels(broadcastId, excludeConnectedChannels) > 0;
    }

    public boolean deleteAllBroadcastPermissionExcludeConnectedChannels(String broadcastId, String currentUserImessageId){
        if(broadcastService.findBroadcast(broadcastId, currentUserImessageId) == null) return false;
        permissionMapper.deleteAllBroadcastPermissionExcludeConnectedChannels(broadcastId);
        return true;
    }

    public boolean updateAllBroadcastPermissionExcludeConnectedChannels(String broadcastId, String currentUserImessageId, Set<String> excludeConnectedChannels){
        boolean success1 = deleteAllBroadcastPermissionExcludeConnectedChannels(broadcastId, currentUserImessageId);
        boolean success2 = insertBroadcastPermissionExcludeConnectedChannels(broadcastId, currentUserImessageId, excludeConnectedChannels);
        return success1 && success2;
    }

    public BroadcastPermission findBroadcastPermission(String broadcastId){
        return permissionMapper.findBroadcastPermission(broadcastId);
    }

    public void deleteBroadcastChannelPermission(String imessageId){
        permissionMapper.deleteBroadcastChannelPermission(imessageId);
    }

    public void deleteBroadcastChannelPermissionExcludeConnectedChannel(String imessageId, String channelId){
        permissionMapper.deleteBroadcastChannelPermissionExcludeConnectedChannel(imessageId, channelId);
    }
    public void deleteExcludeBroadcastChannel(String imessageId, String channelId){
        permissionMapper.deleteExcludeBroadcastChannel(imessageId, channelId);
    }

    public void deleteALlBroadcastPermissionExcludeConnectedChannels(String imessageId, String channelId){
        permissionMapper.deleteALlBroadcastPermissionExcludeConnectedChannels(imessageId, channelId);
    }
}
