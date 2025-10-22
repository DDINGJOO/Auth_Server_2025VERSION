package com.teambiund.bander.auth_server.service.update;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.dto.request.HistoryRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.update.HistoryService;
import com.teambiund.bander.auth_server.auth.service.update.UpdateService;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
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
@DisplayName("UpdateService 테스트")
class UpdateServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private HistoryService historyService;

    @Mock
    private CipherStrategy passwordEncoder;

    @Mock
    private CipherStrategy emailCipher;

    private UpdateService updateService;

    @BeforeEach
    void setUp() {
        updateService = new UpdateService(
                authRepository,
                historyService,
                passwordEncoder,
                emailCipher
        );
    }

    @Nested
    @DisplayName("이메일 확인 테스트")
    class EmailConfirmTests {

        @Test
        @DisplayName("[성공] 이메일 확인 시 사용자 활성화")
        void emailConfirm_validUser_activatesUser() {
            // given
            String userId = "user-id-123";
            Auth auth = Auth.builder()
                    .id(userId)
                    .email("test@example.com")
                    .status(Status.UNCONFIRMED)
                    .userRole(Role.GUEST)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when
            updateService.EmailConfirm(userId);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(auth.getUserRole()).isEqualTo(Role.USER);

            verify(authRepository).findById(userId);
            verify(authRepository).save(auth);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자")
        void emailConfirm_userNotFound_throwsException() {
            // given
            String userId = "non-existent-user";

            when(authRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> updateService.EmailConfirm(userId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

            verify(authRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("이메일 변경 테스트")
    class UpdateEmailTests {

        @Test
        @DisplayName("[성공] 이메일 변경")
        void updateEmail_validInput_success() {
            // given
            String userId = "user-id-123";
            String newEmail = "newemail@example.com";
            String encryptedEmail = "encrypted-new-email";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("old@example.com")
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(emailCipher.encrypt(newEmail)).thenReturn(encryptedEmail);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.updateEmail(userId, newEmail);

            // then
            assertThat(auth.getEmail()).isEqualTo(encryptedEmail);
            assertThat(auth.getStatus()).isEqualTo(Status.UNCONFIRMED);

            verify(authRepository).findById(userId);
            verify(emailCipher).encrypt(newEmail);
            verify(authRepository).save(auth);
            verify(historyService).createHistory(any(HistoryRequest.class));
        }

        @Test
        @DisplayName("[검증] 이메일 변경 시 상태가 UNCONFIRMED로 변경")
        void updateEmail_changesStatusToUnconfirmed() {
            // given
            String userId = "user-id-123";
            String newEmail = "newemail@example.com";
            String encryptedEmail = "encrypted-email";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("old@example.com")
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(emailCipher.encrypt(newEmail)).thenReturn(encryptedEmail);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.updateEmail(userId, newEmail);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.UNCONFIRMED);
        }

        @Test
        @DisplayName("[검증] 이메일 변경 히스토리 생성")
        void updateEmail_createsHistory() {
            // given
            String userId = "user-id-123";
            String newEmail = "newemail@example.com";
            String encryptedEmail = "encrypted-new-email";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("old@example.com")
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(emailCipher.encrypt(newEmail)).thenReturn(encryptedEmail);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            ArgumentCaptor<HistoryRequest> historyCaptor = ArgumentCaptor.forClass(HistoryRequest.class);

            // when
            updateService.updateEmail(userId, newEmail);

            // then
            verify(historyService).createHistory(historyCaptor.capture());
            HistoryRequest capturedHistory = historyCaptor.getValue();
            assertThat(capturedHistory.getAuth()).isEqualTo(auth);
            assertThat(capturedHistory.getAfterValue()).isEqualTo(encryptedEmail);
            assertThat(capturedHistory.getUpdatedColumn()).isEqualTo("email");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자")
        void updateEmail_userNotFound_throwsException() {
            // given
            String userId = "non-existent-user";
            String newEmail = "newemail@example.com";

            when(authRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateService.updateEmail(userId, newEmail))
                    .isInstanceOf(CustomException.class);

            verify(emailCipher, never()).encrypt(anyString());
            verify(authRepository, never()).save(any());
            verify(historyService, never()).createHistory(any());
        }

        @Test
        @DisplayName("[보안] 이메일이 암호화되어 저장됨")
        void updateEmail_emailIsEncrypted() {
            // given
            String userId = "user-id-123";
            String newEmail = "plain@example.com";
            String encryptedEmail = "encrypted-plain-email";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("old@example.com")
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(emailCipher.encrypt(newEmail)).thenReturn(encryptedEmail);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.updateEmail(userId, newEmail);

            // then
            assertThat(auth.getEmail()).isNotEqualTo(newEmail);
            assertThat(auth.getEmail()).isEqualTo(encryptedEmail);
            verify(emailCipher).encrypt(newEmail);
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePasswordTests {

        @Test
        @DisplayName("[성공] 비밀번호 변경")
        void changePassword_validInput_success() {
            // given
            String email = "test@example.com";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";
            String hashedPassword = "$2a$12$newHashedPassword";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(encryptedEmail)
                    .password("oldHashedPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn(hashedPassword);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            assertThat(auth.getPassword()).isEqualTo(hashedPassword);

            verify(emailCipher).encrypt(email);
            verify(authRepository).findByEmailWithHistory(encryptedEmail);
            verify(passwordEncoder).encrypt(newPassword);
            verify(authRepository).save(auth);
            verify(historyService).createHistory(any(HistoryRequest.class));
        }

        @Test
        @DisplayName("[성공] 암호화된 이메일로 먼저 조회, 없으면 평문 조회 (하위 호환성)")
        void changePassword_backwardCompatibility_success() {
            // given
            String email = "test@example.com";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";
            String hashedPassword = "$2a$12$newHashedPassword";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(email)
                    .password("oldHashedPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.empty());
            when(authRepository.findByEmailWithHistory(email)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn(hashedPassword);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            assertThat(auth.getPassword()).isEqualTo(hashedPassword);
            verify(authRepository).findByEmailWithHistory(encryptedEmail);
            verify(authRepository).findByEmailWithHistory(email);
        }

        @Test
        @DisplayName("[검증] 비밀번호 변경 히스토리 생성")
        void changePassword_createsHistory() {
            // given
            String email = "test@example.com";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";
            String hashedPassword = "$2a$12$newHashedPassword";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(encryptedEmail)
                    .password("oldHashedPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn(hashedPassword);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            ArgumentCaptor<HistoryRequest> historyCaptor = ArgumentCaptor.forClass(HistoryRequest.class);

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            verify(historyService).createHistory(historyCaptor.capture());
            HistoryRequest capturedHistory = historyCaptor.getValue();
            assertThat(capturedHistory.getAuth()).isEqualTo(auth);
            assertThat(capturedHistory.getAfterValue()).isEqualTo(hashedPassword);
            assertThat(capturedHistory.getUpdatedColumn()).isEqualTo("password");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자")
        void changePassword_userNotFound_throwsException() {
            // given
            String email = "nonexistent@example.com";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.empty());
            when(authRepository.findByEmailWithHistory(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateService.changePassword(email, newPassword, passConfirm))
                    .isInstanceOf(CustomException.class);

            verify(passwordEncoder, never()).encrypt(anyString());
            verify(authRepository, never()).save(any());
            verify(historyService, never()).createHistory(any());
        }

        @Test
        @DisplayName("[보안] 비밀번호가 평문으로 저장되지 않음")
        void changePassword_passwordIsHashed() {
            // given
            String email = "test@example.com";
            String newPassword = "PlainPassword123!";
            String passConfirm = "PlainPassword123!";
            String encryptedEmail = "encrypted-email";
            String hashedPassword = "$2a$12$hashedPassword";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(encryptedEmail)
                    .password("oldPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn(hashedPassword);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            assertThat(auth.getPassword()).isNotEqualTo(newPassword);
            assertThat(auth.getPassword()).isEqualTo(hashedPassword);
            verify(passwordEncoder).encrypt(newPassword);
        }

        @Test
        @DisplayName("[검증] Fetch Join으로 히스토리와 함께 조회")
        void changePassword_usesFetchJoin() {
            // given
            String email = "test@example.com";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(encryptedEmail)
                    .password("oldPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn("hashedPassword");
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            verify(authRepository).findByEmailWithHistory(encryptedEmail);
            verify(authRepository, never()).findByEmail(anyString());  // 일반 조회 안 함
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("[통합] 이메일 변경 후 확인")
        void scenario_changeEmailThenConfirm() {
            // given - 이메일 변경
            String userId = "user-id-123";
            String newEmail = "newemail@example.com";
            String encryptedEmail = "encrypted-new-email";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("old@example.com")
                    .status(Status.ACTIVE)
                    .userRole(Role.USER)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(emailCipher.encrypt(newEmail)).thenReturn(encryptedEmail);
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when - 이메일 변경
            updateService.updateEmail(userId, newEmail);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.UNCONFIRMED);

            // when - 이메일 확인
            updateService.EmailConfirm(userId);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(auth.getUserRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("[통합] 비밀번호 변경 전체 흐름")
        void scenario_fullPasswordChangeFlow() {
            // given
            String email = "test@example.com";
            String oldPassword = "OldPassword123!";
            String newPassword = "NewPassword123!";
            String passConfirm = "NewPassword123!";
            String encryptedEmail = "encrypted-email";

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email(encryptedEmail)
                    .password("$2a$12$oldHashedPassword")
                    .build();

            when(emailCipher.encrypt(email)).thenReturn(encryptedEmail);
            when(authRepository.findByEmailWithHistory(encryptedEmail)).thenReturn(Optional.of(auth));
            when(passwordEncoder.encrypt(newPassword)).thenReturn("$2a$12$newHashedPassword");
            when(authRepository.save(any(Auth.class))).thenReturn(auth);
            doNothing().when(historyService).createHistory(any(HistoryRequest.class));

            // when
            updateService.changePassword(email, newPassword, passConfirm);

            // then
            assertThat(auth.getPassword()).isEqualTo("$2a$12$newHashedPassword");
            verify(authRepository).save(auth);
            verify(historyService).createHistory(any(HistoryRequest.class));
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("[예외] null userId로 이메일 확인")
        void emailConfirm_nullUserId_throwsException() {
            // when & then
            assertThatThrownBy(() -> updateService.EmailConfirm(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("[예외] null 파라미터로 이메일 변경")
        void updateEmail_nullParameters_throwsException() {
            // when & then
            assertThatThrownBy(() -> updateService.updateEmail(null, "email@test.com"))
                    .isInstanceOf(Exception.class);

            assertThatThrownBy(() -> updateService.updateEmail("user-id", null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("[예외] null 파라미터로 비밀번호 변경")
        void changePassword_nullParameters_throwsException() {
            // when & then
            assertThatThrownBy(() -> updateService.changePassword(null, "pass", "pass"))
                    .isInstanceOf(Exception.class);

            assertThatThrownBy(() -> updateService.changePassword("email@test.com", null, "pass"))
                    .isInstanceOf(Exception.class);
        }
    }
}
