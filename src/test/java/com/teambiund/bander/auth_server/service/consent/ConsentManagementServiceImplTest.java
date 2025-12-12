package com.teambiund.bander.auth_server.service.consent;

import static com.teambiund.bander.auth_server.auth.util.data.ConsentTableInit.consentsAllMaps;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.Consent;
import com.teambiund.bander.auth_server.auth.entity.consentsname.ConsentsTable;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.event.publish.UserConsentChangedEventPub;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.consent.impl.ConsentManagementServiceImpl;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsentManagementServiceImpl 테스트")
class ConsentManagementServiceImplTest {

  @Mock private AuthRepository authRepository;

  @Mock private KeyProvider keyProvider;

  @Mock private UserConsentChangedEventPub userConsentChangedEventPub;

  @InjectMocks private ConsentManagementServiceImpl consentService;

  /** 각 테스트 실행 전 ConsentsTable Mock 데이터 초기화 */
  @BeforeEach
  void setUp() {
    // consentsAllMaps를 초기화하고 테스트용 ConsentsTable 데이터 추가
    consentsAllMaps.clear();
    consentsAllMaps.put(
        TestFixture.CONSENT_ID_TERMS,
        ConsentsTable.builder()
            .id(TestFixture.CONSENT_ID_TERMS)
            .consentName("TERMS_OF_SERVICE")
            .version("v1.0")
            .consentUrl("https://example.com/terms")
            .required(true)
            .build());
    consentsAllMaps.put(
        TestFixture.CONSENT_ID_PRIVACY,
        ConsentsTable.builder()
            .id(TestFixture.CONSENT_ID_PRIVACY)
            .consentName("PRIVACY_POLICY")
            .version("v1.0")
            .consentUrl("https://example.com/privacy")
            .required(true)
            .build());
    consentsAllMaps.put(
        TestFixture.CONSENT_ID_MARKETING,
        ConsentsTable.builder()
            .id(TestFixture.CONSENT_ID_MARKETING)
            .consentName("MARKETING")
            .version("v1.0")
            .consentUrl("https://example.com/marketing")
            .required(false)
            .build());
  }

  /** 테스트 픽스처: 재사용 가능한 테스트 객체 생성 헬퍼 클래스 */
  static class TestFixture {
    // 테스트용 Consent ID 상수
    static final String CONSENT_ID_TERMS = "test-consent-id-terms";
    static final String CONSENT_ID_PRIVACY = "test-consent-id-privacy";
    static final String CONSENT_ID_MARKETING = "test-consent-id-marketing";

    /** Auth 엔티티 생성 */
    static Auth createAuth(String userId) {
      return Auth.builder()
          .id(userId)
          .email("test@example.com")
          .status(Status.ACTIVE)
          .consent(new ArrayList<>())
          .build();
    }

    /** Consent 엔티티 생성 */
    static Consent createConsent(String id, String consentType) {
      return Consent.builder().id(id).consentsTable(consentsAllMaps.get(consentType)).build();
    }

    /** ConsentRequest 생성 (동의) */
    static ConsentRequest createConsentRequest(String consentId, boolean consented) {
      return ConsentRequest.builder().consentId(consentId).consented(consented).build();
    }

    /** ConsentRequest 리스트 생성 (모두 동의) */
    static List<ConsentRequest> createConsentRequests(String... consentIds) {
      List<ConsentRequest> requests = new ArrayList<>();
      for (String consentId : consentIds) {
        requests.add(createConsentRequest(consentId, true));
      }
      return requests;
    }
  }

  @Nested
  @DisplayName("동의 저장 테스트")
  class SaveConsentTests {

    @Test
    @DisplayName("[성공] 동의 정보 저장")
    void saveConsent_validRequests_success() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");
      List<ConsentRequest> requests =
          TestFixture.createConsentRequests(
              TestFixture.CONSENT_ID_TERMS, TestFixture.CONSENT_ID_PRIVACY);

      when(keyProvider.generateKey()).thenReturn("consent-id-1", "consent-id-2");

