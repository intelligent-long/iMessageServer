package com.longx.intelligent.app.imessage.server.data.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Created by LONG on 2024/6/6 at 5:16 PM.
 */
public class StringListValidator implements ConstraintValidator<ValidStringList, List<String>> {
    private int minLength;
    private int maxLength;
    private boolean notEmpty;

    @Override
    public void initialize(ValidStringList constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.notEmpty = constraintAnnotation.notEmpty();
    }

    @Override
    public boolean isValid(List<String> stringList, ConstraintValidatorContext context) {
        if (stringList == null) {
            return true;
        }

        for (String str : stringList) {
            if (str == null || (notEmpty && str.trim().isEmpty())) {
                return false;
            }
            if (str.length() < minLength || str.length() > maxLength) {
                return false;
            }
        }
        return true;
    }
}
