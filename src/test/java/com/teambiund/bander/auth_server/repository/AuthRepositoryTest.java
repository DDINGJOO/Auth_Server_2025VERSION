package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.*;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambiund.bander.auth_server.repository.ConsentTableRepository;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.util.data.ConsentTable_init;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * AuthRepository 테스트
 * - @DataJpaTest: JPA 컴포넌트만 로드 (Service, Controller 제외)
 * - TestEntityManager: 순수 JPA 작업을 위한 헬퍼
 * - 모든 테스트는 자동으로 롤백됨 (@Transactional)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(AuthRepositoryTest.TestConfig.class)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("AuthRepository 테스트")
public class AuthRepositoryTest {
	
	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public KafkaTemplate<String, Object> kafkaTemplate() {
			return mock(KafkaTemplate.class);
		}

		@Bean
		@Primary
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		@Primary
		public StringRedisTemplate stringRedisTemplate() {
			return mock(StringRedisTemplate.class);
		}

		@Bean("aesCipherStrategy")
		@Primary
		public CipherStrategy aesCipherStrategy() {
			return mock(CipherStrategy.class);
		}

		@Bean("pbkdf2CipherStrategy")
		@Primary
		public CipherStrategy pbkdf2CipherStrategy() {
			return mock(CipherStrategy.class);
		}

