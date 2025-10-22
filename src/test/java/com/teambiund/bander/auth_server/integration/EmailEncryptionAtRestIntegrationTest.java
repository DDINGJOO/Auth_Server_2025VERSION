package com.teambiund.bander.auth_server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.teambiund.bander.auth_server.AuthServerApplication;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.login.LoginServiceImpl;
import com.teambiund.bander.auth_server.auth.service.signup.SignupStoreService;
import com.teambiund.bander.auth_server.auth.service.withdrawal.impl.WithdrawalManagementServiceImpl;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.auth.util.generator.token.TokenUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests to ensure plaintext email inputs are correctly matched against
 * encrypted-at-rest emails stored in the database.
 */
@SpringBootTest(classes = AuthServerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("[통합] 이메일 평문 입력 vs. 암호화 저장 매칭 테스트")
public class EmailEncryptionAtRestIntegrationTest {

  // AuthValidator가 제거되어 더 이상 필요 없음
  // 검증은 DTO의 Bean Validation으로 처리됨

  @Autowired private AuthRepository authRepository;

  @Autowired private SignupStoreService signupStoreService;

  @Autowired private LoginServiceImpl loginService;

  @Autowired private WithdrawalManagementServiceImpl withdrawalManagementService;

  @Autowired private CipherStrategy aesCipherStrategy; // bean name: aesCipherStrategy

  @Autowired private CipherStrategy pbkdf2CipherStrategy; // bean name: pbkdf2CipherStrategy

  @MockBean private KeyProvider keyProvider; // 생성되는 키는 고정값으로

  @MockBean private TokenUtil tokenUtil; // 토큰 생성은 단순 목으로 대체

  @Test
  @DisplayName("[가입 차단] DB에 암호화된 이메일이 이미 있을 때, 평문 이메일로 가입 시도 시 차단")
  void signup_shouldBlockWhenEncryptedEmailAlreadyExists() {
    // given: DB에는 암호화된 이메일로 저장된 기존 유저가 있음
    String plainEmail = "exists@example.com";
    String encryptedEmail = aesCipherStrategy.encrypt(plainEmail);
    Auth existing =
        Auth.builder()
            .id("existing-user-id")
            .email(encryptedEmail)
            .provider(Provider.SYSTEM)
            .status(Status.ACTIVE)
            .userRole(Role.USER)
            .createdAt(LocalDateTime.now())
            .password(pbkdf2CipherStrategy.encrypt("Passw0rd!"))
            .build();
    authRepository.save(existing);

    // and: 부수 의존성 목 설정
    when(keyProvider.generateKey()).thenReturn("new-user-id");

    // when & then: 평문 이메일로 가입 시도하면 중복으로 예외
    assertThatThrownBy(() -> signupStoreService.signup(plainEmail, "NewPass123!"))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(AuthErrorCode.EMAIL_ALREADY_EXISTS.name());
  }

  @Test
  @DisplayName("[탈퇴 철회] 평문 이메일 입력으로 암호화 저장 사용자를 찾아 탈퇴 철회")
  void withdrawRetraction_withPlaintextEmail_shouldFindEncryptedStoredUser() {
    // given: 탈퇴 처리된 사용자 (이메일은 암호화 저장)
    String plainEmail = "withdraw@example.com";
    String encryptedEmail = aesCipherStrategy.encrypt(plainEmail);
    Auth user =
        Auth.builder()
            .id("withdraw-user-id")
            .email(encryptedEmail)
            .provider(Provider.SYSTEM)
            .status(Status.ACTIVE)
            .userRole(Role.USER)
            .createdAt(LocalDateTime.now())
            .password(pbkdf2CipherStrategy.encrypt("Passw0rd!"))
            .build();
    // 탈퇴 표시 (Withdraw 엔티티 생성)
    user.markAsDeleted("just because");
    authRepository.save(user);

    // when: 평문 이메일로 철회 요청
    withdrawalManagementService.withdrawRetraction(plainEmail);

    // then: 철회 완료 확인
    Auth found = authRepository.findById(user.getId()).orElseThrow();
    assertThat(found.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(found.getWithdraw()).isNull();
  }

  @Test
  @DisplayName("[로그인] 평문 이메일로 암호화 저장된 올바른 사용자 찾기")
  void login_withPlaintextEmail_shouldFindEncryptedStoredUser() {
    // given: DB에 암호화된 이메일로 저장된 사용자
    String plainEmail = "login-test@example.com";
    String plainPassword = "Passw0rd!";
    String encryptedEmail = aesCipherStrategy.encrypt(plainEmail);
    String encryptedPassword = pbkdf2CipherStrategy.encrypt(plainPassword);

    Auth user =
        Auth.builder()
            .id("login-user-id")
            .email(encryptedEmail)
            .provider(Provider.SYSTEM)
            .status(Status.ACTIVE)
            .userRole(Role.USER)
            .createdAt(LocalDateTime.now())
            .password(encryptedPassword)
            .build();
    authRepository.save(user);

    // and: 토큰 생성 mock 설정
    when(tokenUtil.generateAccessToken(anyString(), any(), anyString()))
        .thenReturn("mock-access-token");
    when(tokenUtil.generateRefreshToken(anyString(), any(), anyString()))
        .thenReturn("mock-refresh-token");

    // when: 평문 이메일과 비밀번호로 로그인 시도
    LoginResponse response = loginService.login(plainEmail, plainPassword);

    // then: 로그인 성공
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
    assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
  }

  @Test
  @DisplayName("[로그인 성능] 10명의 사용자가 있을 때 로그인 지연 시간 측정")
  void login_performance_with10Users() {
    // given: 10명의 사용자 데이터 (암호화된 이메일로 저장)
    String targetPlainEmail = "perf10-user5@example.com";
    String plainPassword = "Passw0rd!";

    List<Auth> users = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      String email = "perf10-user" + i + "@example.com";
      String encryptedEmail = aesCipherStrategy.encrypt(email);
      Auth user =
          Auth.builder()
              .id("perf10-user-" + i)
              .email(encryptedEmail)
              .provider(Provider.SYSTEM)
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .createdAt(LocalDateTime.now())
              .password(pbkdf2CipherStrategy.encrypt(plainPassword))
              .build();
      users.add(user);
    }
    authRepository.saveAll(users);

    // and: 토큰 생성 mock 설정
    when(tokenUtil.generateAccessToken(anyString(), any(), anyString()))
        .thenReturn("mock-access-token");
    when(tokenUtil.generateRefreshToken(anyString(), any(), anyString()))
        .thenReturn("mock-refresh-token");

    // when: 로그인 수행 시간 측정
    long startTime = System.currentTimeMillis();
    LoginResponse response = loginService.login(targetPlainEmail, plainPassword);
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // then: 로그인 성공 및 시간 측정 결과 출력
    assertThat(response).isNotNull();
    System.out.println("[성능] 10명 중 로그인 소요 시간: " + duration + "ms");

    // 기준: 10명일 때 1초 이내 (매우 관대한 기준)
    assertThat(duration).isLessThan(1000L);
  }

  @Test
  @DisplayName("[로그인 성능] 100명의 사용자가 있을 때 로그인 지연 시간 측정")
  void login_performance_with100Users() {
    // given: 100명의 사용자 데이터 (암호화된 이메일로 저장)
    String targetPlainEmail = "perf100-user50@example.com";
    String plainPassword = "Passw0rd!";

    List<Auth> users = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      String email = "perf100-user" + i + "@example.com";
      String encryptedEmail = aesCipherStrategy.encrypt(email);
      Auth user =
          Auth.builder()
              .id("perf100-user-" + i)
              .email(encryptedEmail)
              .provider(Provider.SYSTEM)
              .status(Status.ACTIVE)
              .userRole(Role.USER)
              .createdAt(LocalDateTime.now())
              .password(pbkdf2CipherStrategy.encrypt(plainPassword))
              .build();
      users.add(user);
    }
    authRepository.saveAll(users);

    // and: 토큰 생성 mock 설정
    when(tokenUtil.generateAccessToken(anyString(), any(), anyString()))
        .thenReturn("mock-access-token");
    when(tokenUtil.generateRefreshToken(anyString(), any(), anyString()))
        .thenReturn("mock-refresh-token");

    // when: 로그인 수행 시간 측정
    long startTime = System.currentTimeMillis();
    LoginResponse response = loginService.login(targetPlainEmail, plainPassword);
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // then: 로그인 성공 및 시간 측정 결과 출력
    assertThat(response).isNotNull();
    System.out.println("[성능] 100명 중 로그인 소요 시간: " + duration + "ms");

    // 기준: 100명일 때 5초 이내 (선형 탐색 시 예상 지연)
    assertThat(duration).isLessThan(5000L);
  }

  //    @Test
  //    @DisplayName("[로그인 성능] 1000명의 사용자가 있을 때 로그인 지연 시간 측정")
  //    void login_performance_with1000Users() {
  //        // given: 1000명의 사용자 데이터 (암호화된 이메일로 저장)
  //        String targetPlainEmail = "perf1000-user500@example.com";
  //        String plainPassword = "Passw0rd!";
  //
  //        List<Auth> users = new ArrayList<>();
  //        for (int i = 0; i < 1000; i++) {
  //            String email = "perf1000-user" + i + "@example.com";
  //            String encryptedEmail = aesCipherStrategy.encrypt(email);
  //            Auth user = Auth.builder()
  //                    .id("perf1000-user-" + i)
  //                    .email(encryptedEmail)
  //                    .provider(Provider.SYSTEM)
  //                    .status(Status.ACTIVE)
  //                    .userRole(Role.USER)
  //                    .createdAt(LocalDateTime.now())
  //                    .password(pbkdf2CipherStrategy.encrypt(plainPassword))
  //                    .build();
  //            users.add(user);
  //        }
  //        authRepository.saveAll(users);
  //
  //        // and: 토큰 생성 mock 설정
  //        when(tokenUtil.generateAccessToken(anyString(), any(), anyString()))
  //                .thenReturn("mock-access-token");
  //        when(tokenUtil.generateRefreshToken(anyString(), any(), anyString()))
  //                .thenReturn("mock-refresh-token");
  //
  //        // when: 로그인 수행 시간 측정
  //        long startTime = System.currentTimeMillis();
  //        LoginResponse response = loginService.login(targetPlainEmail, plainPassword);
  //        long endTime = System.currentTimeMillis();
  //        long duration = endTime - startTime;
  //
  //        // then: 로그인 성공 및 시간 측정 결과 출력
  //        assertThat(response).isNotNull();
  //        System.out.println("[성능] 1000명 중 로그인 소요 시간: " + duration + "ms");
  //
  //        // 기준: 1000명일 때 30초 이내 (선형 탐색 시 심각한 지연 예상)
  //        assertThat(duration).isLessThan(30000L);
  //    }
  //
  //    @Test
  //    @DisplayName("[로그인 성능] 10000명의 사용자가 있을 때 로그인 지연 시간 측정")
  //    void login_performance_with10000Users() {
  //        // given: 10000명의 사용자 데이터 (암호화된 이메일로 저장)
  //        String targetPlainEmail = "perf10000-user5000@example.com";
  //        String plainPassword = "Passw0rd!";
  //
  //        System.out.println("[성능] 10000명 사용자 데이터 생성 시작...");
  //        long dataSetupStart = System.currentTimeMillis();
  //
  //        List<Auth> users = new ArrayList<>();
  //        for (int i = 0; i < 10000; i++) {
  //            String email = "perf10000-user" + i + "@example.com";
  //            String encryptedEmail = aesCipherStrategy.encrypt(email);
  //            Auth user = Auth.builder()
  //                    .id("perf10000-user-" + i)
  //                    .email(encryptedEmail)
  //                    .provider(Provider.SYSTEM)
  //                    .status(Status.ACTIVE)
  //                    .userRole(Role.USER)
  //                    .createdAt(LocalDateTime.now())
  //                    .password(pbkdf2CipherStrategy.encrypt(plainPassword))
  //                    .build();
  //            users.add(user);
  //
  //            // 배치로 저장 (메모리 관리)
  //            if (i % 500 == 0 && i > 0) {
  //                authRepository.saveAll(users);
  //                users.clear();
  //                System.out.println("[성능] " + i + "명 저장 완료");
  //            }
  //        }
  //        if (!users.isEmpty()) {
  //            authRepository.saveAll(users);
  //        }
  //
  //        long dataSetupEnd = System.currentTimeMillis();
  //        System.out.println("[성능] 10000명 데이터 생성 완료: " + (dataSetupEnd - dataSetupStart) + "ms");
  //
  //        // and: 토큰 생성 mock 설정
  //        when(tokenUtil.generateAccessToken(anyString(), any(), anyString()))
  //                .thenReturn("mock-access-token");
  //        when(tokenUtil.generateRefreshToken(anyString(), any(), anyString()))
  //                .thenReturn("mock-refresh-token");
  //
  //        // when: 로그인 수행 시간 측정
  //        long startTime = System.currentTimeMillis();
  //        LoginResponse response = loginService.login(targetPlainEmail, plainPassword);
  //        long endTime = System.currentTimeMillis();
  //        long duration = endTime - startTime;
  //
  //        // then: 로그인 성공 및 시간 측정 결과 출력
  //        assertThat(response).isNotNull();
  //        System.out.println("[성능] 10000명 중 로그인 소요 시간: " + duration + "ms");
  //
  //        // 기준: 10000명일 때 60초 이내 (선형 탐색 시 매우 심각한 지연 예상)
  //        // 이 테스트는 현재 구현의 성능 문제를 명확히 보여주기 위한 것
  //        assertThat(duration).isLessThan(60000L);
  //    }
}
