package com.longx.intelligent.app.imessage.server.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;

/**
 * Created by LONG on 2025/5/14 at 11:28 PM.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // 使用类名作为类型标识
        include = JsonTypeInfo.As.PROPERTY, // 在 JSON 中作为一个字段
        property = "type" // 字段名叫 "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GroupChannelAddition.class, name = "GroupChannelAddition"),
        @JsonSubTypes.Type(value = GroupChannelInvitation.class, name = "GroupChannelInvitation")
})
/**
 * 上面的注解没有作用，不自动生成type字段，不知道原因
 */
public interface GroupChannelActivity {
    String uuid();
    String message();
    Date requestTime();
    Date respondTime();
    boolean isAccepted();
    boolean isViewed();
    boolean isExpired();
}
