package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.mapper.ChannelMapper;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by LONG on 2024/5/2 at 1:32 AM.
 */
@Service
public class ChannelService {
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private PermissionService permissionService;

    public boolean isChannelAssociated(String imessageId, String channelImessageId){
        return channelMapper.isChannelAssociated(imessageId, channelImessageId);
    }

    public boolean isInAdding(String imessageId1, String imessageId2){
        return redisOperationService.CHANNEL_ADDITION.isInAdding(imessageId1, imessageId2)
                || redisOperationService.CHANNEL_ADDITION.isInAdding(imessageId2, imessageId1);
    }

    public void saveRequester(ChannelAddition channelAddition) {
        redisOperationService.CHANNEL_ADDITION.saveRequester(channelAddition);
    }

    public void acceptChangeRequester(ChannelAddition channelAddition){
        redisOperationService.CHANNEL_ADDITION.acceptChangeRequester(channelAddition);
    }

    public void saveResponder(ChannelAddition channelAddition) {
        redisOperationService.CHANNEL_ADDITION.saveResponder(channelAddition);
    }

    public void acceptChangeResponder(ChannelAddition channelAddition){
        redisOperationService.CHANNEL_ADDITION.acceptChangeResponder(channelAddition);
    }

    public ChannelAdditionNotViewedCount getChannelAdditionNotViewedCount(String currentUserImessageId){
        return redisOperationService.CHANNEL_ADDITION.getChannelAdditionNotViewedCount(currentUserImessageId);
    }

    public List<ChannelAddition> getAllChannelAddition(String imessageId, HttpSession session){
        return redisOperationService.CHANNEL_ADDITION.getAllChannelAddition(imessageId, session);
    }

    public void viewAllChannelAddition(String imessageId){
        redisOperationService.CHANNEL_ADDITION.setAllToViewed(imessageId);
    }

    public boolean viewOneChannelAddition(String currentUserImessageId, String uuid){
        return redisOperationService.CHANNEL_ADDITION.setOneToViewed(currentUserImessageId, uuid);
    }

    public ChannelAddition getRequesterChannelAdditionByUuid(String uuid, HttpSession session){
        return redisOperationService.CHANNEL_ADDITION.getRequesterChannelAdditionByUuid(uuid, session);
    }

    public ChannelAddition getResponderChannelAdditionByUuid(String uuid, HttpSession session){
        return redisOperationService.CHANNEL_ADDITION.getResponderChannelAdditionByUuid(uuid, session);
    }

    public boolean insertChannelAssociation(ChannelAssociation channelAssociation){
        return channelMapper.insertChannelAssociation(channelAssociation) == 1;
    }

    public List<ChannelAssociation> findAllChannelAssociations(String imessageId){
        List<ChannelAssociation> allChannelAssociations = channelMapper.findAllChannelAssociations(imessageId);
        allChannelAssociations.forEach(channelAssociation -> {
            Channel channel = channelAssociation.getChannel();
            User.UserProfileVisibility userProfileVisibility = permissionService.findUserProfileVisibilityByImessageId(channelAssociation.getImessageId());
            applyProfileVisibilityToChannel(userProfileVisibility, channel);
            ChatMessageAllow chatMessageAllowToMe = permissionService.findChatMessageAllow(channelAssociation.getImessageId(), channelAssociation.getChannelImessageId());
            if(chatMessageAllowToMe == null) chatMessageAllowToMe = new ChatMessageAllow(true, true);
            channelAssociation.setChatMessageAllowToMe(chatMessageAllowToMe);
            ChatMessageAllow chatMessageAllowToThem = permissionService.findChatMessageAllow(channelAssociation.getChannelImessageId(), channelAssociation.getImessageId());
            if(chatMessageAllowToThem == null) chatMessageAllowToThem = new ChatMessageAllow(true, true);
            channelAssociation.setChatMessageAllowToThem(chatMessageAllowToThem);
        });
        return allChannelAssociations;
    }

    public Channel findChannelByImessageId(String channelImessageId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        return findChannelByImessageId(channelImessageId, currentUser.getImessageId());
    }

    public Channel findChannelByImessageId(String channelImessageId, String imessageId){
        Channel channel = channelMapper.findChannelByImessageId(channelImessageId, imessageId);
        User.UserProfileVisibility userProfileVisibility = permissionService.findUserProfileVisibilityByImessageId(channelImessageId);
        applyProfileVisibilityToChannel(userProfileVisibility, channel);
        return channel;
    }

    private void applyProfileVisibilityToChannel(User.UserProfileVisibility userProfileVisibility, Channel channel) {
        if(!userProfileVisibility.isEmailVisible()) channel.setEmail(null);
        if(!userProfileVisibility.isSexVisible()) channel.setSex(null);
        if(!userProfileVisibility.isRegionVisible()) channel.setFirstRegion(null);
        if(!userProfileVisibility.isRegionVisible()) channel.setSecondRegion(null);
        if(!userProfileVisibility.isRegionVisible()) channel.setThirdRegion(null);
    }

