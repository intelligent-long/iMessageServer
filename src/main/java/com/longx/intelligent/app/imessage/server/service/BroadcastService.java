package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.mapper.BroadcastMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by LONG on 2024/7/28 at 1:55 AM.
 */
@Service
public class BroadcastService {
    @Autowired
    private BroadcastMapper broadcastMapper;
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ChannelService channelService;

    public String sendBroadcast(String imessageId, Date time, String text){
        String broadcastId = UUID.randomUUID().toString();
        if(broadcastMapper.insertBroadcast(broadcastId, imessageId, time, text) == 1){
            return broadcastId;
        }else {
            return null;
        }
    }

    public List<Broadcast> findBroadcastsLimit(String lastBroadcastId, int ps, List<String> channelIds, String currentUserId, boolean desc){
        return broadcastMapper.findBroadcastsLimit(lastBroadcastId, ps, channelIds, currentUserId, desc);
    }

    public int countBroadcasts(List<String> channelIds){
        return broadcastMapper.countBroadcasts(channelIds);
    }

    public boolean insertBroadcastMedia(BroadcastMedia broadcastMedia){
        return broadcastMapper.insertBroadcastMedia(broadcastMedia) == 1;
    }

    public List<BroadcastMedia> findBroadcastMedias(String broadcastId){
        return broadcastMapper.findBroadcastMedias(broadcastId);
    }

    public BroadcastMedia findBroadcastMedia(String mediaId){
        return broadcastMapper.findBroadcastMedia(mediaId);
    }

    public byte[] findBroadcastMediaData(String mediaId){
        return (byte[]) broadcastMapper.findBroadcastMediaData(mediaId);
    }

    public boolean deleteBroadcast(String broadcastId, String imessageId){
        return broadcastMapper.deleteBroadcast(broadcastId, imessageId) == 1;
    }

    public Broadcast findBroadcast(String broadcastId, String currentUserId){
        Broadcast broadcast = broadcastMapper.findBroadcast(broadcastId, currentUserId);
        if(broadcast == null) return null;
        List<BroadcastMedia> broadcastMedias = broadcastMapper.findBroadcastMedias(broadcastId);
        broadcast.setBroadcastMedias(broadcastMedias);
        return broadcast;
    }

    public boolean updateBroadcastText(String broadcastId, String newText){
        return broadcastMapper.updateBroadcastText(broadcastId, newText) == 1;
    }

    public boolean updateBroadcastLastEditTime(Date lastEditTime, String broadcastId){
        return broadcastMapper.updateBroadcastLastEditTime(lastEditTime, broadcastId) == 1;
    }

    public boolean deleteBroadcastMedias(List<String> broadcastMediaIds){
        return broadcastMapper.deleteBroadcastMedias(broadcastMediaIds) == broadcastMediaIds.size();
    }

    public boolean updateBroadcastMediaIndex(String mediaId, int index){
        return broadcastMapper.updateBroadcastMediaIndex(mediaId, index) == 1;
    }

    public BroadcastLike findBroadcastLike(String broadcastId, String fromId){
        return broadcastMapper.findBroadcastLike(broadcastId, fromId);
    }

    public BroadcastLike findBroadcastLikeById(String likeId, boolean includeDeleted){
        return broadcastMapper.findBroadcastLikeById(likeId, includeDeleted);
    }

    public List<BroadcastLike> findLikesOfChannelBroadcasts(String channelId, String lastLikeId, int ps){
        List<BroadcastLike> likesOfChannelBroadcasts = broadcastMapper.findLikesOfChannelBroadcasts(channelId, lastLikeId, ps);
        likesOfChannelBroadcasts.forEach(broadcastLike -> {
            if (broadcastLike.getBroadcastDeleted()) {
                broadcastLike.setBroadcastText(null);
                broadcastLike.setCoverMediaId(null);
            }
            boolean hasNewBroadcastLike = redisOperationService.BROADCAST.hasNewBroadcastLike(channelId, broadcastLike.getLikeId());
            broadcastLike.setNew(hasNewBroadcastLike);
        });
        return likesOfChannelBroadcasts;
    }

    public boolean insertBroadcastLike(String likeId, String broadcastId, String imessageId, Date time){
        return broadcastMapper.insertBroadcastLike(likeId, broadcastId, imessageId, time) == 1;
    }

