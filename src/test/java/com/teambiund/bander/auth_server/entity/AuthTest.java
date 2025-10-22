package com.teambiund.bander.auth_server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.teambiund.bander.auth_server.auth.entity.*;
import com.teambiund.bander.auth_server.auth.entity.consentsname.ConsentsTable;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Auth 엔티티 테스트")
class AuthTest {

    private Auth auth;

    @BeforeEach
    void setUp() {
        auth = Auth.builder()
                .id("test-user-id")
                .email("test@example.com")
                .password("encoded-password")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Auth 엔티티 생성 테스트")
    void createAuthEntity() {
        // then
        assertThat(auth).isNotNull();
        assertThat(auth.getId()).isEqualTo("test-user-id");
        assertThat(auth.getEmail()).isEqualTo("test@example.com");
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getUserRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("History 추가 편의 메서드 테스트")
    void addHistory() {
        // given
        History history = History.builder()
                .id("history-id")
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        auth.addHistory(history);

        // then
        assertThat(auth.getHistory()).hasSize(1);
        assertThat(auth.getHistory()).contains(history);
        assertThat(history.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("Consent 추가 편의 메서드 테스트")
    void addConsent() {
    // given
    Consent consent =
        Consent.builder()
		        .id("consent-id")
		        .consentsTable(ConsentsTable.builder()
				        .id("consent-table-id")
				        .required(true)
				        .version("1.0")
				        .consentName("PRIVACY")
				        .build())
		        .build();

        // when
        auth.addConsent(consent);

        // then
        assertThat(auth.getConsent()).hasSize(1);
        assertThat(auth.getConsent()).contains(consent);
        assertThat(consent.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("Consent 제거 편의 메서드 테스트")
    void removeConsent() {
        // given
	    Consent consent =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id")
							    .required(true)
							    .version("1.0")
							    .consentName("PRIVACY")
							    .build())
					    .build();

        auth.addConsent(consent);

        // when
        auth.removeConsent(consent);

        // then
        assertThat(auth.getConsent()).isEmpty();
        assertThat(consent.getUser()).isNull();
    }
	

    @Test
    @DisplayName("Withdraw 설정 편의 메서드 테스트")
    void setWithdraw() {
        // given
        Withdraw withdraw = Withdraw.builder()
                .withdrawReason("개인 사유")
                .withdrawAt(LocalDateTime.now())
                .build();

        // when
        auth.setWithdraw(withdraw);

        // then
        assertThat(auth.getWithdraw()).isEqualTo(withdraw);
        assertThat(withdraw.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("Suspension 추가 편의 메서드 테스트")
    void addSuspension() {
        // given
        Suspend suspend = Suspend.builder()
                .id("suspend-id")
                .reason("부적절한 행동")
                .suspendAt(LocalDateTime.now())
                .suspenderUserId("admin-id")
                .build();

        // when
        auth.addSuspension(suspend);

        // then
        assertThat(auth.getSuspensions()).hasSize(1);
        assertThat(auth.getSuspensions()).contains(suspend);
        assertThat(suspend.getSuspendedUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("LoginStatus 설정 편의 메서드 테스트")
    void setLoginStatus() {
        // given
        LoginStatus loginStatus = LoginStatus.builder()
                .lastLogin(LocalDateTime.now())
                .build();

        // when
        auth.setLoginStatus(loginStatus);

        // then
        assertThat(auth.getLoginStatus()).isEqualTo(loginStatus);
        assertThat(loginStatus.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("회원 탈퇴 처리 편의 메서드 테스트")
    void markAsDeleted() {
        // given
        String withdrawReason = "서비스 불만족";

        // when
        auth.markAsDeleted(withdrawReason);

        // then
        assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(auth.getDeletedAt()).isNotNull();
        assertThat(auth.getWithdraw()).isNotNull();
        assertThat(auth.getWithdraw().getWithdrawReason()).isEqualTo(withdrawReason);
        assertThat(auth.getWithdraw().getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("회원 탈퇴 철회 편의 메서드 테스트")
    void cancelWithdrawal() {
        // given
        auth.markAsDeleted("서비스 불만족");
        assertThat(auth.getStatus()).isEqualTo(Status.DELETED);

        // when
        auth.cancelWithdrawal();

        // then
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getDeletedAt()).isNull();
        assertThat(auth.getWithdraw()).isNull();
    }

    @Test
    @DisplayName("여러 History 추가 테스트")
    void addMultipleHistories() {
        // given
        History history1 = History.builder()
                .id("history-id-1")
                .updatedColumn("email")
                .beforeColumnValue("old1@example.com")
                .afterColumnValue("new1@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        History history2 = History.builder()
                .id("history-id-2")
                .updatedColumn("password")
                .beforeColumnValue("old-password")
                .afterColumnValue("new-password")
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        auth.addHistory(history1);
        auth.addHistory(history2);

        // then
        assertThat(auth.getHistory()).hasSize(2);
        assertThat(auth.getHistory()).containsExactly(history1, history2);
        assertThat(history1.getUser()).isEqualTo(auth);
        assertThat(history2.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("여러 Consent 추가 및 제거 테스트")
    void addAndRemoveMultipleConsents() {
        // given
	    Consent consent1 =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id")
							    .required(true)
							    .version("1.0")
							    .consentName("PRIVACY")
							    .build())
					    .build();
	    
	    Consent consent2 =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id1")
							    .required(true)
							    .version("1.0")
							    .consentName("TERMS")
							    .build())
					    .build();

        // when
        auth.addConsent(consent1);
        auth.addConsent(consent2);

        // then
        assertThat(auth.getConsent()).hasSize(2);

        // when - consent1 제거
        auth.removeConsent(consent1);

        // then
        assertThat(auth.getConsent()).hasSize(1);
        assertThat(auth.getConsent()).contains(consent2);
        assertThat(consent1.getUser()).isNull();
    }

    @Test
    @DisplayName("여러 Suspension 추가 테스트")
    void addMultipleSuspensions() {
        // given
        Suspend suspend1 = Suspend.builder()
                .id("suspend-id-1")
                .reason("첫 번째 경고")
                .suspendAt(LocalDateTime.now())
                .suspenderUserId("admin-id")
                .build();

        Suspend suspend2 = Suspend.builder()
                .id("suspend-id-2")
                .reason("두 번째 경고")
                .suspendAt(LocalDateTime.now())
                .suspenderUserId("admin-id")
                .build();

        // when
        auth.addSuspension(suspend1);
        auth.addSuspension(suspend2);

        // then
        assertThat(auth.getSuspensions()).hasSize(2);
        assertThat(auth.getSuspensions()).containsExactly(suspend1, suspend2);
        assertThat(suspend1.getSuspendedUser()).isEqualTo(auth);
        assertThat(suspend2.getSuspendedUser()).isEqualTo(auth);
    }

    // ===== 파라미터 테스트 - 다양한 열거형 값 =====

    @ParameterizedTest
    @EnumSource(Provider.class)
    @DisplayName("[성공] 모든 Provider 타입으로 Auth 생성")
    void createAuthWithAllProviders(Provider provider) {
        // when
        Auth testAuth = Auth.builder()
                .id("user-" + provider.name())
                .email(provider.name().toLowerCase() + "@example.com")
                .provider(provider)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(testAuth.getProvider()).isEqualTo(provider);
        assertThat(testAuth.getEmail()).contains(provider.name().toLowerCase());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    @DisplayName("[성공] 모든 Status 타입으로 Auth 생성")
    void createAuthWithAllStatuses(Status status) {
        // when
        Auth testAuth = Auth.builder()
                .id("user-" + status.name())
                .email(status.name().toLowerCase() + "@example.com")
                .provider(Provider.SYSTEM)
                .status(status)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(testAuth.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("[성공] 모든 Role 타입으로 Auth 생성")
    void createAuthWithAllRoles(Role role) {
        // when
        Auth testAuth = Auth.builder()
                .id("user-" + role.name())
                .email(role.name().toLowerCase() + "@example.com")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(role)
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(testAuth.getUserRole()).isEqualTo(role);
    }

    @ParameterizedTest
    @ValueSource(strings = {"서비스 불만족", "사용 안 함", "다른 서비스 이용", "개인정보 우려", "기타"})
    @DisplayName("[성공] 다양한 탈퇴 사유로 회원 탈퇴 처리")
    void markAsDeletedWithVariousReasons(String reason) {
        // given
        Auth testAuth = Auth.builder()
                .id("user-withdraw-" + reason.hashCode())
                .email("withdraw@example.com")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        testAuth.markAsDeleted(reason);

        // then
        assertThat(testAuth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(testAuth.getWithdraw()).isNotNull();
        assertThat(testAuth.getWithdraw().getWithdrawReason()).isEqualTo(reason);
    }

    // ===== 엣지 케이스 =====

    @Test
    @DisplayName("[엣지 케이스] 빈 컬렉션 초기화 확인")
    void emptyCollectionsInitialization() {
        // then
        assertThat(auth.getHistory()).isNotNull().isEmpty();
        assertThat(auth.getConsent()).isNotNull().isEmpty();
        assertThat(auth.getSuspensions()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[엣지 케이스] 존재하지 않는 Consent 제거 시도")
    void removeNonExistentConsent() {
        // given
	    Consent consent1 =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id")
							    .required(true)
							    .version("1.0")
							    .consentName("PRIVACY")
							    .build())
					    .build();
	    
	    Consent consent2 =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id1")
							    .required(true)
							    .version("1.0")
							    .consentName("TERMS")
							    .build())
					    .build();




        auth.addConsent(consent1);

        // when
        auth.removeConsent(consent2); // 존재하지 않는 consent 제거 시도

        // then
        assertThat(auth.getConsent()).hasSize(1);
        assertThat(auth.getConsent()).contains(consent1);
    }

    @Test
    @DisplayName("[엣지 케이스] 이미 탈퇴한 회원 재탈퇴")
    void markAsDeletedTwice() {
        // given
        auth.markAsDeleted("첫 번째 탈퇴 사유");
        LocalDateTime firstDeletedAt = auth.getDeletedAt();

        // when
        try {
            Thread.sleep(10); // 시간 차이를 보장
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        auth.markAsDeleted("두 번째 탈퇴 사유");

        // then
        assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(auth.getDeletedAt()).isAfterOrEqualTo(firstDeletedAt);
        assertThat(auth.getWithdraw().getWithdrawReason()).isEqualTo("두 번째 탈퇴 사유");
    }

    @Test
    @DisplayName("[엣지 케이스] 탈퇴하지 않은 회원 탈퇴 철회 시도")
    void cancelWithdrawalWithoutWithdraw() {
        // given
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getWithdraw()).isNull();

        // when
        auth.cancelWithdrawal();

        // then
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getWithdraw()).isNull();
        assertThat(auth.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("[엣지 케이스] 탈퇴와 철회 반복 시나리오")
    void repeatedWithdrawAndCancel() {
        // 첫 번째 탈퇴
        auth.markAsDeleted("첫 번째 탈퇴");
        assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(auth.getWithdraw()).isNotNull();

        // 첫 번째 철회
        auth.cancelWithdrawal();
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getWithdraw()).isNull();

        // 두 번째 탈퇴
        auth.markAsDeleted("두 번째 탈퇴");
        assertThat(auth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(auth.getWithdraw()).isNotNull();

        // 두 번째 철회
        auth.cancelWithdrawal();
        assertThat(auth.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(auth.getWithdraw()).isNull();
    }

    @Test
    @DisplayName("[엣지 케이스] 소셜 로그인 사용자는 비밀번호 null")
    void socialLoginUserWithoutPassword() {
        // when
        Auth socialAuth = Auth.builder()
                .id("user-social")
                .email("social@example.com")
                .provider(Provider.GOOGLE)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // then
        assertThat(socialAuth.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(socialAuth.getPassword()).isNull();
    }

    @Test
    @DisplayName("[엣지 케이스] 전화번호 null 허용")
    void phoneNumberCanBeNull() {
        // then
        assertThat(auth.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("[경계 케이스] 과거 날짜로 createdAt 설정")
    void createAuthWithPastDate() {
        // given
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // when
        Auth pastAuth = Auth.builder()
                .id("user-past")
                .email("past@example.com")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(pastDate)
                .build();

        // then
        assertThat(pastAuth.getCreatedAt()).isEqualTo(pastDate);
        assertThat(pastAuth.getCreatedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("[경계 케이스] Withdraw null 설정")
    void setWithdrawNull() {
        // given
        Withdraw withdraw = Withdraw.builder()
                .withdrawReason("탈퇴 사유")
                .withdrawAt(LocalDateTime.now())
                .build();
        auth.setWithdraw(withdraw);

        // when
        auth.setWithdraw(null);

        // then
        assertThat(auth.getWithdraw()).isNull();
    }

    @Test
    @DisplayName("[경계 케이스] LoginStatus null 설정")
    void setLoginStatusNull() {
        // given
        LoginStatus loginStatus = LoginStatus.builder()
                .lastLogin(LocalDateTime.now())
                .build();
        auth.setLoginStatus(loginStatus);

        // when
        auth.setLoginStatus(null);

        // then
        assertThat(auth.getLoginStatus()).isNull();
    }

    @Test
    @DisplayName("[통합 시나리오] 모든 연관 엔티티 추가")
    void addAllAssociatedEntities() {
        // given
        History history = History.builder()
                .id("history-id")
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(LocalDateTime.now())
                .build();
	    
	    Consent consent =
			    Consent.builder()
					    .id("consent-id")
					    .consentsTable(ConsentsTable.builder()
							    .id("consent-table-id")
							    .required(true)
							    .version("1.0")
							    .consentName("PRIVACY")
							    .build())
					    .build();

        Withdraw withdraw = Withdraw.builder()
                .withdrawReason("탈퇴 사유")
                .withdrawAt(LocalDateTime.now())
                .build();

        Suspend suspend = Suspend.builder()
                .id("suspend-id")
                .reason("정지 사유")
                .suspendAt(LocalDateTime.now())
                .suspendUntil(LocalDate.now().plusDays(7))
                .suspenderUserId("admin-id")
                .build();

        LoginStatus loginStatus = LoginStatus.builder()
                .lastLogin(LocalDateTime.now())
                .build();

        // when
        auth.addHistory(history);
        auth.addConsent(consent);
        auth.setWithdraw(withdraw);
        auth.addSuspension(suspend);
        auth.setLoginStatus(loginStatus);

        // then
        assertThat(auth.getHistory()).hasSize(1).contains(history);
        assertThat(auth.getConsent()).hasSize(1).contains(consent);
        assertThat(auth.getWithdraw()).isEqualTo(withdraw);
        assertThat(auth.getSuspensions()).hasSize(1).contains(suspend);
        assertThat(auth.getLoginStatus()).isEqualTo(loginStatus);

        // 양방향 연관관계 확인
        assertThat(history.getUser()).isEqualTo(auth);
        assertThat(consent.getUser()).isEqualTo(auth);
        assertThat(withdraw.getUser()).isEqualTo(auth);
        assertThat(suspend.getSuspendedUser()).isEqualTo(auth);
        assertThat(loginStatus.getUser()).isEqualTo(auth);
    }
}
