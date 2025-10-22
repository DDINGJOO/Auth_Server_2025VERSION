package com.teambiund.bander.auth_server.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {
  String message() default "비밀번호와 비밀번호 확인이 일치하지 않습니다";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