    public boolean deleteBroadcastLike(String broadcastId, String imessageId){
        return broadcastMapper.deleteBroadcastLike(broadcastId, imessageId) > 0;
    }

    public List<BroadcastLike> findLikesOfBroadcast(String broadcastId, String lastLikeId, int ps, boolean includeDeleted){
        List<BroadcastLike> likesOfBroadcast = broadcastMapper.findLikesOfBroadcast(broadcastId, lastLikeId, ps, includeDeleted);
        likesOfBroadcast.forEach(broadcastLike -> {
            if (broadcastLike.getBroadcastDeleted()) {
                broadcastLike.setBroadcastText(null);
                broadcastLike.setCoverMediaId(null);
            }
        });
        return likesOfBroadcast;
    }

    public boolean insertBroadcastComment(String commentId, String broadcastId, String imessageId, String text, String toCommentId, Date time){
        return broadcastMapper.insertBroadcastComment(commentId, broadcastId, imessageId, text, toCommentId, time) == 1;
    }

    public BroadcastComment findBroadcastCommentById(String commentId, boolean includeDeleted){
        BroadcastComment broadcastComment = broadcastMapper.findBroadcastCommentById(commentId, includeDeleted);
        if(broadcastComment.getBroadcastDeleted()){
            broadcastComment.setBroadcastText(null);
            broadcastComment.setCoverMediaId(null);
        }
        findAndSetToComments(broadcastComment);
        return broadcastComment;
    }

    public List<BroadcastComment> findCommentsOfBroadcast(String broadcastId, String lastCommentId, int ps, boolean includeDeleted){
        List<BroadcastComment> commentsOfBroadcast = broadcastMapper.findCommentsOfBroadcast(broadcastId, lastCommentId, ps, includeDeleted);
        commentsOfBroadcast.forEach(broadcastComment -> {
            if(broadcastComment.getBroadcastDeleted()){
                broadcastComment.setBroadcastText(null);
                broadcastComment.setCoverMediaId(null);
            }
            findAndSetToComments(broadcastComment);
        });
        return commentsOfBroadcast;
    }

    public List<BroadcastComment> findCommentsOfChannelBroadcasts(String channelId, String lastCommentId, int ps){
        List<BroadcastComment> commentsOfChannelBroadcasts = broadcastMapper.findCommentsOfChannelBroadcasts(channelId, lastCommentId, ps);
        commentsOfChannelBroadcasts.forEach(broadcastComment -> {
            if (broadcastComment.getBroadcastDeleted()) {
                broadcastComment.setBroadcastText(null);
                broadcastComment.setCoverMediaId(null);
            }
            boolean hasNewBroadcastComment = redisOperationService.BROADCAST.hasNewBroadcastComment(channelId, broadcastComment.getCommentId());
            broadcastComment.setNew(hasNewBroadcastComment);
            findAndSetToComments(broadcastComment);
        });
        return commentsOfChannelBroadcasts;
    }

    private void findAndSetToComments(BroadcastComment broadcastComment) {
        if(broadcastComment.getToCommentId() != null) {
            BroadcastComment toComment = broadcastMapper.findBroadcastCommentById(broadcastComment.getToCommentId(), false);
            broadcastComment.setToComment(toComment);
            if(toComment.getToCommentId() != null) {
                BroadcastComment toToComment = broadcastMapper.findBroadcastCommentById(toComment.getToCommentId(), false);
                toComment.setToComment(toToComment);
            }
        }
    }

    public boolean deleteBroadcastComment(String commentId, String imessageId){
        return broadcastMapper.deleteBroadcastComment(commentId, imessageId) > 0;
    }

    public List<BroadcastComment> findReplyCommentsOfChannelBroadcast(String channelId, String lastReplyCommentId, int ps){
        List<BroadcastComment> replyCommentsOfChannelBroadcasts = broadcastMapper.findReplyCommentsOfChannelBroadcasts(channelId, lastReplyCommentId, ps);
        replyCommentsOfChannelBroadcasts.forEach(broadcastComment -> {
            if (broadcastComment.getBroadcastDeleted()) {
                broadcastComment.setBroadcastText(null);
                broadcastComment.setCoverMediaId(null);
            }
            boolean hasNewBroadcastReply = redisOperationService.BROADCAST.hasNewBroadcastReply(channelId, broadcastComment.getCommentId());
            broadcastComment.setNew(hasNewBroadcastReply);
            findAndSetToComments(broadcastComment);
        });
        return replyCommentsOfChannelBroadcasts;
    }

