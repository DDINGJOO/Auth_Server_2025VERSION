package com.teambiund.bander.auth_server.service.withdrawal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.Withdraw;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.withdrawal.impl.WithdrawalManagementServiceImpl;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalManagementServiceImpl 테스트")
class WithdrawalManagementServiceImplTest {

  @Mock private AuthRepository authRepository;

  @Mock private CipherStrategy emailCipher;

  private WithdrawalManagementServiceImpl withdrawalService;

  @BeforeEach
  void setUp() {
    withdrawalService = new WithdrawalManagementServiceImpl(authRepository, emailCipher);
  }

  @Nested
  @DisplayName("회원 탈퇴 테스트")
  class WithdrawTests {

    @Test
    @DisplayName("[성공] 정상적인 회원 탈퇴")
    void withdraw_validInput_success() {
      // given
      String userId = "user-id-123";
      String withdrawReason = "서비스가 마음에 들지 않음";

      Auth auth = Auth.builder().id(userId).email("test@example.com").status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw(userId, withdrawReason);

      // then
      assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
      assertThat(auth.getWithdraw()).isNotNull();
      assertThat(auth.getWithdraw().getWithdrawReason()).isEqualTo(withdrawReason);
      assertThat(auth.getWithdraw().getUser()).isEqualTo(auth);

      verify(authRepository).findById(userId);
      verify(authRepository).save(auth);
    }

    @Test
    @DisplayName("[검증] Withdraw 엔티티가 양방향 연관관계로 설정됨")
    void withdraw_bidirectionalRelationship() {
      // given
      String userId = "user-id-123";
      String withdrawReason = "개인 사유";

      Auth auth = Auth.builder().id(userId).email("test@example.com").status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw(userId, withdrawReason);

      // then
      Withdraw withdraw = auth.getWithdraw();
      assertThat(withdraw).isNotNull();
      assertThat(withdraw.getUser()).isEqualTo(auth);
      assertThat(withdraw.getWithdrawReason()).isEqualTo(withdrawReason);
      assertThat(withdraw.getWithdrawAt()).isNotNull();
    }

    @Test
    @DisplayName("[검증] 편의 메서드 markAsDeleted 사용")
    void withdraw_usesMarkAsDeletedConvenienceMethod() {
      // given
      String userId = "user-id-123";
      String withdrawReason = "테스트 사유";

      Auth auth = Auth.builder().id(userId).email("test@example.com").status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw(userId, withdrawReason);

      // then - markAsDeleted가 호출되어 상태와 withdraw가 설정됨
      assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
      assertThat(auth.getWithdraw()).isNotNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 사용자")
    void withdraw_userNotFound_throwsException() {
      // given
      String userId = "non-existent-user";
      String withdrawReason = "사유";

      when(authRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> withdrawalService.withdraw(userId, withdrawReason))
          .isInstanceOf(CustomException.class);

      verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("[경계] 매우 긴 탈퇴 사유")
    void withdraw_veryLongReason() {
      // given
      String userId = "user-id-123";
      String longReason = "탈퇴 사유: " + "A".repeat(500);

      Auth auth = Auth.builder().id(userId).email("test@example.com").status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw(userId, longReason);

      // then
      assertThat(auth.getWithdraw().getWithdrawReason()).isEqualTo(longReason);
    }

    @Test
    @DisplayName("[경계] null 탈퇴 사유")
    void withdraw_nullReason() {
      // given
      String userId = "user-id-123";
      String withdrawReason = null;

      Auth auth = Auth.builder().id(userId).email("test@example.com").status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw(userId, withdrawReason);

      // then
      assertThat(auth.getWithdraw().getWithdrawReason()).isNull();
    }
  }

  @Nested
  @DisplayName("회원 탈퇴 철회 테스트")
  class WithdrawRetractionTests {

    @Test
    @DisplayName("[성공] 정상적인 탈퇴 철회")
    void withdrawRetraction_validInput_success() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Withdraw withdraw = Withdraw.builder().id("withdraw-id").withdrawReason("탈퇴 사유").build();

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .status(Status.DELETED)
              .withdraw(withdraw)
              .build();
      withdraw.setUser(auth);

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdrawRetraction(email);

      // then
      assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(auth.getWithdraw()).isNull();

      verify(emailCipher).encrypt(email);
      verify(authRepository).findByEmailWithWithdraw(encryptedEmail);
      verify(authRepository).save(auth);
    }

    @Test
    @DisplayName("[성공] 암호화된 이메일로 먼저 조회, 없으면 평문 조회 (하위 호환성)")
    void withdrawRetraction_backwardCompatibility_success() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Withdraw withdraw = Withdraw.builder().id("withdraw-id").withdrawReason("탈퇴 사유").build();

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(email)
              .status(Status.DELETED)
              .withdraw(withdraw)
              .build();
      withdraw.setUser(auth);

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.empty());
      when(authRepository.findByEmailWithWithdraw(email)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdrawRetraction(email);

      // then
      assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
      verify(authRepository).findByEmailWithWithdraw(encryptedEmail);
      verify(authRepository).findByEmailWithWithdraw(email);
    }

