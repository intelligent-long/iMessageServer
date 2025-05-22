package com.longx.intelligent.app.imessage.server.data.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Created by LONG on 2023/9/19 at 6:50 PM.
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {
    private String[] strValues;
    private int[] intValues;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        strValues = constraintAnnotation.strValues();
        intValues = constraintAnnotation.intValues();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof String) {
            for (String s : strValues) {
                if (s.equals(value)) {
                    return true;
                }
            }
        } else if (value instanceof Integer) {
            for (int s : intValues) {
                if (s == ((Integer) value).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }
}
