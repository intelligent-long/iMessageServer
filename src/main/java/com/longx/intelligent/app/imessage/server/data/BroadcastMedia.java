package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/8/6 at 7:08 PM.
 */
public class BroadcastMedia {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    private String mediaId;
    private String broadcastId;
    private byte[] media;
    private int type;
    private String extension;
    private int index;
    private Size size;
    private Long videoDuration;

    public BroadcastMedia() {
    }

    public BroadcastMedia(String mediaId, String broadcastId, byte[] media, int type, String extension, int index, Size size, Long videoDuration) {
        this.mediaId = mediaId;
        this.broadcastId = broadcastId;
        this.media = media;
        this.type = type;
        this.extension = extension;
        this.index = index;
        this.size = size;
        this.videoDuration = videoDuration;
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public byte[] getMedia() {
        return media;
    }

    public int getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }

    public int getIndex() {
        return index;
    }

    public Size getSize() {
        return size;
    }

    public Long getVideoDuration() {
        return videoDuration;
    }
}