    public Channel findChannelByImessageIdUser(String channelImessageIdUser, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        return findChannelByImessageIdUser(channelImessageIdUser, currentUser.getImessageId());
    }

    public Channel findChannelByImessageIdUser(String channelImessageIdUser, String imessageId){
        if(!permissionService.findWaysToFindMeByImessageIdUser(channelImessageIdUser).isByImessageIdUser()) return null;
        Channel channel = channelMapper.findChannelByImessageIdUser(channelImessageIdUser, imessageId);
        User.UserProfileVisibility userProfileVisibility = permissionService.findUserProfileVisibilityByImessageIdUser(channelImessageIdUser);
        applyProfileVisibilityToChannel(userProfileVisibility, channel);
        return channel;
    }

    public Channel findChannelByEmail(String channelEmail, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        return findChannelByEmail(channelEmail, currentUser.getImessageId());
    }

    public Channel findChannelByEmail(String channelEmail, String imessageId){
        if(!permissionService.findWaysToFindMeByEmail(channelEmail).isByEmail()) return null;
        Channel channel = channelMapper.findChannelByEmail(channelEmail, imessageId);
        User.UserProfileVisibility userProfileVisibility = permissionService.findUserProfileVisibilityByEmail(channelEmail);
        applyProfileVisibilityToChannel(userProfileVisibility, channel);
        return channel;
    }

    public boolean deleteChannelAssociation(String imessageId, String channelImessageId){
        if(!isChannelAssociated(imessageId, channelImessageId)) return false;
        return channelMapper.updateChannelAssociationToInactive(imessageId, channelImessageId) > 0
                | channelMapper.updateChannelAssociationToInactive(channelImessageId, imessageId) > 0;
    }

    public boolean newChannelNote(String imessageId, String channelImessageId, String note){
        if(!isChannelAssociated(imessageId, channelImessageId)) return false;
        updateChannelNoteToInactive(imessageId, channelImessageId);
        return channelMapper.insertChannelNote(imessageId, channelImessageId, note) == 1;
    }

    public boolean updateChannelNoteToInactive(String imessageId, String channelImessageId){
        return channelMapper.updateChannelNoteToInactive(imessageId, channelImessageId) > 0;
    }

    public void notifyChannelsUpdateToAllAssociatedChannels(String userId) {
        new Thread(() -> {
            List<ChannelAssociation> allChannelAssociations = findAllChannelAssociations(userId);
            allChannelAssociations.forEach(channelAssociation -> {
                simpMessagingTemplate.convertAndSendToUser(channelAssociation.getChannelImessageId(), StompDestinations.CHANNELS_UPDATE, "");
            });
        }).start();
    }

    public String findChannelNote(String imessageId, String channelImessageId){
        return channelMapper.findChannelNote(imessageId, channelImessageId);
    }

    public String insertChannelTag(String imessageId, String name){
        String id = UUID.randomUUID().toString();
        Integer maxOrder = channelMapper.findChannelTagMaxOrder(imessageId);
        if(maxOrder == null) maxOrder = -1;
        if(channelMapper.insertChannelTag(id, imessageId, name, maxOrder + 1) == 1){
            return id;
        }else {
            return null;
        }
    }

    public List<ChannelTag> findAllChannelTags(String imessageId){
        return channelMapper.findAllChannelTags(imessageId);
    }

    public ChannelTag findChannelTag(String imessageId, String tagId){
        return channelMapper.findOneChannelTag(imessageId, tagId);
    }

    public boolean updateChannelTagName(String name, String tagId, String imessageId){
        return channelMapper.updateChannelTagName(name, tagId, imessageId) == 1;
    }

    public boolean updateChannelTagOrder(String tagId, String imessageId, int order){
        return channelMapper.updateChannelTagOrder(tagId, imessageId, order) == 1;
    }

    public boolean insertTagChannel(String tagId, String imessageId, String channelImessageId){
        ChannelTag channelTag = channelMapper.findOneChannelTag(imessageId, tagId);
        if(channelTag == null) return false;
        try {
            return channelMapper.insertTagChannel(tagId, channelImessageId) == 1;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTagChannel(String tagId, String imessageId, String channelImessageId){
        return channelMapper.deleteTagChannel(tagId, imessageId, channelImessageId) == 1;
    }

    public boolean deleteTagChannelOfAll(String imessageId, String channelImessageId){
        return channelMapper.deleteTagChannelOfAll(imessageId, channelImessageId) >= 0;
    }

    public boolean updateChannelTagToInactive(String tagId, String imessageId){
        return channelMapper.updateChannelTagToInactive(tagId, imessageId) == 1;
    }

    public boolean deleteAllTagChannel(String tagId, String imessageId){
        return channelMapper.deleteAllTagChannel(tagId, imessageId) >= 0;
    }
}
