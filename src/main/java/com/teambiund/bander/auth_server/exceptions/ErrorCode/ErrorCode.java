package com.teambiund.bander.auth_server.exceptions.ErrorCode;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {


    EMAIL_NOT_FOUND("EMAIL_NOT_FOUND", "Email not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH("PASSWORD_NOT_MATCH", "Password not match", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid token", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.BAD_REQUEST),
    EMAIL_REGEX_NOT_MATCH("EMAIL_REGEX_NOT_MATCH", "Email regex not match" , HttpStatus.BAD_REQUEST ),
    PASSWORD_REGEX_NOT_MATCH("PASSWORD_REGEX_NOT_MATCH", "Password regex not match" , HttpStatus.BAD_REQUEST),
    PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED("PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED", "Password and password confirm not confirmed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found" , HttpStatus.NOT_FOUND ),
    WITHDRAW_NOT_FOUND("WITHDRAW_NOT_FOUND", "Withdraw not found" , HttpStatus.NOT_FOUND ),
    NOT_ADMIN("NOT_ADMIN", "Not admin" , HttpStatus.FORBIDDEN),
    USER_ALREADY_BLOCKED("USER_ALREADY_BLOCKED", "User already blocked", HttpStatus.BAD_REQUEST),
    USER_NOT_SUSPENDED("USER_NOT_SUSPENDED", "User not suspended", HttpStatus.BAD_REQUEST);




    private final String errCode;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String errCode, String message, HttpStatus status) {

        this.status = status;
        this.errCode = errCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                " status='" + status + '\'' +
                "errCode='" + errCode + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
