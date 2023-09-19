package com.site.validate;

import com.site.utils.UrlNameUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckUrlNameValidate implements ConstraintValidator<CheckUrlName, String> {

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return UrlNameUtil.verifyUrlName(name.trim());
    }
}
