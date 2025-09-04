package com.teambiund.bander.auth_server.util;

import com.teambiund.bander.auth_server.exceptions.CustomException;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(ex.getMessage());
    }
}
