package com.longx.intelligent.app.imessage.server.data.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Created by LONG on 2024/6/4 at 10:41 AM.
 */
@Validated
public class SortGroupTagsPostBody {

    @NotNull(message = "数据不能为空")
    private Map<String, Integer> orderMap;

    public SortGroupTagsPostBody() {
    }

    public SortGroupTagsPostBody(Map<String, Integer> orderMap) {
        this.orderMap = orderMap;
    }

    public Map<String, Integer> getOrderMap() {
        return orderMap;
    }
}
