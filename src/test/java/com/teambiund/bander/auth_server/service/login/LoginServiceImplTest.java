package com.teambiund.bander.auth_server.service.login;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.auth.service.login.LoginServiceImpl;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.auth.util.generator.token.TokenUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServiceImpl 테스트")
class LoginServiceImplTest {

  @Mock private LoginStatusRepository loginStatusRepository;

  @Mock private AuthRepository authRepository;

  @Mock private KeyProvider keyProvider;

  @Mock private CipherStrategy passwordEncoder;

  @Mock private TokenUtil tokenUtil;

  @Mock private CipherStrategy emailCipher;

  private LoginServiceImpl loginService;

  @BeforeEach
  void setUp() {
    loginService =
        new LoginServiceImpl(
            loginStatusRepository,
            authRepository,
            keyProvider,
            passwordEncoder,
            tokenUtil,
            emailCipher);
  }

  @Nested
  @DisplayName("로그인 테스트")
  class LoginTests {

    @Test
    @DisplayName("[성공] 정상적인 로그인")
    void login_validCredentials_success() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";
      String hashedPassword = "$2a$12$hashedPassword";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password(hashedPassword)
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .provider(Provider.SYSTEM)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      LoginResponse response = loginService.login(email, password);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo("access-token");
      assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
      assertThat(response.getDeviceId()).isNotNull();

      verify(emailCipher).encrypt(email);
      verify(authRepository).findByEmailWithLoginStatus(encryptedEmail);
      verify(passwordEncoder).matches(password, hashedPassword);
      verify(tokenUtil).generateAccessToken(eq("user-id-123"), eq(Role.USER), anyString());
      verify(tokenUtil).generateRefreshToken(eq("user-id-123"), eq(Role.USER), anyString());
      verify(authRepository).save(auth);
    }

