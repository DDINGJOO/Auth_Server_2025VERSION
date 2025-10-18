package com.teambiund.bander.auth_server.validation;

import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignupRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(SignupRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        String password = request.getPassword();
        String passwordConfirm = request.getPasswordConfirm();

        if (password == null || passwordConfirm == null) {
            return false;
        }

        boolean isValid = password.equals(passwordConfirm);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("passwordConfirm")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
