package com.teambiund.bander.auth_server.service.suspension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.Suspend;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.repository.SuspendRepository;
import com.teambiund.bander.auth_server.auth.service.suspension.impl.SuspensionManagementServiceImpl;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuspensionManagementServiceImpl 테스트")
class SuspensionManagementServiceImplTest {

    @Mock
    private SuspendRepository suspendRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private KeyProvider keyProvider;

    @InjectMocks
    private SuspensionManagementServiceImpl suspensionService;

    @Nested
    @DisplayName("정지 해제 테스트")
    class ReleaseTests {

        @Test
        @DisplayName("[성공] 정지된 사용자 해제")
        void release_suspendedUser_success() {
            // given
            String userId = "user-id-123";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("test@example.com")
                    .status(Status.SUSPENDED)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.release(userId);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);

            verify(authRepository).findById(userId);
            verify(authRepository).save(auth);
        }

        @Test
        @DisplayName("[성공] BLOCKED 상태 사용자도 해제 가능")
        void release_blockedUser_success() {
            // given
            String userId = "user-id-123";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("test@example.com")
                    .status(Status.BLOCKED)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.release(userId);

            // then
            assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자")
        void release_userNotFound_throwsException() {
            // given
            String userId = "non-existent-user";

            when(authRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> suspensionService.release(userId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 이미 활성 상태인 사용자")
        void release_alreadyActiveUser_throwsException() {
            // given
            String userId = "user-id-123";

            Auth auth = Auth.builder()
                    .id(userId)
                    .email("test@example.com")
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(auth));

      // when & then
      assertThatThrownBy(() -> suspensionService.release(userId))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_SUSPENDED);

            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[검증] 다양한 상태에서 해제 시도")
        void release_variousStatuses() {
            // given - SLEEPING 상태
            Auth sleepingAuth = Auth.builder()
                    .id("sleeping-user")
                    .status(Status.SLEEPING)
                    .build();

            when(authRepository.findById("sleeping-user")).thenReturn(Optional.of(sleepingAuth));
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.release("sleeping-user");

            // then
            assertThat(sleepingAuth.getStatus()).isEqualTo(Status.ACTIVE);
        }
    }

    @Nested
    @DisplayName("사용자 정지 테스트")
    class SuspendTests {

        @Test
        @DisplayName("[성공] 정상적인 사용자 정지")
        void suspend_validInput_success() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";
            String suspendReason = "부적절한 행위";
            Long suspendDate = 7L;

            Auth user = Auth.builder()
                    .id(userId)
                    .email("user@example.com")
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .email("admin@example.com")
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id-123");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, suspendReason, suspenderUserId, suspendDate);

            // then
            assertThat(user.getStatus()).isEqualTo(Status.BLOCKED);
            assertThat(user.getSuspensions()).hasSize(1);

            Suspend suspend = user.getSuspensions().get(0);
            assertThat(suspend.getId()).isEqualTo("suspend-id-123");
            assertThat(suspend.getReason()).isEqualTo(suspendReason);
            assertThat(suspend.getSuspenderUserId()).isEqualTo(suspenderUserId);
            assertThat(suspend.getSuspendUntil()).isEqualTo(LocalDate.now().plusDays(suspendDate));
            assertThat(suspend.getSuspendedUser()).isEqualTo(user);

            verify(authRepository).findById(userId);
            verify(authRepository).findById(suspenderUserId);
            verify(keyProvider).generateKey();
            verify(authRepository).save(user);
        }

        @Test
        @DisplayName("[검증] 양방향 연관관계 설정")
        void suspend_bidirectionalRelationship() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, "사유", suspenderUserId, 7L);

            // then
            Suspend suspend = user.getSuspensions().get(0);
            assertThat(suspend.getSuspendedUser()).isEqualTo(user);
            assertThat(user.getSuspensions()).contains(suspend);
        }

