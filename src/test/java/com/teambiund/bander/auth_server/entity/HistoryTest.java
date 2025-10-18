package com.teambiund.bander.auth_server.entity;

import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("History 엔티티 테스트")
class HistoryTest {

    private Auth auth;

    @BeforeEach
    void setUp() {
        auth = Auth.builder()
                .id("test-user-id")
                .email("test@example.com")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== 성공 케이스 =====

    @Test
    @DisplayName("[성공] History 엔티티 생성 - 모든 필드 포함")
    void createHistoryEntity() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        History history = History.builder()
                .id("history-id-1")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(now)
                .build();

        // then
        assertThat(history.getId()).isEqualTo("history-id-1");
        assertThat(history.getUser()).isEqualTo(auth);
        assertThat(history.getUpdatedColumn()).isEqualTo("email");
        assertThat(history.getBeforeColumnValue()).isEqualTo("old@example.com");
        assertThat(history.getAfterColumnValue()).isEqualTo("new@example.com");
        assertThat(history.getUpdatedAt()).isEqualTo(now);
        assertThat(history.getVersion()).isEqualTo(0); // 초기 버전
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "phoneNumber", "status"})
    @DisplayName("[성공] 다양한 컬럼 변경 이력 생성")
    void createHistoryWithVariousColumns(String column) {
        // when
        History history = History.builder()
                .id("history-" + column)
                .user(auth)
                .updatedColumn(column)
                .beforeColumnValue("old-value")
                .afterColumnValue("new-value")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getUpdatedColumn()).isEqualTo(column);
    }

    @Test
    @DisplayName("[성공] 비밀번호 변경 이력 생성")
    void createPasswordChangeHistory() {
        // when
        History history = History.builder()
                .id("history-password")
                .user(auth)
                .updatedColumn("password")
                .beforeColumnValue("old-encrypted-password")
                .afterColumnValue("new-encrypted-password")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getUpdatedColumn()).isEqualTo("password");
        assertThat(history.getBeforeColumnValue()).isNotEqualTo(history.getAfterColumnValue());
    }

    @Test
    @DisplayName("[성공] 이메일 변경 이력 생성")
    void createEmailChangeHistory() {
        // when
        History history = History.builder()
                .id("history-email")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getUpdatedColumn()).isEqualTo("email");
        assertThat(history.getBeforeColumnValue()).contains("old");
        assertThat(history.getAfterColumnValue()).contains("new");
    }

    @Test
    @DisplayName("[성공] 전화번호 변경 이력 생성")
    void createPhoneNumberChangeHistory() {
        // when
        History history = History.builder()
                .id("history-phone")
                .user(auth)
                .updatedColumn("phoneNumber")
                .beforeColumnValue("010-1111-1111")
                .afterColumnValue("010-2222-2222")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getUpdatedColumn()).isEqualTo("phoneNumber");
        assertThat(history.getBeforeColumnValue()).isEqualTo("010-1111-1111");
        assertThat(history.getAfterColumnValue()).isEqualTo("010-2222-2222");
    }

    // ===== 엣지 케이스 =====

    @Test
    @DisplayName("[엣지 케이스] null 값으로 변경된 경우")
    void createHistoryWithNullValue() {
        // when
        History history = History.builder()
                .id("history-null")
                .user(auth)
                .updatedColumn("phoneNumber")
                .beforeColumnValue("010-1234-5678")
                .afterColumnValue(null)
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getBeforeColumnValue()).isEqualTo("010-1234-5678");
        assertThat(history.getAfterColumnValue()).isNull();
    }

    @Test
    @DisplayName("[엣지 케이스] null에서 값으로 변경된 경우")
    void createHistoryFromNullValue() {
        // when
        History history = History.builder()
                .id("history-from-null")
                .user(auth)
                .updatedColumn("phoneNumber")
                .beforeColumnValue(null)
                .afterColumnValue("010-1234-5678")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getBeforeColumnValue()).isNull();
        assertThat(history.getAfterColumnValue()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("[엣지 케이스] 같은 값으로 변경 (실제로는 변경 없음)")
    void createHistoryWithSameValue() {
        // when
        History history = History.builder()
                .id("history-same")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("test@example.com")
                .afterColumnValue("test@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getBeforeColumnValue()).isEqualTo(history.getAfterColumnValue());
    }

    @Test
    @DisplayName("[경계 케이스] 빈 문자열로 변경")
    void createHistoryWithEmptyString() {
        // when
        History history = History.builder()
                .id("history-empty")
                .user(auth)
                .updatedColumn("phoneNumber")
                .beforeColumnValue("010-1234-5678")
                .afterColumnValue("")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getBeforeColumnValue()).isEqualTo("010-1234-5678");
        assertThat(history.getAfterColumnValue()).isEmpty();
    }

    @Test
    @DisplayName("[경계 케이스] 매우 긴 값 변경")
    void createHistoryWithLongValue() {
        // given
        String longValue = "a".repeat(1000);

        // when
        History history = History.builder()
                .id("history-long")
                .user(auth)
                .updatedColumn("description")
                .beforeColumnValue("short")
                .afterColumnValue(longValue)
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history.getAfterColumnValue()).hasSize(1000);
    }

    @Test
    @DisplayName("[경계 케이스] 과거 날짜로 변경 이력 생성")
    void createHistoryWithPastDate() {
        // given
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);

        // when
        History history = History.builder()
                .id("history-past")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(pastDate)
                .build();

        // then
        assertThat(history.getUpdatedAt()).isEqualTo(pastDate);
        assertThat(history.getUpdatedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("[성공] Setter를 통한 값 변경")
    void updateHistoryValues() {
        // given
        History history = History.builder()
                .id("history-update")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        history.setUpdatedColumn("password");
        history.setBeforeColumnValue("old-password");
        history.setAfterColumnValue("new-password");
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        history.setUpdatedAt(newTime);

        // then
        assertThat(history.getUpdatedColumn()).isEqualTo("password");
        assertThat(history.getBeforeColumnValue()).isEqualTo("old-password");
        assertThat(history.getAfterColumnValue()).isEqualTo("new-password");
        assertThat(history.getUpdatedAt()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("[성공] User 연관관계 설정")
    void setUser() {
        // given
        History history = History.builder()
                .id("history-user")
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("new@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        history.setUser(auth);

        // then
        assertThat(history.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("[성공] 여러 변경 이력을 시간순으로 구분")
    void createMultipleHistoriesInTimeOrder() throws InterruptedException {
        // given & when
        History history1 = History.builder()
                .id("history-1")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("email1@example.com")
                .afterColumnValue("email2@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        Thread.sleep(10);

        History history2 = History.builder()
                .id("history-2")
                .user(auth)
                .updatedColumn("email")
                .beforeColumnValue("email2@example.com")
                .afterColumnValue("email3@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(history2.getUpdatedAt()).isAfter(history1.getUpdatedAt());
    }
}
