package com.teambiund.bander.auth_server.auth.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
  private final ErrorCodeType errorcode;

  public CustomException(ErrorCodeType errorcode) {

    super(errorcode.toString());
    this.errorcode = errorcode;
  }

  public HttpStatus getStatus() {
    return errorcode.getStatus();
  }
}
