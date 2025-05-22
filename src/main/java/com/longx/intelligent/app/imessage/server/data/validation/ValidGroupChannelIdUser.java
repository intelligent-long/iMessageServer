package com.longx.intelligent.app.imessage.server.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by LONG on 2024/3/31 at 6:20 PM.
 */
@Size(min=2, max=30, message = "群频道 ID 应不少于2位，不超过30位")
@NotNull(message = "群频道 ID 不能为空")
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface ValidGroupChannelIdUser {
    String message() default "群频道 ID 不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
