package com.banka.banka.anotasyon;

import com.banka.banka.validation.IbanValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IbanValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIban {

    String message() default "Geçersiz IBAN formatı";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
