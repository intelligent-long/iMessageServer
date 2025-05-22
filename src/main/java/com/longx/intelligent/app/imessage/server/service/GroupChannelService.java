package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.mapper.GroupChannelMapper;
import com.longx.intelligent.app.imessage.server.util.NanoIdUtil;
import com.longx.intelligent.app.imessage.server.util.StringUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by LONG on 2025/4/15 at 10:03 PM.
 */
@Service
public class GroupChannelService {
    @Autowired
    private GroupChannelMapper groupChannelMapper;
    @Autowired
    private RedisOperationService redisOperationService;

    public synchronized String generateGroupChannelId(){
        String groupChannelId = "imgid_" + NanoIdUtil.randomNanoId();
        boolean isExist = groupChannelMapper.findGroupChannelById(groupChannelId, null) != null;
        if(isExist){
            return generateGroupChannelId();
        }
        return groupChannelId;
    }

    public String createGroupChannel(String ownerImessageId, String name){
        String groupChannelId = generateGroupChannelId();
        if(groupChannelMapper.insertGroupChannel(groupChannelId, groupChannelId, ownerImessageId, name, new Date()) == 1){
            return groupChannelId;
        }
        return null;
    }

    public boolean setNote(String imessageId, String groupChannelId, String note){
        if(!isGroupChannelAssociated(groupChannelId, imessageId)) return false;
        updateGroupChannelNoteToInactive(imessageId, groupChannelId);
        return groupChannelMapper.insertGroupChannelNote(imessageId, groupChannelId, note) == 1;
    }

    public boolean updateGroupChannelNoteToInactive(String imessageId, String groupChannelId){
        return groupChannelMapper.updateGroupChannelNoteToInactive(imessageId, groupChannelId) > 0;
    }

    public String insertGroupChannelTag(String imessageId, String name){
        String id = UUID.randomUUID().toString();
        Integer maxOrder = groupChannelMapper.findGroupChannelTagMaxOrder(imessageId);
        if(maxOrder == null) maxOrder = -1;
        if(groupChannelMapper.insertGroupChannelTag(id, imessageId, name, maxOrder + 1) == 1){
            return id;
        }else {
            return null;
        }
    }

