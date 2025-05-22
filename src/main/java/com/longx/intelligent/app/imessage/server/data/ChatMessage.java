package com.longx.intelligent.app.imessage.server.data;

import java.util.Date;

/**
 * Created by LONG on 2024/5/13 at 12:08 AM.
 */
public class ChatMessage {
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

    public ChatMessage() {
    }

    public ChatMessage(int type, String uuid, String from, String to, Date time, String text, String fileName, String imageId,
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

    public static ChatMessage newText(String uuid, String from, String to, Date time, String text){
        return new ChatMessage(TYPE_TEXT, uuid, from, to, time, text, null, null, null, null, null, null, null);
    }

    public static ChatMessage newImage(String uuid, String from, String to, Date time, String fileName, String imageId){
        return new ChatMessage(TYPE_IMAGE, uuid, from, to, time, null, fileName, imageId, null, null, null, null, null);
    }

    public static ChatMessage newFile(String uuid, String from, String to, Date time, String fileName, String fileId){
        return new ChatMessage(TYPE_FILE, uuid, from, to, time, null, fileName, null, fileId, null, null, null, null);
    }

    public static ChatMessage newVideo(String uuid, String from, String to, Date time, String fileName, String videoId){
        return new ChatMessage(TYPE_VIDEO, uuid, from, to, time, null, fileName, null, null, videoId, null, null, null);
    }

    public static ChatMessage newVoice(String uuid, String from, String to, Date time, String voiceId){
        return new ChatMessage(TYPE_VOICE, uuid, from, to, time, null, null, null, null, null, voiceId, null, null);
    }

    public static ChatMessage newUnsend(String uuid, String from, String to, Date time, String unsendMessageUuid){
        return new ChatMessage(TYPE_UNSEND, uuid, from, to, time, null, null, null, null, null, null, unsendMessageUuid, null);
    }

    public static ChatMessage newMessageExpired(String uuid, String from, String to, Date time, Integer expiredMessageCount){
        return new ChatMessage(TYPE_MESSAGE_EXPIRED, uuid, from, to, time, null, null, null, null, null, null, null, expiredMessageCount);
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
}
