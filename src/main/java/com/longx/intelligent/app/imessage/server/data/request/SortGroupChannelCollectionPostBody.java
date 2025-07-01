package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Created by LONG on 2025/7/2 at 1:35 AM.
 */
@Validated
public class SortGroupChannelCollectionPostBody {

    @NotNull(message = "数据不能为空")
    private Map<String, Integer> orderMap;

    public SortGroupChannelCollectionPostBody() {
    }

    public SortGroupChannelCollectionPostBody(Map<String, Integer> orderMap) {
        this.orderMap = orderMap;
    }

    public Map<String, Integer> getOrderMap() {
        return orderMap;
    }
}
