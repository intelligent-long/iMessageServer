package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.Broadcast;
import com.longx.intelligent.app.imessage.server.data.BroadcastComment;
import com.longx.intelligent.app.imessage.server.data.BroadcastLike;
import com.longx.intelligent.app.imessage.server.data.BroadcastMedia;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2024/7/28 at 2:02 AM.
 */
@Mapper
public interface BroadcastMapper {

    int insertBroadcast(String broadcastId, String imessageId, Date time, String text);

    List<Broadcast> findBroadcastsLimit(String lastBroadcastId, int rows, List<String> channelIds, String currentUserId, boolean desc);

    int countBroadcasts(List<String> channelIds);

    int insertBroadcastMedia(BroadcastMedia broadcastMedia);

    List<BroadcastMedia> findBroadcastMedias(String broadcastId);

    BroadcastMedia findBroadcastMedia(String mediaId);

    Object findBroadcastMediaData(String mediaId);

    int deleteBroadcast(String broadcastId, String imessageId);

    Broadcast findBroadcast(String broadcastId, String currentUserId);

    int updateBroadcastText(String broadcastId, String newText);

    int updateBroadcastLastEditTime(Date lastEditTime, String broadcastId);

    int deleteBroadcastMedias(List<String> broadcastMediaIds);

    int updateBroadcastMediaIndex(String mediaId, int index);

    BroadcastLike findBroadcastLike(String broadcastId, String fromId);

    BroadcastLike findBroadcastLikeById(String likeId, boolean includeDeleted);

    List<BroadcastLike> findLikesOfChannelBroadcasts(String channelId, String lastLikeId, int rows);

    int insertBroadcastLike(String likeId, String broadcastId, String imessageId, Date time);

    int deleteBroadcastLike(String broadcastId, String imessageId);

    List<BroadcastLike> findLikesOfBroadcast(String broadcastId, String lastLikeId, int rows, boolean includeDeleted);

    int insertBroadcastComment(String commentId, String broadcastId, String imessageId, String text, String toCommentId, Date time);

    BroadcastComment findBroadcastCommentById(String commentId, boolean includeDeleted);

    List<BroadcastComment> findCommentsOfBroadcast(String broadcastId, String lastCommentId, int rows, boolean includeDeleted);

    List<BroadcastComment> findCommentsOfChannelBroadcasts(String channelId, String lastCommentId, int rows);

    int deleteBroadcastComment(String commentId, String imessageId);

    List<BroadcastComment> findReplyCommentsOfChannelBroadcasts(String channelId, String lastReplyCommentId, int rows);

    int getBroadcastPosition(String broadcastId);
}
