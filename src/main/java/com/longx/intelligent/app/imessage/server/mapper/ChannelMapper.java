package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.Channel;
import com.longx.intelligent.app.imessage.server.data.ChannelAssociation;
import com.longx.intelligent.app.imessage.server.data.ChannelCollectionItem;
import com.longx.intelligent.app.imessage.server.data.ChannelTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by LONG on 2024/5/2 at 1:34 AM.
 */
@Mapper
public interface ChannelMapper {

    boolean isChannelAssociated(String imessageId, String channelImessageId);

    int insertChannelAssociation(ChannelAssociation channelAssociation);

    List<ChannelAssociation> findAllChannelAssociations(String imessageId);

    Channel findChannelByImessageId(String channelImessageId, String currentUserId);

    Channel findChannelByImessageIdUser(String channelImessageIdUser, String currentUserId);

    Channel findChannelByEmail(String channelEmail, String imessageId);

    int updateChannelAssociationToInactive(String imessageId, String channelImessageId);

    int updateChannelNoteToInactive(String imessageId, String channelImessageId);

    int insertChannelNote(String imessageId, String channelImessageId, String note);

    String findChannelNote(String imessageId, String channelImessageId);

    int insertChannelTag(String tagId, String imessageId, String name, int order);

    List<ChannelTag> findAllChannelTags(String imessageId);

    ChannelTag findOneChannelTag(String imessageId, String tagId);

    Integer findChannelTagMaxOrder(String imessageId);

    int updateChannelTagName(String name, String tagId, String imessageId);

    int updateChannelTagOrder(String tagId, String imessageId, int order);

    int insertTagChannel(String tagId, String channelImessageId);

    int deleteTagChannel(String tagId, String imessageId, String channelImessageId);

    int deleteTagChannelOfAll(String imessageId, String channelImessageId);

    int updateChannelTagToInactive(String tagId, String imessageId);

    int deleteAllTagChannel(String tagId, String imessageId);

    List<ChannelCollectionItem> findAllChannelCollections(String owner);

    int getMaxOrder();

    int addChannelCollection(ChannelCollectionItem channelCollectionItem);

    int removeChannelCollection(String uuid, String owner);
}
