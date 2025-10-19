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
  EMAIL_REGEX_NOT_MATCH("EMAIL_REGEX_NOT_MATCH", "Email regex not match", HttpStatus.BAD_REQUEST),
  PASSWORD_REGEX_NOT_MATCH(
      "PASSWORD_REGEX_NOT_MATCH", "Password regex not match", HttpStatus.BAD_REQUEST),
  PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED(
      "PASSWORD_AND_PASSWORD_CONFIRM_NOT_CONFIRMED",
      "Password and password confirm not confirmed",
      HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND),
  WITHDRAW_NOT_FOUND("WITHDRAW_NOT_FOUND", "Withdraw not found", HttpStatus.NOT_FOUND),
  NOT_ADMIN("NOT_ADMIN", "Not admin", HttpStatus.FORBIDDEN),
  USER_ALREADY_BLOCKED("USER_ALREADY_BLOCKED", "User already blocked", HttpStatus.BAD_REQUEST),
  USER_NOT_SUSPENDED("USER_NOT_SUSPENDED", "User not suspended", HttpStatus.BAD_REQUEST),
  REQUIRED_CONSENT_NOT_PROVIDED(
      "REQUIRED_CONSENT_NOT_PROVIDED", "Required consent not provided", HttpStatus.BAD_REQUEST),
  CONSENT_NOT_FOUND("CONSENT_NOT_FOUND", "Consent not found", HttpStatus.NOT_FOUND),
  INVALID_CODE("INVALID_CODE", "Invalid code", HttpStatus.BAD_REQUEST),
  INVALID_USER("INVALID_USER", "Invalid user", HttpStatus.BAD_REQUEST),
  CONSENT_NOT_VALID("CONSENT_NOT_VALID", "Consent not valid", HttpStatus.BAD_REQUEST),
  PHONE_NUMBER_REGEX_NOT_MATCH(
      "PHONE_NUMBER_REGEX_NOT_MATCH", "Phone number regex not match", HttpStatus.BAD_REQUEST),
  CAN_NOT_RESEND_EMAIl("CAN_NOT_RESEND_EMAIL", "Can not resend email", HttpStatus.BAD_REQUEST),
  ALREADY_GENERATE_CODE("ALREADY_GENERATE_CODE", "Already generate code", HttpStatus.BAD_REQUEST),
  MARKETING_CONSENT_NOT_PROVIDED(
      "MARKETING_CONSENT_NOT_PROVIDED", "Marketing consent not provided", HttpStatus.BAD_REQUEST),
  PERSONAL_INFO_NOT_PROVIDED(
      "PERSONAL_INFO_NOT_PROVIDED", "Personal info not provided", HttpStatus.BAD_REQUEST),
  PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Password mismatch", HttpStatus.BAD_REQUEST),
  USER_IS_SLEEPING("USER_IS_SLEEPING", "User is sleeping", HttpStatus.BAD_REQUEST),
  USER_IS_BLOCKED("USER_IS_BLOCKED", "User is blocked", HttpStatus.BAD_REQUEST),
  USER_IS_DELETED("USER_IS_DELETED", "User is deleted", HttpStatus.BAD_REQUEST),
  USER_IS_SUSPENDED("USER_IS_SUSPENDED", "User is suspended", HttpStatus.BAD_REQUEST),
  INVALID_DEVICE_ID("INVALID_DEVICE_ID", "Invalid device id", HttpStatus.BAD_REQUEST),
  EXPIRED_TOKEN("EXPIRED_TOKEN", "Expired token", HttpStatus.BAD_REQUEST),
  NOT_CONFIRMED_EMAIL("NOT_CONFIRMED_EMAIL", "Not confirmed email", HttpStatus.BAD_REQUEST),
  NOT_CONSENTED_REQUIRED_CONSENT(
      "NOT_CONSENTED_REQUIRED_CONSENT", "Not confirmed consent", HttpStatus.BAD_REQUEST),
  ENCRYPTION_ERROR("ENCRYPTION_ERROR", "Encryption error", HttpStatus.INTERNAL_SERVER_ERROR),
  DECRYPTION_ERROR("DECRYPTION_ERROR", "Decryption error", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_EMAIL("INVALID_EMAIL", "Invalid email", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD("INVALID_PASSWORD", "Invalid password", HttpStatus.BAD_REQUEST),
  SOCIAL_LOGIN_FAILED("SOCIAL_LOGIN_FAILED", "Social login failed", HttpStatus.UNAUTHORIZED),
  ;

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
    return "ErrorCode{"
        + " status='"
        + status
        + '\''
        + "errCode='"
        + errCode
        + '\''
        + ", message='"
        + message
        + '\''
        + '}';
  }
}
