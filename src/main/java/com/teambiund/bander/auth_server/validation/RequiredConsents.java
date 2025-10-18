package com.teambiund.bander.auth_server.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** 필수 동의항목 검증 어노테이션 - 필수 동의항목이 모두 포함되어 있는지 확인 - 필수 항목은 consented = true 여야 함 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredConsentsValidator.class)
@Documented
public @interface RequiredConsents {
  String message() default "필수 동의 항목에 동의하지 않았습니다";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
