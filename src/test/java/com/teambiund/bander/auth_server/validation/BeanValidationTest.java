package com.teambiund.bander.auth_server.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.dto.request.LoginRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SuspendRequest;
import com.teambiund.bander.auth_server.auth.dto.request.TokenRefreshRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Bean Validation 테스트")
class BeanValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("SignupRequest - 유효한 요청")
    void signupRequest_Valid() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Test1234!@#$");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SignupRequest - 이메일 형식 오류")
    void signupRequest_InvalidEmail() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("invalid-email");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Test1234!@#$");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("SignupRequest - 이메일 필드 누락")
    void signupRequest_BlankEmail() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Test1234!@#$");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("이메일은 필수 입력 항목입니다"))).isTrue();
    }

    @Test
    @DisplayName("SignupRequest - 비밀번호 형식 오류 (특수문자 없음)")
    void signupRequest_InvalidPassword_NoSpecialChar() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234");
        request.setPasswordConfirm("Test1234");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다");
    }

    @Test
    @DisplayName("SignupRequest - 비밀번호 형식 오류 (8자 미만)")
    void signupRequest_InvalidPassword_TooShort() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Te1!");
        request.setPasswordConfirm("Te1!");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다");
    }

    @Test
    @DisplayName("SignupRequest - 비밀번호 불일치")
    void signupRequest_PasswordMismatch() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Different1234!@#$");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest consent = new ConsentRequest();
        consent.setConsentId("consent-id-1");
        consent.setConsented(true);
        consents.add(consent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("비밀번호와 비밀번호 확인이 일치하지 않습니다");
    }

    @Test
    @DisplayName("SignupRequest - 동의 항목 누락")
    void signupRequest_NullConsent() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Test1234!@#$");
        request.setConsentReqs(null);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // @NotNull + @RequiredConsents
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("동의 항목은 필수입니다"))).isTrue();
    }

    @Test
    @DisplayName("LoginRequest - 유효한 요청")
    void loginRequest_Valid() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234!@#$");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("LoginRequest - 이메일 누락")
    void loginRequest_BlankEmail() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("Test1234!@#$");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("이메일은 필수 입력 항목입니다"))).isTrue();
    }

    @Test
    @DisplayName("LoginRequest - 비밀번호 누락")
    void loginRequest_BlankPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("비밀번호는 필수 입력 항목입니다");
    }

    @Test
    @DisplayName("ConsentRequest - 유효한 요청")
    void consentRequest_Valid() {
        // Given
        ConsentRequest request = new ConsentRequest();
        request.setConsentId("consent-id-1");
        request.setConsented(true);

        // When
        Set<ConstraintViolation<ConsentRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ConsentRequest - 동의 항목 ID 누락")
    void consentRequest_BlankConsentId() {
        // Given
        ConsentRequest request = new ConsentRequest();
        request.setConsentId("");
        request.setConsented(true);

        // When
        Set<ConstraintViolation<ConsentRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("동의 항목 ID는 필수입니다");
    }

    @Test
    @DisplayName("SuspendRequest - 유효한 요청")
    void suspendRequest_Valid() {
        // Given
        SuspendRequest request = SuspendRequest.builder()
                .suspendReason("부적절한 행위")
                .suspenderUserId("admin123")
                .suspendedUserId("user123")
                .suspendDay(7L)
                .build();

        // When
        Set<ConstraintViolation<SuspendRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SuspendRequest - 정지 사유 누락")
    void suspendRequest_BlankReason() {
        // Given
        SuspendRequest request = SuspendRequest.builder()
                .suspendReason("")
                .suspenderUserId("admin123")
                .suspendedUserId("user123")
                .suspendDay(7L)
                .build();

        // When
        Set<ConstraintViolation<SuspendRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("정지 사유는 필수입니다");
    }

    @Test
    @DisplayName("SuspendRequest - 정지 일수 최소값 위반")
    void suspendRequest_InvalidSuspendDay() {
        // Given
        SuspendRequest request = SuspendRequest.builder()
                .suspendReason("부적절한 행위")
                .suspenderUserId("admin123")
                .suspendedUserId("user123")
                .suspendDay(0L)
                .build();

        // When
        Set<ConstraintViolation<SuspendRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("정지 일수는 최소 1일 이상이어야 합니다");
    }

    @Test
    @DisplayName("TokenRefreshRequest - 유효한 요청")
    void tokenRefreshRequest_Valid() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("valid-refresh-token");
        request.setDeviceId("device-123");

        // When
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("TokenRefreshRequest - 리프레시 토큰 누락")
    void tokenRefreshRequest_BlankRefreshToken() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("");
        request.setDeviceId("device-123");

        // When
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("리프레시 토큰은 필수입니다");
    }

    @Test
    @DisplayName("TokenRefreshRequest - 디바이스 ID 누락")
    void tokenRefreshRequest_BlankDeviceId() {
        // Given
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("valid-refresh-token");
        request.setDeviceId("");

        // When
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("디바이스 ID는 필수입니다");
    }

    @Test
    @DisplayName("SignupRequest - 중첩된 ConsentRequest 검증")
    void signupRequest_NestedConsentValidation() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test1234!@#$");
        request.setPasswordConfirm("Test1234!@#$");

        List<ConsentRequest> consents = new ArrayList<>();
        ConsentRequest invalidConsent = new ConsentRequest();
        invalidConsent.setConsentId("");  // 유효하지 않은 값
        invalidConsent.setConsented(true);
        consents.add(invalidConsent);
        request.setConsentReqs(consents);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("동의 항목 ID는 필수입니다");
    }

    @Test
    @DisplayName("SignupRequest - 다중 검증 오류")
    void signupRequest_MultipleValidationErrors() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("invalid-email");
        request.setPassword("weak");
        request.setPasswordConfirm("weak");
        request.setConsentReqs(null);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(4); // 이메일, 비밀번호, @NotNull 동의항목, @RequiredConsents
    }
}