		@Bean
		@Primary
		public ConsentTableRepository consentTableRepository() {
			return mock(ConsentTableRepository.class);
		}
	}
    @Autowired
    private TestEntityManager em;

    @Autowired
    private AuthRepository authRepository;

    // ===== 기본 CRUD 테스트 =====

    @Test
    @DisplayName("[성공] save - 새로운 Auth 저장")
    void saveNewAuth() {
        // given
        Auth auth = createTestAuth("save@example.com");

        // when
        Auth saved = authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        assertThat(saved.getId()).isNotNull();
        Optional<Auth> found = authRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("save@example.com");
    }

    @Test
    @DisplayName("[성공] findById - 존재하는 ID로 조회")
    void findByIdSuccess() {
        // given
        Auth auth = createTestAuth("findid@example.com");
        em.persist(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> found = authRepository.findById(auth.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findid@example.com");
    }

    @Test
    @DisplayName("[실패] findById - 존재하지 않는 ID로 조회")
    void findByIdNotFound() {
        // when
        Optional<Auth> found = authRepository.findById("non-existent-id");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("[성공] findByEmail - 존재하는 이메일로 조회")
    void findByEmailSuccess() {
        // given
        Auth auth = createTestAuth("findmail@example.com");
        em.persist(auth);
        em.flush();

        // when
        Optional<Auth> found = authRepository.findByEmail("findmail@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findmail@example.com");
    }

    @Test
    @DisplayName("[실패] findByEmail - 존재하지 않는 이메일로 조회")
    void findByEmailNotFound() {
        // when
        Optional<Auth> found = authRepository.findByEmail("notfound@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("[성공] existsByEmail - 존재하는 이메일")
    void existsByEmailTrue() {
        // given
        Auth auth = createTestAuth("exists@example.com");
        em.persist(auth);
        em.flush();

        // when
        boolean exists = authRepository.existsByEmail("exists@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("[실패] existsByEmail - 존재하지 않는 이메일")
    void existsByEmailFalse() {
        // when
        boolean exists = authRepository.existsByEmail("notexists@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("[성공] update - Auth 업데이트")
    void updateAuth() {
        // given
        Auth auth = createTestAuth("update@example.com");
        em.persist(auth);
        em.flush();
        em.clear();

        // when
        Auth found = authRepository.findById(auth.getId()).orElseThrow();
        found.setStatus(Status.SUSPENDED);
        found.setUpdatedAt(LocalDateTime.now());
        authRepository.save(found);
        em.flush();
        em.clear();

        // then
        Auth updated = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.SUSPENDED);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] delete - Auth 삭제")
    void deleteAuth() {
        // given
        Auth auth = createTestAuth("delete@example.com");
        em.persist(auth);
        em.flush();
        String authId = auth.getId();

        // when
        authRepository.delete(auth);
        em.flush();

        // then
        Optional<Auth> deleted = authRepository.findById(authId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("[성공] findAll - 모든 Auth 조회")
    void findAllAuths() {
        // given
        Auth auth1 = createTestAuth("user1@example.com");
        Auth auth2 = createTestAuth("user2@example.com");
        Auth auth3 = createTestAuth("user3@example.com");
        em.persist(auth1);
        em.persist(auth2);
        em.persist(auth3);
        em.flush();

        // when
        List<Auth> all = authRepository.findAll();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(3);
        assertThat(all).extracting(Auth::getEmail)
                .contains("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    @DisplayName("[성공] count - Auth 개수 확인")
    void countAuths() {
        // given
        Auth auth1 = createTestAuth("count1@example.com");
        Auth auth2 = createTestAuth("count2@example.com");
        em.persist(auth1);
        em.persist(auth2);
        em.flush();

        // when
        long count = authRepository.count();

        // then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    // ===== Cascade 및 연관관계 테스트 =====

    @Test
    @DisplayName("[성공] Auth와 History Cascade 저장")
    void saveAuthWithHistoryCascade() {
        // given
        Auth auth = createTestAuth("cascade@example.com");

        History history = History.builder()
                .id("history-1")
                .updatedColumn("email")
                .beforeColumnValue("old@example.com")
                .afterColumnValue("cascade@example.com")
                .updatedAt(LocalDateTime.now())
                .build();

        auth.addHistory(history);

        // when
        authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        Auth saved = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(saved.getHistory()).hasSize(1);
        assertThat(saved.getHistory().get(0).getUpdatedColumn()).isEqualTo("email");
        assertThat(saved.getHistory().get(0).getUser()).isEqualTo(saved);
    }

    @Test
    @DisplayName("[성공] Auth와 Consent Cascade 저장")
    void saveAuthWithConsentCascade() {
        // given
        Auth auth = createTestAuth("consent@example.com");

        Consent consent1 = Consent.builder()
                .id("consent-1")
                .consentType("PRIVACY")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();

        Consent consent2 = Consent.builder()
                .id("consent-2")
                .consentType("TERMS")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();

        auth.addConsent(consent1);
        auth.addConsent(consent2);

        // when
        authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        Auth saved = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(saved.getConsent()).hasSize(2);
        assertThat(saved.getConsent())
                .extracting(Consent::getConsentType)
                .containsExactlyInAnyOrder("PRIVACY", "TERMS");
    }

    @Test
    @DisplayName("[성공] Consent OrphanRemoval 테스트")
    void consentOrphanRemoval() {
        // given
        Auth auth = createTestAuth("orphan@example.com");

        Consent consent1 = Consent.builder()
                .id("consent-1")
                .consentType("PRIVACY")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();

        Consent consent2 = Consent.builder()
                .id("consent-2")
                .consentType("TERMS")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();

        auth.addConsent(consent1);
        auth.addConsent(consent2);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Auth found = authRepository.findById(auth.getId()).orElseThrow();
        Consent toRemove = found.getConsent().get(0);
        found.removeConsent(toRemove);
        authRepository.save(found);
        em.flush();
        em.clear();

        // then
        Auth result = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(result.getConsent()).hasSize(1);
    }

    @Test
    @DisplayName("[성공] Auth와 Withdraw Cascade 저장")
    void saveAuthWithWithdrawCascade() {
        // given
        Auth auth = createTestAuth("withdraw@example.com");

        Withdraw withdraw = Withdraw.builder()
                .withdrawReason("테스트 탈퇴")
                .withdrawAt(LocalDateTime.now())
                .build();

        auth.setWithdraw(withdraw);
        auth.setStatus(Status.DELETED);
        auth.setDeletedAt(LocalDateTime.now());

        // when
        authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        Auth saved = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(saved.getWithdraw()).isNotNull();
        assertThat(saved.getWithdraw().getWithdrawReason()).isEqualTo("테스트 탈퇴");
        assertThat(saved.getWithdraw().getUser()).isEqualTo(saved);
    }

    // ===== 편의 메서드 테스트 =====

    @Test
    @DisplayName("[성공] markAsDeleted 편의 메서드")
    void markAsDeletedMethod() {
        // given
        Auth auth = createTestAuth("markas@example.com");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Auth found = authRepository.findById(auth.getId()).orElseThrow();
        found.markAsDeleted("서비스 불만족");
        authRepository.save(found);
        em.flush();
        em.clear();

        // then
        Auth result = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.DELETED);
        assertThat(result.getDeletedAt()).isNotNull();
        assertThat(result.getWithdraw()).isNotNull();
        assertThat(result.getWithdraw().getWithdrawReason()).isEqualTo("서비스 불만족");
    }

    @Test
    @DisplayName("[성공] cancelWithdrawal 편의 메서드")
    void cancelWithdrawalMethod() {
        // given
        Auth auth = createTestAuth("cancel@example.com");
        auth.markAsDeleted("테스트");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Auth found = authRepository.findById(auth.getId()).orElseThrow();
        found.cancelWithdrawal();
        authRepository.save(found);
        em.flush();
        em.clear();

        // then
        Auth result = authRepository.findById(auth.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getWithdraw()).isNull();
    }

    // ===== Custom Query 메서드 테스트 =====

    @Test
    @DisplayName("[성공] findByEmailWithHistory - History 있음")
    void findByEmailWithHistoryWithData() {
        // given
        Auth auth = createTestAuth("history@example.com");

        History history = History.builder()
                .id("history-1")
                .updatedColumn("password")
                .beforeColumnValue("old")
                .afterColumnValue("new")
                .updatedAt(LocalDateTime.now())
                .build();

        auth.addHistory(history);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> result = authRepository.findByEmailWithHistory("history@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getHistory()).hasSize(1);
        assertThat(result.get().getHistory().get(0).getUpdatedColumn()).isEqualTo("password");
    }

    @Test
    @DisplayName("[성공] findByEmailWithHistory - History 없음")
    void findByEmailWithHistoryEmpty() {
        // given
        Auth auth = createTestAuth("nohistory@example.com");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> result = authRepository.findByEmailWithHistory("nohistory@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getHistory()).isEmpty();
    }

    @Test
    @DisplayName("[성공] findByEmailWithConsent - Consent 있음")
    void findByEmailWithConsentWithData() {
        // given
        Auth auth = createTestAuth("withconsent@example.com");

        Consent consent = Consent.builder()
                .id("consent-1")
                .consentType("PRIVACY")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();

        auth.addConsent(consent);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> result = authRepository.findByEmailWithConsent("withconsent@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getConsent()).hasSize(1);
        assertThat(result.get().getConsent().get(0).getConsentType()).isEqualTo("PRIVACY");
    }

    @Test
    @DisplayName("[성공] findByEmailWithConsent - Consent 없음")
    void findByEmailWithConsentEmpty() {
        // given
        Auth auth = createTestAuth("noconsent@example.com");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> result = authRepository.findByEmailWithConsent("noconsent@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getConsent()).isEmpty();
    }

    @Test
    @DisplayName("[성공] findByIdWithConsent")
    void findByIdWithConsent() {
        // given
        Auth auth = createTestAuth("idconsent@example.com");

        Consent consent = Consent.builder()
                .id("consent-1")
                .consentType("TERMS")
                .version("2.0")
                .agreementAt(LocalDateTime.now())
                .build();

        auth.addConsent(consent);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // when
        Optional<Auth> result = authRepository.findByIdWithConsent(auth.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getConsent()).hasSize(1);
        assertThat(result.get().getConsent().get(0).getVersion()).isEqualTo("2.0");
    }

    @Test
    @DisplayName("[성공] deleteByDeletedAtBefore - 조건 충족")
    void deleteByDeletedAtBeforeMatch() {
        // given
        Auth oldAuth = createTestAuth("old@example.com");
        oldAuth.setDeletedAt(LocalDateTime.now().minusYears(4));
        authRepository.save(oldAuth);

        Auth recentAuth = createTestAuth("recent@example.com");
        recentAuth.setDeletedAt(LocalDateTime.now().minusDays(1));
        authRepository.save(recentAuth);

        em.flush();

        // when
        authRepository.deleteByDeletedAtBefore(LocalDateTime.now().minusYears(3));
        em.flush();

        // then
        assertThat(authRepository.existsByEmail("old@example.com")).isFalse();
        assertThat(authRepository.existsByEmail("recent@example.com")).isTrue();
    }

    @Test
    @DisplayName("[성공] deleteByDeletedAtBefore - 조건 미충족")
    void deleteByDeletedAtBeforeNoMatch() {
        // given
        Auth auth = createTestAuth("nodelete@example.com");
        auth.setDeletedAt(LocalDateTime.now().minusDays(1));
        authRepository.save(auth);
        em.flush();
        long beforeCount = authRepository.count();

        // when
        authRepository.deleteByDeletedAtBefore(LocalDateTime.now().minusYears(10));
        em.flush();

        // then
        long afterCount = authRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount);
    }

    // ===== 복합 시나리오 테스트 =====

    @Test
    @DisplayName("[통합] 회원 가입부터 탈퇴까지 전체 흐름")
    void fullUserLifecycle() {
        // 1. 회원 가입
        Auth auth = createTestAuth("lifecycle@example.com");
        Auth saved = authRepository.save(auth);
        em.flush();
        em.clear();
        assertThat(authRepository.existsByEmail("lifecycle@example.com")).isTrue();

        // 2. 동의서 추가
        auth = authRepository.findById(saved.getId()).orElseThrow();
        Consent consent = Consent.builder()
                .id("consent-lifecycle")
                .consentType("PRIVACY")
                .version("1.0")
                .agreementAt(LocalDateTime.now())
                .build();
        auth.addConsent(consent);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // 3. 정보 변경 및 이력 생성
        auth = authRepository.findById(saved.getId()).orElseThrow();
        History history = History.builder()
                .id("history-lifecycle")
                .updatedColumn("email")
                .beforeColumnValue("lifecycle@example.com")
                .afterColumnValue("newlifecycle@example.com")
                .updatedAt(LocalDateTime.now())
                .build();
        auth.addHistory(history);
        auth.setEmail("newlifecycle@example.com");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // 4. 계정 정지
        auth = authRepository.findById(saved.getId()).orElseThrow();
        auth.setStatus(Status.SUSPENDED);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // 5. 정지 해제
        auth = authRepository.findById(saved.getId()).orElseThrow();
        auth.setStatus(Status.ACTIVE);
        authRepository.save(auth);
        em.flush();
        em.clear();

        // 6. 회원 탈퇴
        auth = authRepository.findById(saved.getId()).orElseThrow();
        auth.markAsDeleted("테스트 종료");
        authRepository.save(auth);
        em.flush();
        em.clear();

        // 최종 확인
        Auth finalAuth = authRepository.findById(saved.getId()).orElseThrow();
        assertThat(finalAuth.getStatus()).isEqualTo(Status.DELETED);
        assertThat(finalAuth.getWithdraw()).isNotNull();
        assertThat(finalAuth.getHistory()).hasSize(1);
        assertThat(finalAuth.getConsent()).hasSize(1);
    }

    @Test
    @DisplayName("[경계 케이스] 대량의 History와 함께 저장 및 조회")
    void saveAndFindAuthWithManyHistories() {
        // given
        Auth auth = createTestAuth("manyhistories@example.com");

        for (int i = 0; i < 50; i++) {
            History history = History.builder()
                    .id("history-" + i)
                    .updatedColumn("column-" + i)
                    .beforeColumnValue("before-" + i)
                    .afterColumnValue("after-" + i)
                    .updatedAt(LocalDateTime.now())
                    .build();
            auth.addHistory(history);
        }

        // when
        authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        Optional<Auth> result = authRepository.findByEmailWithHistory("manyhistories@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getHistory()).hasSize(50);
    }

    @Test
    @DisplayName("[경계 케이스] 대량의 Consent와 함께 저장 및 조회")
    void saveAndFindAuthWithManyConsents() {
        // given
        Auth auth = createTestAuth("manyconsents@example.com");

        for (int i = 0; i < 30; i++) {
            Consent consent = Consent.builder()
                    .id("consent-" + i)
                    .consentType("TYPE-" + i)
                    .version("1.0")
                    .agreementAt(LocalDateTime.now())
                    .build();
            auth.addConsent(consent);
        }

        // when
        authRepository.save(auth);
        em.flush();
        em.clear();

        // then
        Optional<Auth> result = authRepository.findByEmailWithConsent("manyconsents@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getConsent()).hasSize(30);
    }

    @Test
    @DisplayName("[엣지 케이스] 중복 이메일로 여러 Auth 저장")
    void saveDuplicateEmail() {
        // given
        Auth auth1 = createTestAuth("duplicate@example.com");
        Auth auth2 = Auth.builder()
                .id("different-id")
                .email("duplicate@example.com")
                .password("password")
                .provider(Provider.GOOGLE)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        authRepository.save(auth1);
        authRepository.save(auth2);
        em.flush();

        // then - 중복 허용됨 (DB 제약이 없다면)
        List<Auth> all = authRepository.findAll();
        long duplicateCount = all.stream()
                .filter(a -> "duplicate@example.com".equals(a.getEmail()))
                .count();
        assertThat(duplicateCount).isGreaterThanOrEqualTo(1);
    }

    // Helper method
    private Auth createTestAuth(String email) {
        return Auth.builder()
                .id("user-" + email.hashCode())
                .email(email)
                .password("encoded-password")
                .provider(Provider.SYSTEM)
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }
	
}