      // when
      consentService.saveConsent(auth, requests);

      // then
      assertThat(auth.getConsent()).hasSize(2);
      verify(keyProvider, times(2)).generateKey();
    }

    @Test
    @DisplayName("[성공] consented=false인 항목은 저장하지 않음")
    void saveConsent_filteredByConsented_success() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");
      List<ConsentRequest> requests =
          List.of(
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true),
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_MARKETING, false));

      when(keyProvider.generateKey()).thenReturn("consent-id-1");

      // when
      consentService.saveConsent(auth, requests);

      // then
      assertThat(auth.getConsent()).hasSize(1);
      verify(keyProvider, times(1)).generateKey();
    }

    @Test
    @DisplayName("[성공] 빈 리스트 처리")
    void saveConsent_emptyList_success() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");
      List<ConsentRequest> requests = new ArrayList<>();

      // when
      consentService.saveConsent(auth, requests);

      // then
      assertThat(auth.getConsent()).isEmpty();
      verify(keyProvider, never()).generateKey();
    }

    @Test
    @DisplayName("[검증] 양방향 연관관계 설정")
    void saveConsent_bidirectionalRelationship() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");
      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true));

      when(keyProvider.generateKey()).thenReturn("consent-id");

      // when
      consentService.saveConsent(auth, requests);

      // then
      Consent savedConsent = auth.getConsent().get(0);
      assertThat(savedConsent.getUser()).isEqualTo(auth);
    }

    @Test
    @DisplayName("[검증] 동의 필드가 올바르게 설정됨")
    void saveConsent_fieldsSetCorrectly() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");
      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true));

      when(keyProvider.generateKey()).thenReturn("consent-id-123");

      // when
      consentService.saveConsent(auth, requests);

      // then
      Consent savedConsent = auth.getConsent().get(0);
      assertThat(savedConsent.getId()).isEqualTo("consent-id-123");
      assertThat(savedConsent.getConsentsTable().getConsentName()).isEqualTo("TERMS_OF_SERVICE");
      assertThat(savedConsent.getConsentsTable().getVersion()).isEqualTo("v1.0");
      assertThat(savedConsent.getConsentsTable().getConsentUrl())
          .isEqualTo("https://example.com/terms");
      assertThat(savedConsent.getConsentedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("동의 변경 테스트")
  class ChangeConsentTests {

    @Test
    @DisplayName("[성공] 새로운 동의 추가")
    void changeConsent_addNewConsent_success() {
      // given
      String userId = "user-id-123";
      Consent existingConsent =
          TestFixture.createConsent("existing-id", TestFixture.CONSENT_ID_TERMS);

      Auth auth = TestFixture.createAuth(userId);
      auth.setConsent(new ArrayList<>(List.of(existingConsent)));
      existingConsent.setUser(auth);

      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_PRIVACY, true));

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
      when(keyProvider.generateKey()).thenReturn("new-consent-id");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      consentService.changeConsent(userId, requests);

      // then
      assertThat(auth.getConsent()).hasSize(2);
      verify(authRepository).findByIdWithConsent(userId);
      verify(authRepository).save(auth);
    }

    @Test
    @DisplayName("[성공] 기존 동의 제거")
    void changeConsent_removeExistingConsent_success() {
      // given
      String userId = "user-id-123";
      Consent existingConsent =
          TestFixture.createConsent("existing-id", TestFixture.CONSENT_ID_MARKETING);

      Auth auth = TestFixture.createAuth(userId);
      auth.setConsent(new ArrayList<>(List.of(existingConsent)));
      existingConsent.setUser(auth);

      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_MARKETING, false));

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      consentService.changeConsent(userId, requests);

      // then
      assertThat(auth.getConsent()).isEmpty();
      verify(authRepository).save(auth);
    }

    @Test
    @DisplayName("[성공] 이미 존재하는 동의는 중복 추가하지 않음")
    void changeConsent_duplicateConsentNotAdded() {
      // given
      String userId = "user-id-123";
      Consent existingConsent =
          Consent.builder()
              .id("existing-consent-id")
              .consentsTable(consentsAllMaps.get(TestFixture.CONSENT_ID_TERMS))
              .build();

      Auth auth =
          Auth.builder().id(userId).consent(new ArrayList<>(List.of(existingConsent))).build();
      existingConsent.setUser(auth);

      List<ConsentRequest> requests =
          List.of(
              ConsentRequest.builder()
                  .consentId(TestFixture.CONSENT_ID_TERMS)
                  .consented(true) // 이미 동의한 항목
                  .build());

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      consentService.changeConsent(userId, requests);

      // then
      assertThat(auth.getConsent()).hasSize(1); // 중복 추가 안 됨
      verify(keyProvider, never()).generateKey(); // 새 ID 생성 안 됨
    }

    @Test
    @DisplayName("[성공] 복합 시나리오: 추가, 제거, 유지")
    void changeConsent_complexScenario_success() {
      // given
      String userId = "user-id-123";
      Consent consent1 =
          Consent.builder()
              .id("consent-1")
              .consentsTable(consentsAllMaps.get(TestFixture.CONSENT_ID_TERMS))
              .build();
      Consent consent2 =
          Consent.builder()
              .id("consent-2")
              .consentsTable(consentsAllMaps.get(TestFixture.CONSENT_ID_PRIVACY))
              .build();

      Auth auth =
          Auth.builder().id(userId).consent(new ArrayList<>(List.of(consent1, consent2))).build();
      consent1.setUser(auth);
      consent2.setUser(auth);

      List<ConsentRequest> requests =
          List.of(
              ConsentRequest.builder()
                  .consentId(TestFixture.CONSENT_ID_PRIVACY)
                  .consented(false) // 제거
                  .build(),
              ConsentRequest.builder()
                  .consentId(TestFixture.CONSENT_ID_MARKETING)
                  .consented(true) // 추가
                  .build());

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
      when(keyProvider.generateKey()).thenReturn("new-consent-id");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      consentService.changeConsent(userId, requests);

      // then
      assertThat(auth.getConsent()).hasSize(2); // TERMS 유지 + MARKETING 추가
    }

    @Test
    @DisplayName("[실패] 사용자를 찾을 수 없음")
    void changeConsent_userNotFound_throwsException() {
      // given
      String userId = "non-existent-user";
      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true));

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> consentService.changeConsent(userId, requests))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorcode", AuthErrorCode.USER_NOT_FOUND);

      verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("[검증] Fetch Join으로 N+1 문제 방지")
    void changeConsent_usesfetchJoin() {
      // given
      String userId = "user-id-123";
      Auth auth = TestFixture.createAuth(userId);
      List<ConsentRequest> requests = new ArrayList<>();

      when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when
      consentService.changeConsent(userId, requests);

      // then
      verify(authRepository).findByIdWithConsent(userId); // Fetch Join 메서드 호출
      verify(authRepository, never()).findById(anyString()); // 일반 조회 안 함
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTests {

    @Test
    @DisplayName("[통합] 회원가입 시 동의 저장 후 변경")
    void scenario_saveOnSignupThenChange() {
      // given - 회원가입 시 동의 저장
      Auth auth = TestFixture.createAuth("user-id-123");

      List<ConsentRequest> initialRequests =
          List.of(
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true),
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_MARKETING, false));

      when(keyProvider.generateKey()).thenReturn("consent-1");

      // when - 회원가입 시 동의 저장
      consentService.saveConsent(auth, initialRequests);

      // then
      assertThat(auth.getConsent()).hasSize(1); // MARKETING은 false이므로 저장 안 됨

      // given - 동의 변경
      List<ConsentRequest> changeRequests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_MARKETING, true));

      when(authRepository.findByIdWithConsent("user-id-123")).thenReturn(Optional.of(auth));
      when(keyProvider.generateKey()).thenReturn("consent-2");
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when - 동의 변경
      consentService.changeConsent("user-id-123", changeRequests);

      // then
      assertThat(auth.getConsent()).hasSize(2);
    }

    @Test
    @DisplayName("[통합] 모든 동의 철회 후 재동의")
    void scenario_revokeAllThenReConsent() {
      // given - 초기 동의 상태
      Consent consent1 = TestFixture.createConsent("consent-1", TestFixture.CONSENT_ID_TERMS);
      Consent consent2 = TestFixture.createConsent("consent-2", TestFixture.CONSENT_ID_PRIVACY);

      Auth auth = TestFixture.createAuth("user-id-123");
      auth.setConsent(new ArrayList<>(List.of(consent1, consent2)));
      consent1.setUser(auth);
      consent2.setUser(auth);

      // given - 모든 동의 철회
      List<ConsentRequest> revokeRequests =
          List.of(
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, false),
              TestFixture.createConsentRequest(TestFixture.CONSENT_ID_PRIVACY, false));

      when(authRepository.findByIdWithConsent("user-id-123")).thenReturn(Optional.of(auth));
      when(authRepository.save(any(Auth.class))).thenReturn(auth);

      // when - 모든 동의 철회
      consentService.changeConsent("user-id-123", revokeRequests);

      // then
      assertThat(auth.getConsent()).isEmpty();

      // given - 재동의
      List<ConsentRequest> reConsentRequests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true));

      when(keyProvider.generateKey()).thenReturn("new-consent-id");

      // when - 재동의
      consentService.changeConsent("user-id-123", reConsentRequests);

      // then
      assertThat(auth.getConsent()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryTests {

    @Test
    @DisplayName("[경계] 다수의 동의 항목 처리")
    void boundary_manyConsents() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");

      List<ConsentRequest> requests = new ArrayList<>();
      for (int i = 0; i < 50; i++) {
        String consentId = "CONSENT_" + i;
        // 각 consentId에 대한 ConsentsTable 추가
        consentsAllMaps.put(
            consentId,
            ConsentsTable.builder()
                .id(consentId)
                .consentName("CONSENT_TYPE_" + i)
                .version("v1.0")
                .consentUrl("https://example.com/consent/" + i)
                .required(false)
                .build());
        requests.add(TestFixture.createConsentRequest(consentId, true));
      }

      when(keyProvider.generateKey()).thenAnswer(invocation -> "consent-id-" + System.nanoTime());

      // when
      consentService.saveConsent(auth, requests);

      // then
      assertThat(auth.getConsent()).hasSize(50);
    }

    @Test
    @DisplayName("[경계] 매우 긴 동의 ID")
    void boundary_veryLongConsentId() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");

      String longConsentId = "VERY_LONG_CONSENT_ID_" + "A".repeat(200);
      // 긴 consentId에 대한 ConsentsTable 추가
      consentsAllMaps.put(
          longConsentId,
          ConsentsTable.builder()
              .id(longConsentId)
              .consentName("LONG_CONSENT_TYPE")
              .version("v1.0")
              .consentUrl("https://example.com/long")
              .required(false)
              .build());

      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(longConsentId, true));

      when(keyProvider.generateKey()).thenReturn("consent-id");

      // when
      consentService.saveConsent(auth, requests);

      // then
      assertThat(auth.getConsent()).hasSize(1);
      assertThat(auth.getConsent().get(0).getConsentsTable().getId()).isEqualTo(longConsentId);
    }
  }

  @Nested
  @DisplayName("예외 상황 테스트")
  class ExceptionTests {

    @Test
    @DisplayName("[예외] null Auth 처리")
    void saveConsent_nullAuth_throwsException() {
      // given
      List<ConsentRequest> requests =
          List.of(TestFixture.createConsentRequest(TestFixture.CONSENT_ID_TERMS, true));

      // when & then
      assertThatThrownBy(() -> consentService.saveConsent(null, requests))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("[예외] null 요청 리스트 처리")
    void saveConsent_nullRequests_throwsException() {
      // given
      Auth auth = TestFixture.createAuth("user-id-123");

      // when & then
      assertThatThrownBy(() -> consentService.saveConsent(auth, null))
          .isInstanceOf(Exception.class);
    }
  }
}
