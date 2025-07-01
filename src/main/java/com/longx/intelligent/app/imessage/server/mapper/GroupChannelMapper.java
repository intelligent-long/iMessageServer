package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2025/4/15 at 10:05 PM.
 */
@Mapper
public interface GroupChannelMapper {
    int insertGroupChannel(String groupChannelId, String groupChannelIdUser, String ownerImessageId, String name, Date createTime);

    GroupChannel findGroupChannelById(String groupChannelId, String currentUserId);

    GroupChannel findGroupChannelByIdUser(String groupChannelIdUser, String currentUserId);

    GroupChannel findGroupChannelByIdIncludeInactive(String groupChannelId, String currentUserId);

    GroupChannel findGroupChannelByIdUserIncludeInactive(String groupChannelIdUser, String currentUserId);

    Integer findGroupChannelTagMaxOrder(String imessageId);

    int insertGroupChannelTag(String tagId, String imessageId, String name, int order);

    GroupChannelTag findOneGroupChannelTag(String imessageId, String tagId);

    int insertTagGroupChannel(String tagId, String groupChannelId);

    int updateGroupChannelNoteToInactive(String imessageId, String groupChannelId);

    int insertGroupChannelNote(String imessageId, String groupChannelId, String note);

    boolean isGroupChannelAssociated(String groupChannelId, String requester);

    List<GroupChannel> findAllAssociatedGroupChannels(String currentUserId);

    List<GroupChannel> findAllOwnerGroupChannels(String currentUserId);

    int insertGroupChannelAssociation(String associationId, String groupChannelId, String owner, String requester, String requestMessage, Date requestTime, Date acceptTime, String inviteUuid);

    int updateGroupChannelName(String groupChannelId, String newName, String owner);

    Date findGroupChannelIdUserLastChangeTime(String groupChannelId);

    int updateGroupChannelIdUserLastChangeTime(Date lastChangeTime, String groupChannelId, String owner);

    int updateGroupChannelIdUser(String groupChannelId, String newGroupChannelIdUser, String owner);

    int changeRegion(Integer firstRegionAdcode, Integer secondRegionAdcode, Integer thirdRegionAdcode, String groupChannelId, String owner);

    int updateGroupChannelAvatar(GroupChannelAvatar avatar, byte[] data);

    int updateAvatarHashWithGroupChannel(String avatarHash, String groupChannelId, String owner);

    GroupChannelAvatar findAvatar(String avatarHash);

    Object findAvatarData(String avatarHash);

    List<GroupChannelTag> findAllGroupChannelTags(String imessageId);

    int updateGroupChannelTagOrder(String tagId, String imessageId, int order);

    int updateGroupChannelTagToInactive(String tagId, String imessageId);

    int deleteAllTagGroupChannel(String tagId, String imessageId);

    int updateGroupChannelTagName(String name, String tagId, String imessageId);

    int deleteTagGroupChannel(String tagId, String imessageId, String groupChannelId);

    int updateGroupJoinVerificationEnabled(String groupChannelId, Boolean joinVerification, String owner);

    Boolean isGroupJoinNeedVerification(String groupChannelId);

    int setGroupChannelAssociationToInactive(String groupChannelId, String requester);

    int setAllGroupChannelAssociationToInactive(String groupChannelId);

    int changeGroupChannelOwner(String groupChannelId, String changeToChannelId);

    int updateGroupChannelToInactive(String groupChannelId);

    int updateAllGroupChannelNoteToInactive(String groupChannelId);

    int updateAllGroupChannelTagToInactive(String groupChannelId);

    List<GroupChannelCollectionItem> findAllGroupChannelCollections(String owner);

    int getMaxOrder();

    int addGroupChannelCollection(GroupChannelCollectionItem groupChannelCollectionItem);

    int removeGroupChannelCollection(String uuid, String owner);

    int updateGroupChannelCollectionOrder(String uuid, String owner, int order);
}
