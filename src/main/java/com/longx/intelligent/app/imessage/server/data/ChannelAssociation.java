package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2024/5/8 at 2:06 AM.
 */
public class ChannelAssociation {
    private String associationId;
    private String imessageId;
    private String channelImessageId;
    private boolean isRequester;
    private Date requestTime;
    private Date acceptTime;
    private boolean isActive;
    private Channel channel;
    private ChatMessageAllow chatMessageAllowToThem;
    private ChatMessageAllow chatMessageAllowToMe;

    public ChannelAssociation() {
    }

    public ChannelAssociation(String associationId, String imessageId, String channelImessageId, boolean isRequester, Date requestTime, Date acceptTime, boolean isActive, ChatMessageAllow chatMessageAllowToThem, ChatMessageAllow chatMessageAllowToMe) {
        this.associationId = associationId;
        this.imessageId = imessageId;
        this.channelImessageId = channelImessageId;
        this.isRequester = isRequester;
        this.requestTime = requestTime;
        this.acceptTime = acceptTime;
        this.isActive = isActive;
        this.chatMessageAllowToThem = chatMessageAllowToThem;
        this.chatMessageAllowToMe = chatMessageAllowToMe;
    }

    public ChannelAssociation(String associationId, String imessageId, String channelImessageId, boolean isRequester, Date requestTime, Date acceptTime, boolean isActive, ChatMessageAllow chatMessageAllowToThem, ChatMessageAllow chatMessageAllowToMe, Channel channel) {
        this(associationId, imessageId, channelImessageId, isRequester, requestTime, acceptTime, isActive, chatMessageAllowToThem, chatMessageAllowToMe);
        this.channel = channel;
    }

    public String getAssociationId() {
        return associationId;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getChannelImessageId() {
        return channelImessageId;
    }

    public boolean isRequester() {
        return isRequester;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public Date getAcceptTime() {
        return acceptTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChatMessageAllow getChatMessageAllowToThem() {
        return chatMessageAllowToThem;
    }

    public void setChatMessageAllowToThem(ChatMessageAllow chatMessageAllowToThem) {
        this.chatMessageAllowToThem = chatMessageAllowToThem;
    }

    public ChatMessageAllow getChatMessageAllowToMe() {
        return chatMessageAllowToMe;
    }

    public void setChatMessageAllowToMe(ChatMessageAllow chatMessageAllowToMe) {
        this.chatMessageAllowToMe = chatMessageAllowToMe;
    }
}
