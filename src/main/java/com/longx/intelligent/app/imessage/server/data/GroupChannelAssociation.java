package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2025/4/20 at 2:02 AM.
 */
public class GroupChannelAssociation {
    private String associationId;
    private String groupChannelId;
    private String owner;
    private Channel requester;
    private String requestMessage;
    private Date requestTime;
    private Date acceptTime;
    private String inviteUuid;

    public GroupChannelAssociation() {
    }

    public GroupChannelAssociation(String associationId, String groupChannelId, String owner, Channel requester, String requestMessage, Date requestTime, Date acceptTime, boolean isActive, String inviteUuid) {
        this.associationId = associationId;
        this.groupChannelId = groupChannelId;
        this.owner = owner;
        this.requester = requester;
        this.requestMessage = requestMessage;
        this.requestTime = requestTime;
        this.acceptTime = acceptTime;
        this.inviteUuid = inviteUuid;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getGroupChannelId() {
        return groupChannelId;
    }

    public String getOwner() {
        return owner;
    }

    public Channel getRequester() {
        return requester;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public Date getAcceptTime() {
        return acceptTime;
    }

    public String getInviteUuid() {
        return inviteUuid;
    }
}
