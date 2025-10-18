package com.teambiund.bander.auth_server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 한국 전화번호 형식 검증 구현체
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final String PHONE_NUMBER_REGEX = "^010[0-9]{8}$";

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return false;
        }

        return phoneNumber.matches(PHONE_NUMBER_REGEX);
    }
}