    public boolean insertTagGroupChannel(String tagId, String imessageId, String groupChannelId){
        GroupChannelTag groupChannelTag = groupChannelMapper.findOneGroupChannelTag(imessageId, tagId);
        if(groupChannelTag == null) return false;
        try {
            return groupChannelMapper.insertTagGroupChannel(tagId, groupChannelId) == 1;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean isGroupChannelAssociated(String groupChannelId, String requester){
        return groupChannelMapper.isGroupChannelAssociated(groupChannelId, requester);
    }

    public List<GroupChannel> findAllAssociatedGroupChannels(String currentUserId){
        return groupChannelMapper.findAllAssociatedGroupChannels(currentUserId);
    }

    public List<GroupChannel> findAllOwnerGroupChannels(String currentUserId){
        return groupChannelMapper.findAllOwnerGroupChannels(currentUserId);
    }

    public String insertGroupChannelAssociation(String groupChannelId, String owner, String requester, String requestMessage, Date requestTime, Date acceptTime, String inviteUuid){
        String id = UUID.randomUUID().toString();
        if(groupChannelMapper.insertGroupChannelAssociation(id, groupChannelId, owner, requester, requestMessage, requestTime, acceptTime, inviteUuid) == 1){
            return id;
        }else {
            return null;
        }
    }

    public GroupChannel findGroupChannelById(String groupChannelId, String currentUserId){
        return groupChannelMapper.findGroupChannelById(groupChannelId, currentUserId);
    }

    public GroupChannel findGroupChannelByIdUser(String groupChannelIdUser, String currentUserId){
        return groupChannelMapper.findGroupChannelByIdUser(groupChannelIdUser, currentUserId);
    }

    public boolean updateGroupChannelName(String groupChannelId, String newName, String owner){
        return groupChannelMapper.updateGroupChannelName(groupChannelId, newName, owner) > 0;
    }

    public Date findGroupChannelIdUserLastChangeTime(String groupChannelId){
        return groupChannelMapper.findGroupChannelIdUserLastChangeTime(groupChannelId);
    }

    public boolean updateGroupChannelIdUserLastChangeTime(Date lastChangeTime, String groupChannelId, String owner){
        return groupChannelMapper.updateGroupChannelIdUserLastChangeTime(lastChangeTime, groupChannelId, owner) == 1;
    }

    public boolean isGroupChannelIdUserValid(String groupChannelIdUser){
        for (String groupChannelIdInvalidContent : Constants.GROUP_CHANNEL_ID_USER_INVALID_CONTENTS) {
            if (StringUtil.containsIgnoreCase(groupChannelIdUser, groupChannelIdInvalidContent)) {
                return false;
            }
        }
        return true;
    }

    public boolean updateGroupChannelIdUser(String groupChannelId, String newGroupChannelIdUser, String owner){
        return groupChannelMapper.updateGroupChannelIdUser(groupChannelId, newGroupChannelIdUser, owner) == 1;
    }

    public boolean updateGroupChannelAvatar(GroupChannelAvatar avatar, byte[] data){
        return groupChannelMapper.updateGroupChannelAvatar(avatar, data) == 1;
    }

    public boolean updateAvatarHashWithGroupChannel(String avatarHash, String groupChannelId, String owner){
        return groupChannelMapper.updateAvatarHashWithGroupChannel(avatarHash, groupChannelId, owner) == 1;
    }

    public GroupChannelAvatar findAvatar(String avatarHash){
        return groupChannelMapper.findAvatar(avatarHash);
    }

    public byte[] findAvatarData(String avatarHash){
        return (byte[]) groupChannelMapper.findAvatarData(avatarHash);
    }

    public List<GroupChannelTag> findAllGroupChannelTags(String imessageId){
        return groupChannelMapper.findAllGroupChannelTags(imessageId);
    }

    public boolean updateGroupChannelTagOrder(String tagId, String imessageId, int order){
        return groupChannelMapper.updateGroupChannelTagOrder(tagId, imessageId, order) == 1;
    }

    public boolean updateGroupChannelTagToInactive(String tagId, String imessageId){
        return groupChannelMapper.updateGroupChannelTagToInactive(tagId, imessageId) == 1;
    }

    public boolean deleteAllTagGroupChannel(String tagId, String imessageId){
        return groupChannelMapper.deleteAllTagGroupChannel(tagId, imessageId) >= 0;
    }

    public GroupChannelTag findGroupChannelTag(String imessageId, String tagId){
        return groupChannelMapper.findOneGroupChannelTag(imessageId, tagId);
    }

    public boolean updateGroupChannelTagName(String name, String tagId, String imessageId){
        return groupChannelMapper.updateGroupChannelTagName(name, tagId, imessageId) == 1;
    }

    public boolean deleteTagGroupChannel(String tagId, String imessageId, String groupChannelId){
        return groupChannelMapper.deleteTagGroupChannel(tagId, imessageId, groupChannelId) == 1;
    }

    public boolean updateGroupJoinVerification(String groupChannelId, Boolean joinVerification, String owner){
        return groupChannelMapper.updateGroupJoinVerification(groupChannelId, joinVerification, owner) == 1;
    }

    public boolean isGroupJoinNeedVerification(String groupChannelId){
        Boolean groupJoinNeedVerification = groupChannelMapper.isGroupJoinNeedVerification(groupChannelId);
        if(groupJoinNeedVerification == null) return false;
        return groupJoinNeedVerification;
    }

    public boolean isInAdding(String imessageId, String groupChannelId) {
        return redisOperationService.GROUP_CHANNEL_ADDITION.isInAdding(imessageId, groupChannelId);
    }

    public void saveRequester(GroupChannelAddition groupChannelAddition) {
        redisOperationService.GROUP_CHANNEL_ADDITION.saveRequester(groupChannelAddition);
    }

    public void saveResponder(GroupChannelAddition groupChannelAddition) {
        redisOperationService.GROUP_CHANNEL_ADDITION.saveResponder(groupChannelAddition);
    }

    public GroupChannelAdditionNotViewedCount getGroupChannelAdditionNotViewedCount(String currentUserImessageId){
        return redisOperationService.GROUP_CHANNEL_ADDITION.getGroupChannelAdditionNotViewedCount(currentUserImessageId);
    }

    public List<GroupChannelAddition> getAllGroupChannelAddition(String currentUserId, HttpSession session){
        return redisOperationService.GROUP_CHANNEL_ADDITION.getAllGroupChannelAddition(currentUserId, session);
    }

    public List<GroupChannelInvitation> getAllGroupChannelInvitation(String imessageId, HttpSession session){
        return redisOperationService.GROUP_CHANNEL_ADDITION.getAllGroupChannelInvitation(imessageId, session);
    }

    public List<GroupChannelInvitation> getGroupChannelInvitationByUuid(String uuid, HttpSession session){
        return redisOperationService.GROUP_CHANNEL_ADDITION.getGroupChannelInvitationByUuid(uuid, session);
    }

    public GroupChannelAddition getRequesterGroupChannelAdditionByUuid(String uuid, HttpSession session){
        return redisOperationService.GROUP_CHANNEL_ADDITION.getRequesterGroupChannelAdditionByUuid(uuid, session);
    }

    public void acceptChangeRequester(GroupChannelAddition groupChannelAddition){
        redisOperationService.GROUP_CHANNEL_ADDITION.acceptChangeRequester(groupChannelAddition);
    }

    public void acceptChangeResponder(GroupChannelAddition groupChannelAddition){
        redisOperationService.GROUP_CHANNEL_ADDITION.acceptChangeResponder(groupChannelAddition);
    }

    public boolean viewOneChannelAddition(String currentUserImessageId, String uuid){
        return redisOperationService.GROUP_CHANNEL_ADDITION.setOneToViewed(currentUserImessageId, uuid);
    }

    public void saveInviter(GroupChannelInvitation groupChannelInvitation) {
        redisOperationService.GROUP_CHANNEL_ADDITION.saveInviter(groupChannelInvitation);
    }

    public void saveInvitee(GroupChannelInvitation groupChannelInvitation) {
        redisOperationService.GROUP_CHANNEL_ADDITION.saveInvitee(groupChannelInvitation);
    }

    public boolean isInInviting(String inviter, String invitee, String groupChannelId) {
        return redisOperationService.GROUP_CHANNEL_ADDITION.isInInviting(inviter, invitee, groupChannelId);
    }
}
