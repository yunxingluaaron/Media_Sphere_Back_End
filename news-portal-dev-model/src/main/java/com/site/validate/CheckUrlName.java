package com.site.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckUrlNameValidate.class)
public @interface CheckUrlName {

    String message() default "Illegal link name! The link name cannot contains blank and should has a length between 6 to 16.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}