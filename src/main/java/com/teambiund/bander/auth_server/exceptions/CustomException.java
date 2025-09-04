package com.teambiund.bander.auth_server.exceptions;

import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import org.springframework.http.HttpStatus;

public class CustomException extends Exception {
    private final ErrorCode errorcode;
    public CustomException(ErrorCode errorcode) {

        super(errorcode.toString());
        this.errorcode = errorcode;
    }
    public HttpStatus getStatus() {
        return errorcode.getStatus();
    }
}
