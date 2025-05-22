package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/8/21 at 4:23 PM.
 */
public class VideoInfo {
    private Size size;
    private long duration;

    public VideoInfo(Size size, long duration) {
        this.size = size;
        this.duration = duration;
    }

    public Size getSize() {
        return size;
    }

    public long getDuration() {
        return duration;
    }
}
