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
 * Created by LONG on 2024/3/31 at 6:20 PM.
 */
@Size(min = 6, max = 6, message = "验证码必须是6位")
@Pattern(regexp = "^[0-9]*$", message = "验证码只能为数字")
@NotNull(message = "验证码不能为空")
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface ValidVerifyCode {
    String message() default "验证码不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
