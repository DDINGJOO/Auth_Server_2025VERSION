package com.teambiund.bander.auth_server.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Abstraction for error codes used across the application. Existing enums or classes can implement
 * this to be usable with common exception handling.
 */
public interface ErrorCodeType {
  String getErrCode();

  String getMessage();

  HttpStatus getStatus();
}
