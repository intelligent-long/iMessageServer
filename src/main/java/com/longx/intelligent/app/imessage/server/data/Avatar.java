package com.longx.intelligent.app.imessage.server.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by LONG on 2024/4/29 at 9:28 PM.
 */
public class Avatar implements Serializable {
    private String hash;
    private String imessageId;
    private String extension;
    private Date time;

    public Avatar() {
    }

    public Avatar(String hash, String imessageId, String extension, Date time) {
        this.hash = hash;
        this.imessageId = imessageId;
        this.extension = extension;
        this.time = time;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getExtension() {
        return extension;
    }

    public Date getTime() {
        return time;
    }

    public String getHash() {
        return hash;
    }
}
