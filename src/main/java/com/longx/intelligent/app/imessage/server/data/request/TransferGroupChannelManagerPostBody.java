package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2025/6/20 at 5:22 AM.
 */
@Validated
public class TransferGroupChannelManagerPostBody {
    @NotBlank(message = "参数不合法")
    @NotNull(message = "参数不合法")
    private String toTransferGroupChannelId;
    @NotBlank(message = "参数不合法")
    @NotNull(message = "参数不合法")
    private String transferToChannelId;

    public TransferGroupChannelManagerPostBody() {
    }

    public TransferGroupChannelManagerPostBody(String toTransferGroupChannelId, String transferToChannelId) {
        this.toTransferGroupChannelId = toTransferGroupChannelId;
        this.transferToChannelId = transferToChannelId;
    }

    public String getToTransferGroupChannelId() {
        return toTransferGroupChannelId;
    }

    public String getTransferToChannelId() {
        return transferToChannelId;
    }
}
