package com.teambiund.bander.auth_server.util.vailidator.impl;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.enums.ConsentType;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;



@Component
public class ValidatorImpl implements Validator {

    private static final String DEFAULT_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String DEFAULT_PASSWORD_REGEX = "^(?=.{8,}$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]).+$";

    private static final String PHONE_NUMBER_REGEX = "^[0-9]{11}$";

    @Value("${regex.email:}")
    private String emailRegex = DEFAULT_EMAIL_REGEX; // fallback when not injected

    @Value("${regex.password:}")
    private String passwordRegex = DEFAULT_PASSWORD_REGEX; // fallback when not injected

    private final String phoneNumberRegex = PHONE_NUMBER_REGEX;
    private static final List<ConsentType> requiredList = List.of(ConsentType.PERSONAL_INFO);

    // No-arg constructor keeps defaults for plain instantiation in unit tests

    @Override
    public void emailValid(String email) {
        String pattern = (emailRegex == null || emailRegex.isEmpty()) ? DEFAULT_EMAIL_REGEX : emailRegex;
        if (email == null || !email.matches(pattern)) {
            throw new CustomException(ErrorCode.EMAIL_REGEX_NOT_MATCH);
        }
    }


    @Override
    public void passwordValid(String password) {
        String pattern = (passwordRegex == null || passwordRegex.isEmpty()) ? DEFAULT_PASSWORD_REGEX : passwordRegex;
        if (password == null || !password.matches(pattern)) {
            throw new CustomException(ErrorCode.PASSWORD_REGEX_NOT_MATCH);
        }
    }

    @Override
    public void passConfirmValid(String password, String passConfirm) throws CustomException {
        if (password == null || passConfirm == null || !password.equals(passConfirm))
        {
            throw new CustomException(ErrorCode.PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED);
        }
    }

    @Override
    public void requiredValid(List<ConsentType> value) throws CustomException {
        if (value == null || value.isEmpty() || !value.contains(ConsentType.PERSONAL_INFO)) {
            throw new CustomException(ErrorCode.REQUIRED_CONSENT_NOT_PROVIDED);
        }
    }

    @Override
    public boolean validateConsentList(List<ConsentRequest> reqs) {
        reqs.forEach(req -> {
            if (req.getConsent().equals(ConsentType.PERSONAL_INFO)) {
                if (req.isConsented() == false) {
                    throw new CustomException(ErrorCode.PERSONAL_INFO_NOT_PROVIDED);
                }
            }
        });
        return true;
    }

    @Override
    public boolean validatePhoneNumber(String phoneNumber) throws CustomException {
        String pattern = (phoneNumberRegex == null || phoneNumberRegex.isEmpty()) ? PHONE_NUMBER_REGEX : phoneNumberRegex;
        if (phoneNumber == null || !phoneNumber.matches(pattern) || !phoneNumber.startsWith("010")) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_REGEX_NOT_MATCH);
        }
        return true;
    }
}