    @Test
    @DisplayName("[성공] 암호화된 이메일로 먼저 조회, 없으면 평문 조회 (하위 호환성)")
    void login_backwardCompatibility_success() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";
      String hashedPassword = "$2a$12$hashedPassword";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password(hashedPassword)
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.empty());
      when(authRepository.findByEmailWithLoginStatus(email)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      LoginResponse response = loginService.login(email, password);

      // then
      assertThat(response).isNotNull();
      verify(authRepository).findByEmailWithLoginStatus(encryptedEmail);
      verify(authRepository).findByEmailWithLoginStatus(email);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 사용자")
    void login_userNotFound_throwsException() {
      // given
      String email = "nonexistent@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.empty());
      when(authRepository.findByEmailWithLoginStatus(email)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

      verify(passwordEncoder, never()).matches(anyString(), anyString());
      verify(tokenUtil, never()).generateAccessToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("[실패] 비밀번호 불일치")
    void login_passwordMismatch_throwsException() {
      // given
      String email = "test@example.com";
      String password = "WrongPassword";
      String encryptedEmail = "encrypted-email";
      String hashedPassword = "$2a$12$hashedPassword";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password(hashedPassword)
              .status(Status.ACTIVE)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.PASSWORD_MISMATCH);

      verify(tokenUtil, never()).generateAccessToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("[검증] LoginStatus가 올바르게 설정됨")
    void login_loginStatusSetCorrectly() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      loginService.login(email, password);

      // then
      ArgumentCaptor<Auth> authCaptor = ArgumentCaptor.forClass(Auth.class);
      verify(authRepository).save(authCaptor.capture());
      Auth savedAuth = authCaptor.getValue();
      assertThat(savedAuth.getLoginStatus()).isNotNull();
      assertThat(savedAuth.getLoginStatus().getLastLogin()).isNotNull();
    }
  }

  @Nested
  @DisplayName("사용자 상태 검증 테스트")
  class UserStatusTests {

    @Test
    @DisplayName("[실패] 휴면 상태 사용자 로그인 차단")
    void login_sleepingUser_throwsException() {
      // given
      String email = "sleeping@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.SLEEPING)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_IS_SLEEPING);

      verify(tokenUtil, never()).generateAccessToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("[실패] 차단된 사용자 로그인 차단")
    void login_blockedUser_throwsException() {
      // given
      String email = "blocked@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.BLOCKED)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("[실패] 정지된 사용자 로그인 차단")
    void login_suspendedUser_throwsException() {
      // given
      String email = "suspended@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.SUSPENDED)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("[실패] 탈퇴한 사용자 로그인 차단")
    void login_deletedUser_throwsException() {
      // given
      String email = "deleted@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.DELETED)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> loginService.login(email, password))
          .isInstanceOf(CustomException.class);
    }
  }

  @Nested
  @DisplayName("리프레시 토큰 테스트")
  class RefreshTokenTests {

    @Test
    @DisplayName("[성공] 유효한 리프레시 토큰으로 새 토큰 발급")
    void refreshToken_validToken_success() {
      // given
      String refreshToken = "valid-refresh-token";
      String deviceId = "device-123";
      String userId = "user-id-123";

      Auth auth =
          Auth.builder()
              .id(userId)
              .email("test@example.com")
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(tokenUtil.isValid(refreshToken)).thenReturn(true);
      when(tokenUtil.extractUserId(refreshToken)).thenReturn(userId);
      when(tokenUtil.extractDeviceId(refreshToken)).thenReturn(deviceId);
      when(authRepository.findByIdWithLoginStatus(userId)).thenReturn(Optional.of(auth));
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      LoginResponse response = loginService.refreshToken(refreshToken, deviceId);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo("new-access-token");
      assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
      assertThat(response.getDeviceId()).isNotNull();

      verify(tokenUtil).isValid(refreshToken);
      verify(tokenUtil).extractUserId(refreshToken);
      verify(tokenUtil).extractDeviceId(refreshToken);
      verify(authRepository).findByIdWithLoginStatus(userId);
    }

    @Test
    @DisplayName("[실패] 만료된 리프레시 토큰")
    void refreshToken_expiredToken_throwsException() {
      // given
      String refreshToken = "expired-refresh-token";
      String deviceId = "device-123";

      when(tokenUtil.isValid(refreshToken)).thenReturn(false);

      // when & then
      assertThatThrownBy(() -> loginService.refreshToken(refreshToken, deviceId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.EXPIRED_TOKEN);

      verify(tokenUtil, never()).extractUserId(anyString());
      verify(authRepository, never()).findByIdWithLoginStatus(anyString());
    }

    @Test
    @DisplayName("[실패] userId가 null인 토큰")
    void refreshToken_nullUserId_throwsException() {
      // given
      String refreshToken = "invalid-token";
      String deviceId = "device-123";

      when(tokenUtil.isValid(refreshToken)).thenReturn(true);
      when(tokenUtil.extractUserId(refreshToken)).thenReturn(null);
      when(tokenUtil.extractDeviceId(refreshToken)).thenReturn(deviceId);

      // when & then
      assertThatThrownBy(() -> loginService.refreshToken(refreshToken, deviceId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.INVALID_TOKEN);

      verify(authRepository, never()).findByIdWithLoginStatus(anyString());
    }

    @Test
    @DisplayName("[실패] deviceId 불일치")
    void refreshToken_deviceIdMismatch_throwsException() {
      // given
      String refreshToken = "valid-refresh-token";
      String deviceId = "device-123";
      String tokenDeviceId = "different-device";

      when(tokenUtil.isValid(refreshToken)).thenReturn(true);
      when(tokenUtil.extractUserId(refreshToken)).thenReturn("user-id");
      when(tokenUtil.extractDeviceId(refreshToken)).thenReturn(tokenDeviceId);

      // when & then
      assertThatThrownBy(() -> loginService.refreshToken(refreshToken, deviceId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.INVALID_DEVICE_ID);

      verify(authRepository, never()).findByIdWithLoginStatus(anyString());
    }

    @Test
    @DisplayName("[실패] 사용자를 찾을 수 없음")
    void refreshToken_userNotFound_throwsException() {
      // given
      String refreshToken = "valid-refresh-token";
      String deviceId = "device-123";
      String userId = "non-existent-user";

      when(tokenUtil.isValid(refreshToken)).thenReturn(true);
      when(tokenUtil.extractUserId(refreshToken)).thenReturn(userId);
      when(tokenUtil.extractDeviceId(refreshToken)).thenReturn(deviceId);
      when(authRepository.findByIdWithLoginStatus(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> loginService.refreshToken(refreshToken, deviceId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

      verify(tokenUtil, never()).generateAccessToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("[검증] 리프레시 시에도 LoginStatus 업데이트")
    void refreshToken_updatesLoginStatus() {
      // given
      String refreshToken = "valid-refresh-token";
      String deviceId = "device-123";
      String userId = "user-id-123";

      Auth auth =
          Auth.builder()
              .id(userId)
              .email("test@example.com")
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(tokenUtil.isValid(refreshToken)).thenReturn(true);
      when(tokenUtil.extractUserId(refreshToken)).thenReturn(userId);
      when(tokenUtil.extractDeviceId(refreshToken)).thenReturn(deviceId);
      when(authRepository.findByIdWithLoginStatus(userId)).thenReturn(Optional.of(auth));
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-refresh-token");
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      loginService.refreshToken(refreshToken, deviceId);

      // then
      ArgumentCaptor<Auth> authCaptor = ArgumentCaptor.forClass(Auth.class);
      verify(authRepository).save(authCaptor.capture());
      Auth savedAuth = authCaptor.getValue();
      assertThat(savedAuth.getLoginStatus()).isNotNull();
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTests {

    @Test
    @DisplayName("[통합] 로그인 후 리프레시 토큰으로 재발급")
    void scenario_loginThenRefresh() {
      // given - 로그인
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password("hashedPassword")
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when - 로그인
      LoginResponse loginResponse = loginService.login(email, password);

      // then
      assertThat(loginResponse.getAccessToken()).isEqualTo("access-token");
      assertThat(loginResponse.getRefreshToken()).isEqualTo("refresh-token");
      String deviceId = loginResponse.getDeviceId();

      // given - 리프레시
      when(tokenUtil.isValid("refresh-token")).thenReturn(true);
      when(tokenUtil.extractUserId("refresh-token")).thenReturn("user-id-123");
      when(tokenUtil.extractDeviceId("refresh-token")).thenReturn(deviceId);
      when(authRepository.findByIdWithLoginStatus("user-id-123")).thenReturn(Optional.of(auth));
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("new-refresh-token");

      // when - 리프레시
      LoginResponse refreshResponse = loginService.refreshToken("refresh-token", deviceId);

      // then
      assertThat(refreshResponse.getAccessToken()).isEqualTo("new-access-token");
      assertThat(refreshResponse.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("[통합] ADMIN 사용자 로그인")
    void scenario_adminLogin() {
      // given
      String email = "admin@example.com";
      String password = "AdminPassword123!";
      String encryptedEmail = "encrypted-admin-email";

      Auth adminAuth =
          Auth.builder()
              .id("admin-id")
              .email(email)
              .password("hashedPassword")
              .status(Status.ACTIVE)
              .userRole(Role.ADMIN)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(adminAuth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
      when(tokenUtil.generateAccessToken(eq("admin-id"), eq(Role.ADMIN), anyString()))
          .thenReturn("admin-access-token");
      when(tokenUtil.generateRefreshToken(eq("admin-id"), eq(Role.ADMIN), anyString()))
          .thenReturn("admin-refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(adminAuth);

      // when
      LoginResponse response = loginService.login(email, password);

      // then
      assertThat(response).isNotNull();
      verify(tokenUtil).generateAccessToken(eq("admin-id"), eq(Role.ADMIN), anyString());
      verify(tokenUtil).generateRefreshToken(eq("admin-id"), eq(Role.ADMIN), anyString());
    }
  }

  @Nested
  @DisplayName("보안 테스트")
  class SecurityTests {

    @Test
    @DisplayName("[보안] 비밀번호는 평문과 비교하지 않음")
    void security_passwordNotComparedInPlaintext() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";
      String hashedPassword = "$2a$12$hashedPassword";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .password(hashedPassword)
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      loginService.login(email, password);

      // then - passwordEncoder.matches가 호출되었는지 확인 (평문 비교가 아님)
      verify(passwordEncoder).matches(password, hashedPassword);
    }

    @Test
    @DisplayName("[보안] 이메일은 암호화되어 조회됨")
    void security_emailEncryptedBeforeLookup() {
      // given
      String email = "test@example.com";
      String password = "Password123!";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .password("hashedPassword")
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithLoginStatus(encryptedEmail)).thenReturn(Optional.of(auth));
      when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
      when(tokenUtil.generateAccessToken(anyString(), any(Role.class), anyString()))
          .thenReturn("access-token");
      when(tokenUtil.generateRefreshToken(anyString(), any(Role.class), anyString()))
          .thenReturn("refresh-token");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      loginService.login(email, password);

      // then
      verify(emailCipher).encrypt(email);
      verify(authRepository).findByEmailWithLoginStatus(encryptedEmail);
    }
  }
}
