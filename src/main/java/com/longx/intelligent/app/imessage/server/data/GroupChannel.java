package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2025/4/15 at 10:49 PM.
 */
public class GroupChannel {
    private GroupAvatar groupAvatar;
    private String groupChannelId;
    private String groupChannelIdUser;
    private String owner;
    private String name;
    private String note;
    private Date createTime;
    private List<GroupChannelAssociation> groupChannelAssociations;
    private Region firstRegion;
    private Region secondRegion;
    private Region thirdRegion;
    private String avatarHash;
    private Boolean groupJoinVerification;

    public GroupChannel() {
    }

    public GroupChannel(GroupAvatar groupAvatar, String groupChannelId, String groupChannelIdUser, String owner, String name,
                        String note, Date createTime, List<GroupChannelAssociation> groupChannelAssociations, Region firstRegion,
                        Region secondRegion, Region thirdRegion, String avatarHash, Boolean groupJoinVerification) {
        this.groupAvatar = groupAvatar;
        this.groupChannelId = groupChannelId;
        this.groupChannelIdUser = groupChannelIdUser;
        this.owner = owner;
        this.name = name;
        this.note = note;
        this.createTime = createTime;
        this.groupChannelAssociations = groupChannelAssociations;
        this.firstRegion = firstRegion;
        this.secondRegion = secondRegion;
        this.thirdRegion = thirdRegion;
        this.avatarHash = avatarHash;
        this.groupJoinVerification = groupJoinVerification;
    }

    public GroupAvatar getGroupAvatar() {
        return groupAvatar;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getGroupChannelIdUser() {
        return groupChannelIdUser;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public List<GroupChannelAssociation> getGroupChannelAssociations() {
        return groupChannelAssociations;
    }

    public Region getFirstRegion() {
        return firstRegion;
    }

    public Region getSecondRegion() {
        return secondRegion;
    }

    public Region getThirdRegion() {
        return thirdRegion;
    }

    public String getAvatarHash() {
        return avatarHash;
    }

    public Boolean getGroupJoinVerification() {
        return groupJoinVerification;
    }

    @Override
    public String toString() {
        return "GroupChannel{" +
                "groupAvatar=" + groupAvatar +
                ", groupChannelId='" + groupChannelId + '\'' +
                ", groupChannelIdUser='" + groupChannelIdUser + '\'' +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                ", createTime=" + createTime +
                ", groupChannelAssociations=" + groupChannelAssociations +
                ", firstRegion=" + firstRegion +
                ", secondRegion=" + secondRegion +
                ", thirdRegion=" + thirdRegion +
                ", avatarHash='" + avatarHash + '\'' +
                ", groupJoinVerification=" + groupJoinVerification +
                '}';
    }
}
