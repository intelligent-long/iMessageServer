package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2025/7/13 at 4:28 PM.
 */
public class GroupChatMessage {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_VOICE = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_FILE = 4;
    public static final int TYPE_NOTICE = 5;
    public static final int TYPE_UNSEND = 6;
    public static final int TYPE_MESSAGE_EXPIRED = 7;
    private int type;
    private String uuid;
    private String from;
    private String to;
    private Date time;
    private String text;
    private String fileName;
    private String imageId;
    private String fileId;
    private String videoId;
    private String voiceId;
    private String unsendMessageUuid;
    private Integer expiredMessageCount;

    public GroupChatMessage() {
    }

    public GroupChatMessage(int type, String uuid, String from, String to, Date time, String text, String fileName, String imageId,
                       String fileId, String videoId, String voiceId, String unsendMessageUuid, Integer expiredMessageCount) {
        this.type = type;
        this.uuid = uuid;
        this.from = from;
        this.to = to;
        this.text = text;
        this.time = time;
        this.fileName = fileName;
        this.imageId = imageId;
        this.fileId = fileId;
        this.videoId = videoId;
        this.voiceId = voiceId;
        this.unsendMessageUuid = unsendMessageUuid;
        this.expiredMessageCount = expiredMessageCount;
    }

    public static GroupChatMessage newText(String uuid, String from, String to, Date time, String text){
        return new GroupChatMessage(TYPE_TEXT, uuid, from, to, time, text, null, null, null, null, null, null, null);
    }

    public static GroupChatMessage newImage(String uuid, String from, String to, Date time, String fileName, String imageId){
        return new GroupChatMessage(TYPE_IMAGE, uuid, from, to, time, null, fileName, imageId, null, null, null, null, null);
    }

    public static GroupChatMessage newFile(String uuid, String from, String to, Date time, String fileName, String fileId){
        return new GroupChatMessage(TYPE_FILE, uuid, from, to, time, null, fileName, null, fileId, null, null, null, null);
    }

    public static GroupChatMessage newVideo(String uuid, String from, String to, Date time, String fileName, String videoId){
        return new GroupChatMessage(TYPE_VIDEO, uuid, from, to, time, null, fileName, null, null, videoId, null, null, null);
    }

    public static GroupChatMessage newVoice(String uuid, String from, String to, Date time, String voiceId){
        return new GroupChatMessage(TYPE_VOICE, uuid, from, to, time, null, null, null, null, null, voiceId, null, null);
    }

    public static GroupChatMessage newUnsend(String uuid, String from, String to, Date time, String unsendMessageUuid){
        return new GroupChatMessage(TYPE_UNSEND, uuid, from, to, time, null, null, null, null, null, null, unsendMessageUuid, null);
    }

    public static GroupChatMessage newMessageExpired(String uuid, String from, String to, Date time, Integer expiredMessageCount){
        return new GroupChatMessage(TYPE_MESSAGE_EXPIRED, uuid, from, to, time, null, null, null, null, null, null, null, expiredMessageCount);
    }

    public int getType() {
        return type;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
    }

    public String getFileName() {
        return fileName;
    }

    public String getImageId() {
        return imageId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public String getUnsendMessageUuid() {
        return unsendMessageUuid;
    }

    public Integer getExpiredMessageCount() {
        return expiredMessageCount;
    }

    @Override
    public String toString() {
        return "GroupChatMessage{" +
                "type=" + type +
                ", uuid='" + uuid + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", time=" + time +
                ", text='" + text + '\'' +
                ", fileName='" + fileName + '\'' +
                ", imageId='" + imageId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", videoId='" + videoId + '\'' +
                ", voiceId='" + voiceId + '\'' +
                ", unsendMessageUuid='" + unsendMessageUuid + '\'' +
                ", expiredMessageCount=" + expiredMessageCount +
                '}';
    }
}
