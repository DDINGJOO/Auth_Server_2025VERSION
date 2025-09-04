package com.teambiund.bander.auth_server.util.vailidator.impl;

import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class ValidatorImpl implements Validator {

    private static final String DEFAULT_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String DEFAULT_PASSWORD_REGEX = "^(?=.{8,}$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]).+$";

    @Value("${regex.email:}")
    private String emailRegex = DEFAULT_EMAIL_REGEX; // fallback when not injected

    @Value("${regex.password:}")
    private String passwordRegex = DEFAULT_PASSWORD_REGEX; // fallback when not injected

    // No-arg constructor keeps defaults for plain instantiation in unit tests

    @Override
    public void emailValid(String email) throws CustomException {
        String pattern = (emailRegex == null || emailRegex.isEmpty()) ? DEFAULT_EMAIL_REGEX : emailRegex;
        if (email == null || !email.matches(pattern)) {
            throw new CustomException(ErrorCode.EMAIL_REGEX_NOT_MATCH);
        }
    }

    @Override
    public void passwordValid(String password) throws CustomException{
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
}
