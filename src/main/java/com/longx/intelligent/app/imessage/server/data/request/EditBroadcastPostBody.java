package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Created by LONG on 2024/8/29 at 12:03 AM.
 */
@Validated
public class EditBroadcastPostBody {
    @NotBlank
    private String broadcastId;
    @Size(max = Constants.MAX_BROADCAST_TEXT_LENGTH, message = "正文不能超过" + Constants.MAX_BROADCAST_TEXT_LENGTH + "位")
    private String newText;
    private Map<String, Integer> leftMedias;
    private List<Integer> addMediaTypes;
    private List<String> addMediaExtensions;
    private List<Integer> addMediaIndexes;

    public EditBroadcastPostBody() {
    }

    public EditBroadcastPostBody(String broadcastId, String newText, List<Integer> addMediaTypes, List<String> addMediaExtensions, List<Integer> addMediaIndexes, Map<String, Integer> leftMedias) {
        this.broadcastId = broadcastId;
        this.newText = newText;
        this.addMediaTypes = addMediaTypes;
        this.addMediaExtensions = addMediaExtensions;
        this.addMediaIndexes = addMediaIndexes;
        this.leftMedias = leftMedias;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public String getNewText() {
        return newText;
    }

    public List<Integer> getAddMediaTypes() {
        return addMediaTypes;
    }

    public List<String> getAddMediaExtensions() {
        return addMediaExtensions;
    }

    public List<Integer> getAddMediaIndexes() {
        return addMediaIndexes;
    }

    public Map<String, Integer> getLeftMedias() {
        return leftMedias;
    }
}
