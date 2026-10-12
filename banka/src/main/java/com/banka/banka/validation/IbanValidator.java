package com.banka.banka.validation;

import com.banka.banka.anotasyon.ValidIban;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public void initialize(ValidIban constraintAnnotation) {
    }

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null) {
            return false;
        }

        String trimmedIban = iban.trim();
        return trimmedIban.startsWith("TR") && trimmedIban.length() == 26;
    }
}
