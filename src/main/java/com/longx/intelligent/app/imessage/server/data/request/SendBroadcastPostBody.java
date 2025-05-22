package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.BroadcastPermission;
import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Created by LONG on 2024/7/28 at 2:05 AM.
 */
@Validated
public class SendBroadcastPostBody {

    @Size(max = Constants.MAX_BROADCAST_TEXT_LENGTH, message = "正文不能超过" + Constants.MAX_BROADCAST_TEXT_LENGTH + "位")
    private String text;

    private List<Integer> mediaTypes;

    private List<String> mediaExtensions;

    private BroadcastPermission broadcastPermission;

    public SendBroadcastPostBody() {
    }

    public SendBroadcastPostBody(String text, List<Integer> mediaTypes, List<String> mediaExtensions, BroadcastPermission broadcastPermission) {
        this.text = text;
        this.mediaTypes = mediaTypes;
        this.mediaExtensions = mediaExtensions;
        this.broadcastPermission = broadcastPermission;
    }

    public String getText() {
        return text;
    }

    public List<Integer> getMediaTypes() {
        return mediaTypes;
    }

    public List<String> getMediaExtensions() {
        return mediaExtensions;
    }

    public BroadcastPermission getBroadcastPermission() {
        return broadcastPermission;
    }
}