    public boolean determineBroadcastVisibility(Broadcast broadcast, String currentUserImessageId){
        if (broadcast.getImessageId().equals(currentUserImessageId)) {
            return true;
        }

        if(!determineBroadcastChannelVisibility(broadcast.getImessageId(), currentUserImessageId)){
            return false;
        }

        BroadcastPermission broadcastPermission = permissionService.findBroadcastPermission(broadcast.getBroadcastId());
        if(broadcastPermission == null){
            return true;
        }
        if(broadcastPermission.getPermission() == BroadcastPermission.PRIVATE){
            return false;
        }
        if(broadcastPermission.getPermission() == BroadcastPermission.PUBLIC){
            return true;
        }
        if(broadcastPermission.getPermission() == BroadcastPermission.CONNECTED_CHANNEL_CIRCLE){
            Set<String> excludeConnectedChannels = broadcastPermission.getExcludeConnectedChannels();
            boolean isExclude = excludeConnectedChannels.contains(currentUserImessageId);
            List<ChannelAssociation> channelAssociations = channelService.findAllChannelAssociations(broadcast.getImessageId());
            boolean isAssociated = false;
            for (ChannelAssociation channelAssociation : channelAssociations) {
                if(channelAssociation.getChannelImessageId().equals(currentUserImessageId)){
                    isAssociated = true;
                    break;
                }
            }
            return !isExclude && isAssociated;
        }
        return true;
    }

    public boolean determineBroadcastChannelVisibility(String channelImessageId, String currentUserImessageId){
        if (channelImessageId.equals(currentUserImessageId)) {
            return true;
        }
        BroadcastChannelPermission broadcastChannelPermission = permissionService.findBroadcastChannelPermission(channelImessageId);
        if(broadcastChannelPermission == null){
            return true;
        }
        if(broadcastChannelPermission.getPermission() == BroadcastChannelPermission.PRIVATE){
            return false;
        }
        if(broadcastChannelPermission.getPermission() == BroadcastChannelPermission.PUBLIC){
            return true;
        }
        if(broadcastChannelPermission.getPermission() == BroadcastChannelPermission.CONNECTED_CHANNEL_CIRCLE){
            Set<String> excludeConnectedChannels = broadcastChannelPermission.getExcludeConnectedChannels();
            boolean isExclude = excludeConnectedChannels.contains(currentUserImessageId);
            List<ChannelAssociation> channelAssociations = channelService.findAllChannelAssociations(channelImessageId);
            boolean isAssociated = false;
            for (ChannelAssociation channelAssociation : channelAssociations) {
                if(channelAssociation.getChannelImessageId().equals(currentUserImessageId)){
                    isAssociated = true;
                    break;
                }
            }
            return !isExclude && isAssociated;
        }
        return false;
    }

    public List<Broadcast> filterBroadcastListToVisibleContents(List<Broadcast> broadcastList, String currentUserImessageId){
        List<Broadcast> visibleContents = new ArrayList<>();
        Set<String> excludeBroadcastChannels = permissionService.findExcludeBroadcastChannels(currentUserImessageId);
        broadcastList.forEach(broadcast -> {
            if (determineBroadcastVisibility(broadcast, currentUserImessageId) && !excludeBroadcastChannels.contains(broadcast.getImessageId())) {
                visibleContents.add(broadcast);
            }
        });
        visibleContents.forEach(broadcast -> {
            if(!broadcast.getImessageId().equals(currentUserImessageId)){
                broadcast.setBroadcastPermission(null);
            }
        });
        return visibleContents;
    }

    public List<Broadcast> filterBroadcastListToVisibleContentsForChannel(List<Broadcast> broadcastList, String currentUserImessageId){
        List<Broadcast> visibleContents = new ArrayList<>();
        broadcastList.forEach(broadcast -> {
            if (determineBroadcastVisibility(broadcast, currentUserImessageId)) {
                visibleContents.add(broadcast);
            }
        });
        visibleContents.forEach(broadcast -> {
            if(!broadcast.getImessageId().equals(currentUserImessageId)){
                broadcast.setBroadcastPermission(null);
            }
        });
        return visibleContents;
    }

    public int getBroadcastPosition(String broadcastId){
        return broadcastMapper.getBroadcastPosition(broadcastId);
    }
}
