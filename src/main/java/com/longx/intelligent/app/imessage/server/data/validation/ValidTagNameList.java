package com.longx.intelligent.app.imessage.server.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by LONG on 2024/6/6 at 5:20 PM.
 */
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@ValidStringList(minLength = 1, maxLength = 30, notEmpty = true, message = "标签名应不少于1位，不超过30位")
public @interface ValidTagNameList {

    String message() default "标签名应不少于1位，不超过30位";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
