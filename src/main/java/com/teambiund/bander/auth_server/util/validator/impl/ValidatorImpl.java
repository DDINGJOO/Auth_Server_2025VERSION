package com.teambiund.bander.auth_server.util.validator.impl;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.consents_name.ConsentsTable;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.util.validator.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.teambiund.bander.auth_server.util.data.ConsentTable_init.requiredConsents;


@Component
public class ValidatorImpl implements Validator {

    private static final java.lang.String DEFAULT_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final java.lang.String DEFAULT_PASSWORD_REGEX = "^(?=.{8,}$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]).+$";

    private static final java.lang.String PHONE_NUMBER_REGEX = "^[0-9]{11}$";
    private final java.lang.String phoneNumberRegex = PHONE_NUMBER_REGEX;
    @Value("${regex.email:}")
    private java.lang.String emailRegex = DEFAULT_EMAIL_REGEX; // fallback when not injected
    @Value("${regex.password:}")
    private java.lang.String passwordRegex = DEFAULT_PASSWORD_REGEX; // fallback when not injected


    @Override
    public void emailValid(java.lang.String email) {
        java.lang.String pattern = (emailRegex == null || emailRegex.isEmpty()) ? DEFAULT_EMAIL_REGEX : emailRegex;
        if (email == null || !email.matches(pattern)) {
            throw new CustomException(ErrorCode.EMAIL_REGEX_NOT_MATCH);
        }
    }


    @Override
    public void passwordValid(java.lang.String password) {
        java.lang.String pattern = (passwordRegex == null || passwordRegex.isEmpty()) ? DEFAULT_PASSWORD_REGEX : passwordRegex;
        if (password == null || !password.matches(pattern)) {
            throw new CustomException(ErrorCode.PASSWORD_REGEX_NOT_MATCH);
        }
    }

    @Override
    public void passConfirmValid(java.lang.String password, java.lang.String passConfirm) throws CustomException {
        if (password == null || passConfirm == null || !password.equals(passConfirm))
        {
            throw new CustomException(ErrorCode.PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED);
        }
    }

    @Override
    public void requiredValid(List<String> value) throws CustomException {

    }

    @Override
    public boolean validateConsentList(List<ConsentRequest> reqs) {
        for (ConsentRequest req : reqs) {
            if (!req.isConsented()) {
                for (ConsentsTable consentsTable : requiredConsents) {
                    if (consentsTable.getConsentName().equals(req.getConsentName())) {
                        if (consentsTable.getVersion().equals(req.getVersion())) {
                            return false;
                        }
                    }
                }
                throw new CustomException(ErrorCode.NOT_CONSENTED_REQUIRED_CONSENT);
            }
        }

        return true;
    }


    @Override
    public boolean validatePhoneNumber(java.lang.String phoneNumber) throws CustomException {
        if (phoneNumber == null || !phoneNumber.matches(phoneNumberRegex) || !phoneNumber.startsWith("010")) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_REGEX_NOT_MATCH);
        }
        return true;
    }
}