    @Test
    @DisplayName("[검증] 편의 메서드 cancelWithdrawal 사용")
    void withdrawRetraction_usesCancelWithdrawalConvenienceMethod() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Withdraw withdraw = Withdraw.builder().id("withdraw-id").withdrawReason("탈퇴 사유").build();

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .status(Status.DELETED)
              .withdraw(withdraw)
              .build();
      withdraw.setUser(auth);

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdrawRetraction(email);

      // then - cancelWithdrawal이 호출되어 상태가 ACTIVE로 변경되고 withdraw가 null이 됨
      assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(auth.getWithdraw()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 사용자")
    void withdrawRetraction_userNotFound_throwsException() {
      // given
      String email = "nonexistent@example.com";
      String encryptedEmail = "encrypted-nonexistent";

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.empty());
      when(authRepository.findByEmailWithWithdraw(email)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> withdrawalService.withdrawRetraction(email))
          .isInstanceOf(CustomException.class);

      verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("[실패] 탈퇴 정보가 없음")
    void withdrawRetraction_withdrawNotFound_throwsException() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .status(Status.ACTIVE)
              .withdraw(null) // 탈퇴 정보 없음
              .build();

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));

      // when & then
      assertThatThrownBy(() -> withdrawalService.withdrawRetraction(email))
          .isInstanceOf(CustomException.class);

      verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("[검증] orphanRemoval로 Withdraw 엔티티 자동 삭제")
    void withdrawRetraction_orphanRemovalDeletesWithdraw() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Withdraw withdraw = Withdraw.builder().id("withdraw-id").withdrawReason("탈퇴 사유").build();

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .status(Status.DELETED)
              .withdraw(withdraw)
              .build();
      withdraw.setUser(auth);

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdrawRetraction(email);

      // then - withdraw가 null이 되어 orphanRemoval로 삭제됨
      assertThat(auth.getWithdraw()).isNull();
      verify(authRepository).save(auth);
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTests {

    @Test
    @DisplayName("[통합] 탈퇴 후 철회")
    void scenario_withdrawThenRetraction() {
      // given - 탈퇴
      String userId = "user-id-123";
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";
      String withdrawReason = "서비스 불만족";

      Auth auth = Auth.builder().id(userId).email(encryptedEmail).status(Status.ACTIVE).build();

      when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when - 탈퇴
      withdrawalService.withdraw(userId, withdrawReason);

      // then
      assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
      assertThat(auth.getWithdraw()).isNotNull();

      // given - 철회
      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));

      // when - 철회
      withdrawalService.withdrawRetraction(email);

      // then
      assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
      assertThat(auth.getWithdraw()).isNull();
    }

    @Test
    @DisplayName("[통합] 다양한 상태의 사용자 탈퇴")
    void scenario_withdrawDifferentStatuses() {
      // given - ACTIVE 사용자
      Auth activeAuth =
          Auth.builder()
              .id("active-user")
              .email("active@example.com")
              .status(Status.ACTIVE)
              .build();

      when(authRepository.findById("active-user")).thenReturn(Optional.of(activeAuth));
      when(authRepository.save(any(Auth.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      withdrawalService.withdraw("active-user", "사유");

      // then
      assertThat(activeAuth.getStatus()).isEqualTo(Status.DELETED);

      // given - SLEEPING 사용자
      Auth sleepingAuth =
          Auth.builder()
              .id("sleeping-user")
              .email("sleeping@example.com")
              .status(Status.SLEEPING)
              .build();

      when(authRepository.findById("sleeping-user")).thenReturn(Optional.of(sleepingAuth));

      // when
      withdrawalService.withdraw("sleeping-user", "사유");

      // then
      assertThat(sleepingAuth.getStatus()).isEqualTo(Status.DELETED);
    }
  }

  @Nested
  @DisplayName("예외 상황 테스트")
  class ExceptionTests {

    @Test
    @DisplayName("[예외] null userId로 탈퇴")
    void withdraw_nullUserId_throwsException() {
      // when & then
      assertThatThrownBy(() -> withdrawalService.withdraw(null, "사유"))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("[예외] null email로 탈퇴 철회")
    void withdrawRetraction_nullEmail_throwsException() {
      // when & then
      assertThatThrownBy(() -> withdrawalService.withdrawRetraction(null))
          .isInstanceOf(Exception.class);
    }
  }

  @Nested
  @DisplayName("보안 테스트")
  class SecurityTests {

    @Test
    @DisplayName("[보안] 이메일은 암호화되어 조회됨")
    void withdrawRetraction_emailEncryptedBeforeLookup() {
      // given
      String email = "test@example.com";
      String encryptedEmail = "encrypted-email";

      Withdraw withdraw = Withdraw.builder().id("withdraw-id").withdrawReason("탈퇴 사유").build();

      Auth auth =
          Auth.builder()
              .id("user-id-123")
              .email(encryptedEmail)
              .status(Status.DELETED)
              .withdraw(withdraw)
              .build();
      withdraw.setUser(auth);

      when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
      when(authRepository.findByEmailWithWithdraw(encryptedEmail)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      withdrawalService.withdrawRetraction(email);

      // then
      verify(emailCipher).encrypt(email);
      verify(authRepository).findByEmailWithWithdraw(encryptedEmail);
    }
  }
}
