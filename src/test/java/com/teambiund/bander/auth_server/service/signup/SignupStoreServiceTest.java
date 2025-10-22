package com.teambiund.bander.auth_server.service.signup;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.signup.SignupStoreService;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignupStoreService 테스트")
class SignupStoreServiceTest {

  @Mock private AuthRepository authRepository;

  @Mock private KeyProvider keyProvider;

  @Mock private CipherStrategy passwordEncoder;

  @Mock private CipherStrategy emailCipher;

  private SignupStoreService signupStoreService;

  @BeforeEach
  void setUp() {
    // emailCipher.encrypt returns input for test simplicity
    when(emailCipher.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

    signupStoreService =
        new SignupStoreService(authRepository, keyProvider, passwordEncoder, emailCipher);
  }

  @Nested
  @DisplayName("회원가입 테스트 (이메일/비밀번호)")
  class SignupTests {

    @Test
    @DisplayName("[성공] 정상적인 회원가입")
    void signup_validInput_success() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String userId = "user-id-12345";
      String hashedPassword = "$2a$12$hashedPassword";

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn(userId);
      when(passwordEncoder.encrypt(password)).thenReturn(hashedPassword);
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signup(email, password);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(userId);
      assertThat(result.getEmail()).isEqualTo(email);
      assertThat(result.getPassword()).isEqualTo(hashedPassword);
      assertThat(result.getProvider()).isEqualTo(Provider.SYSTEM);
      assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(result.getUserRole()).isEqualTo(Role.USER);

      // Validator는 DTO에서 Bean Validation으로 처리됨
      verify(authRepository, atLeast(1)).findByEmail(email);
      verify(passwordEncoder).encrypt(password);
      verify(authRepository).save(any(Auth.class));
    }

    @Test
    @DisplayName("[실패] 이미 존재하는 이메일")
    void signup_existingEmail_throwsException() {
      // given
      String email = "existing@example.com";
      String password = "Password123!";

      Auth existingUser = Auth.builder().id("existing-user-id").email(email).build();

      when(authRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

      // when & then
      assertThatThrownBy(() -> signupStoreService.signup(email, password))
          .isInstanceOf(CustomException.class);

      verify(authRepository, atLeast(1)).findByEmail(email);
      verify(authRepository, never()).save(any(Auth.class));
    }

    // Validator 테스트는 BeanValidationTest.java에서 처리됨

    @Test
    @DisplayName("[보안] 비밀번호가 평문으로 저장되지 않음")
    void signup_passwordIsHashed() {
      // given
      String email = "test@example.com";
      String password = "PlainPassword123!";
      String hashedPassword = "$2a$12$hashedPassword";

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn("user-id");
      when(passwordEncoder.encrypt(password)).thenReturn(hashedPassword);
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signup(email, password);

      // then
      assertThat(result.getPassword()).isNotEqualTo(password);
      assertThat(result.getPassword()).isEqualTo(hashedPassword);
      verify(passwordEncoder).encrypt(password);
    }
  }

  @Nested
  @DisplayName("소셜 로그인 회원가입 테스트")
  class SignupFromOtherProviderTests {

    @Test
    @DisplayName("[성공] 카카오 소셜 로그인 회원가입")
    void signupFromOtherProvider_kakao_success() {
      // given
      String email = "kakao@example.com";
      Provider provider = Provider.KAKAO;
      String userId = "kakao-user-id";

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn(userId);
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signupFromOtherProvider(email, provider);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(userId);
      assertThat(result.getEmail()).isEqualTo(email);
      assertThat(result.getPassword()).isNull(); // 소셜 로그인은 비밀번호 없음
      assertThat(result.getProvider()).isEqualTo(Provider.KAKAO);
      assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(result.getUserRole()).isEqualTo(Role.USER);

      verify(authRepository, atLeast(1)).findByEmail(email);
      verify(keyProvider).generateKey();
      verify(authRepository).save(any(Auth.class));
    }

    @Test
    @DisplayName("[성공] 애플 소셜 로그인 회원가입")
    void signupFromOtherProvider_apple_success() {
      // given
      String email = "apple@example.com";
      Provider provider = Provider.APPLE;
      String userId = "apple-user-id";

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn(userId);
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signupFromOtherProvider(email, provider);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getProvider()).isEqualTo(Provider.APPLE);
      assertThat(result.getPassword()).isNull();
    }

    @Test
    @DisplayName("[실패] 이미 존재하는 이메일 (소셜 로그인)")
    void signupFromOtherProvider_existingEmail_throwsException() {
      // given
      String email = "existing@example.com";
      Provider provider = Provider.KAKAO;

      Auth existingUser =
          Auth.builder().id("existing-user-id").email(email).provider(Provider.SYSTEM).build();

      when(authRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

      // when & then
      assertThatThrownBy(() -> signupStoreService.signupFromOtherProvider(email, provider))
          .isInstanceOf(CustomException.class);

      verify(authRepository).findByEmail(email);
      verify(authRepository, never()).save(any(Auth.class));
    }

    @Test
    @DisplayName("[검증] 소셜 로그인은 비밀번호 검증 안 함")
    void signupFromOtherProvider_noPasswordValidation() {
      // given
      String email = "social@example.com";
      Provider provider = Provider.KAKAO;

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn("user-id");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      signupStoreService.signupFromOtherProvider(email, provider);

      // then (소셜 로그인은 비밀번호 불필요)
      verify(passwordEncoder, never()).encrypt(anyString());
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTests {

    @Test
    @DisplayName("[통합] 정상적인 회원가입 전체 흐름")
    void scenario_normalSignupFlow() {
      // given
      String email = "newuser@example.com";
      String password = "SecurePassword123!";

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn("new-user-id");
      when(passwordEncoder.encrypt(password)).thenReturn("$2a$12$hashed");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth auth = signupStoreService.signup(email, password);

      // then - 전체 필드 검증
      assertThat(auth.getId()).isNotBlank();
      assertThat(auth.getEmail()).isEqualTo(email);
      assertThat(auth.getPassword()).startsWith("$2a$12$");
      assertThat(auth.getProvider()).isEqualTo(Provider.SYSTEM);
      assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(auth.getUserRole()).isEqualTo(Role.USER);
      assertThat(auth.getCreatedAt()).isNotNull();

      // 실행 순서 확인 (Validator는 DTO Bean Validation으로 처리됨)
      var inOrder = inOrder(authRepository, passwordEncoder);
      inOrder.verify(authRepository, atLeast(1)).findByEmail(email);
      inOrder.verify(passwordEncoder).encrypt(password);
      inOrder.verify(authRepository).save(any(Auth.class));
    }

    @Test
    @DisplayName("[통합] 동일 이메일로 일반/소셜 중복 가입 방지")
    void scenario_preventDuplicateSignup() {
      // given - 일반 가입된 사용자
      String email = "user@example.com";
      Auth existingAuth =
          Auth.builder().id("existing-id").email(email).provider(Provider.SYSTEM).build();

      when(authRepository.findByEmail(email)).thenReturn(Optional.of(existingAuth));

      // when & then - 동일 이메일로 일반 가입 시도
      assertThatThrownBy(() -> signupStoreService.signup(email, "pass"))
          .isInstanceOf(CustomException.class);

      // when & then - 동일 이메일로 소셜 가입 시도
      assertThatThrownBy(() -> signupStoreService.signupFromOtherProvider(email, Provider.KAKAO))
          .isInstanceOf(CustomException.class);
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryTests {

    @Test
    @DisplayName("[경계] 매우 긴 이메일")
    void boundary_veryLongEmail() {
      // given
      String longEmail = "a".repeat(100) + "@example.com";
      String password = "Password123!";

      when(authRepository.findByEmail(longEmail)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn("user-id");
      when(passwordEncoder.encrypt(password)).thenReturn("hashed");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signup(longEmail, password);

      // then
      assertThat(result.getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("[경계] 매우 긴 비밀번호")
    void boundary_veryLongPassword() {
      // given
      String email = "test@example.com";
      String longPassword = "A1!a" + "a".repeat(200);

      when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(keyProvider.generateKey()).thenReturn("user-id");
      when(passwordEncoder.encrypt(longPassword)).thenReturn("hashed-long-password");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      Auth result = signupStoreService.signup(email, longPassword);

      // then
      assertThat(result).isNotNull();
      verify(passwordEncoder).encrypt(longPassword);
    }
  }
}