        @Test
        @DisplayName("[검증] 편의 메서드 addSuspension 사용")
        void suspend_usesAddSuspensionConvenienceMethod() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, "사유", suspenderUserId, 7L);

            // then - addSuspension이 호출되어 양방향 연관관계가 설정됨
            assertThat(user.getSuspensions()).hasSize(1);
            assertThat(user.getSuspensions().get(0).getSuspendedUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("[검증] 정지 기간 계산")
        void suspend_calculatesSuspendUntilCorrectly() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";
            Long suspendDays = 30L;

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, "사유", suspenderUserId, suspendDays);

            // then
            LocalDate expectedSuspendUntil = LocalDate.now().plusDays(suspendDays);
            assertThat(user.getSuspensions().get(0).getSuspendUntil()).isEqualTo(expectedSuspendUntil);
        }

        @Test
        @DisplayName("[실패] 대상 사용자를 찾을 수 없음")
        void suspend_userNotFound_throwsException() {
            // given
            String userId = "non-existent-user";
            String suspenderUserId = "admin-id";

            when(authRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 정지 처리자를 찾을 수 없음")
        void suspend_suspenderNotFound_throwsException() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "non-existent-admin";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 정지 처리자가 ADMIN이 아님")
        void suspend_suspenderNotAdmin_throwsException() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "regular-user-id";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .build();

            Auth regularUser = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.USER)  // ADMIN이 아님
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(regularUser));

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.NOT_ADMIN);

            verify(keyProvider, never()).generateKey();
            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 이미 차단된 사용자")
        void suspend_alreadyBlockedUser_throwsException() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.BLOCKED)  // 이미 차단됨
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_ALREADY_BLOCKED);

            verify(keyProvider, never()).generateKey();
            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[경계] 매우 긴 정지 기간")
        void suspend_veryLongSuspendPeriod() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";
            Long suspendDays = 365L * 10;  // 10년

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, "사유", suspenderUserId, suspendDays);

            // then
            LocalDate expectedDate = LocalDate.now().plusDays(suspendDays);
            assertThat(user.getSuspensions().get(0).getSuspendUntil()).isEqualTo(expectedDate);
        }

        @Test
        @DisplayName("[경계] 1일 정지")
        void suspend_oneDaySuspend() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";
            Long suspendDays = 1L;

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            suspensionService.suspend(userId, "사유", suspenderUserId, suspendDays);

            // then
            LocalDate expectedDate = LocalDate.now().plusDays(1);
            assertThat(user.getSuspensions().get(0).getSuspendUntil()).isEqualTo(expectedDate);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("[통합] 정지 후 해제")
        void scenario_suspendThenRelease() {
            // given - 정지
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when - 정지
            suspensionService.suspend(userId, "부적절한 행위", suspenderUserId, 7L);

            // then
            assertThat(user.getStatus()).isEqualTo(Status.BLOCKED);
            assertThat(user.getSuspensions()).hasSize(1);

            // when - 해제
            suspensionService.release(userId);

            // then
            assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("[통합] 여러 번 정지된 사용자")
        void scenario_multipleSuspensions() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
		            .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-1", "suspend-2", "suspend-3");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when - 첫 번째 정지
            suspensionService.suspend(userId, "첫 번째 위반", suspenderUserId, 7L);
            suspensionService.release(userId);

            // when - 두 번째 정지
            suspensionService.suspend(userId, "두 번째 위반", suspenderUserId, 14L);
            suspensionService.release(userId);

            // when - 세 번째 정지
            suspensionService.suspend(userId, "세 번째 위반", suspenderUserId, 30L);

            // then - 정지 히스토리 확인
            assertThat(user.getSuspensions()).hasSize(3);
            assertThat(user.getStatus()).isEqualTo(Status.BLOCKED);
        }

        @Test
        @DisplayName("[통합] ADMIN이 일반 사용자로 변경되면 정지 불가")
        void scenario_adminDowngradedToUser() {
            // given
            String userId = "user-id-123";
            String suspenderUserId = "ex-admin-id";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .build();

            Auth exAdmin = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.USER)  // 이전에는 ADMIN이었지만 지금은 USER
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(exAdmin));

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.NOT_ADMIN);
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("[예외] null userId로 해제")
        void release_nullUserId_throwsException() {
            // when & then
            assertThatThrownBy(() -> suspensionService.release(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("[예외] null 파라미터로 정지")
        void suspend_nullParameters_throwsException() {
            // when & then
            assertThatThrownBy(() -> suspensionService.suspend(null, "사유", "admin", 7L))
                    .isInstanceOf(Exception.class);

            assertThatThrownBy(() -> suspensionService.suspend("user", "사유", null, 7L))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("권한 테스트")
    class AuthorizationTests {

        @Test
        @DisplayName("[권한] ADMIN만 정지 가능")
        void authorization_onlyAdminCanSuspend() {
            // given - USER 역할
            String userId = "user-id-123";
            String suspenderUserId = "regular-user-id";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .build();

            Auth regularUser = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.USER)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(regularUser));

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.NOT_ADMIN);

            // given - GUEST 역할
            Auth guest = Auth.builder()
                    .id(suspenderUserId)
                    .userRole(Role.GUEST)
                    .build();

            when(authRepository.findById(suspenderUserId)).thenReturn(Optional.of(guest));

      // when & then
      assertThatThrownBy(() -> suspensionService.suspend(userId, "사유", suspenderUserId, 7L))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.NOT_ADMIN);
        }

        @Test
        @DisplayName("[권한] ADMIN 역할 확인")
        void authorization_adminRoleCheck() {
            // given
            String userId = "user-id-123";
            String adminUserId = "admin-id-456";

            Auth user = Auth.builder()
                    .id(userId)
                    .status(Status.ACTIVE)
                    .suspensions(new ArrayList<>())
                    .build();

            Auth admin = Auth.builder()
                    .id(adminUserId)
                    .userRole(Role.ADMIN)
                    .build();

            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authRepository.findById(adminUserId)).thenReturn(Optional.of(admin));
            when(keyProvider.generateKey()).thenReturn("suspend-id");
            when(authRepository.save(any(Auth.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when & then - 예외가 발생하지 않음
            assertThatCode(() -> suspensionService.suspend(userId, "사유", adminUserId, 7L))
                    .doesNotThrowAnyException();
        }
    }
}
