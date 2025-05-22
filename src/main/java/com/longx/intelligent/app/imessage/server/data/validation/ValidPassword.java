package com.longx.intelligent.app.imessage.server.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by LONG on 2024/3/31 at 6:23 PM.
 */
@Size(min = 8, max = 16, message = "密码应不少于8位，不超过16位")
@Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "密码只能包含英文字母、数字、'.'、'-'和'_'")
@NotNull(message = "密码不能为空")
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface ValidPassword {
    String message() default "密码不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
