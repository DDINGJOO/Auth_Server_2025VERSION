package com.teambiund.bander.auth_server.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 한국 전화번호 형식 검증 어노테이션
 * - 11자리 숫자
 * - 010으로 시작
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {
    String message() default "올바른 전화번호 형식이 아닙니다 (010으로 시작하는 11자리 숫자)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
