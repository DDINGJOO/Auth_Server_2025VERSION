package com.teambiund.bander.auth_server.service.consent;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.consent.impl.ConsentManagementServiceImpl;
import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private AuthRepository authRepository;

    @Mock
    private KeyProvider keyProvider;

    @InjectMocks
    private ConsentManagementServiceImpl consentService;

    @Nested
    @DisplayName("동의 저장 테스트")
    class SaveConsentTests {

        @Test
        @DisplayName("[성공] 동의 정보 저장")
        void saveConsent_validRequests_success() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .email("test@example.com")
                    .status(Status.ACTIVE)
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v1.0")
                            .consented(true)
                            .build(),
                    ConsentRequest.builder()
                            .consentName("PRIVACY_POLICY")
                            .version("v1.0")
                            .consented(true)
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("consent-id-1", "consent-id-2");

            // when
            consentService.saveConsent(auth, requests);

            // then
            assertThat(auth.getConsent()).hasSize(2);
            assertThat(auth.getConsent())
                    .extracting(Consent::getConsentType)
                    .containsExactlyInAnyOrder("TERMS_OF_SERVICE", "PRIVACY_POLICY");

            verify(keyProvider, times(2)).generateKey();
        }

        @Test
        @DisplayName("[성공] consented=false인 항목은 저장하지 않음")
        void saveConsent_filteredByConsented_success() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v1.0")
                            .consented(true)
                            .build(),
                    ConsentRequest.builder()
                            .consentName("MARKETING")
                            .version("v1.0")
                            .consented(false)  // 동의하지 않음
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("consent-id-1");

            // when
            consentService.saveConsent(auth, requests);

            // then
            assertThat(auth.getConsent()).hasSize(1);
            assertThat(auth.getConsent())
                    .extracting(Consent::getConsentType)
                    .containsExactly("TERMS_OF_SERVICE");

            verify(keyProvider, times(1)).generateKey();
        }

        @Test
        @DisplayName("[성공] 빈 리스트 처리")
        void saveConsent_emptyList_success() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

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
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS")
                            .version("v1.0")
                            .consented(true)
                            .build()
            );

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
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v1.0")
                            .consented(true)
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("consent-id-123");

            // when
            consentService.saveConsent(auth, requests);

            // then
            Consent savedConsent = auth.getConsent().get(0);
            assertThat(savedConsent.getId()).isEqualTo("consent-id-123");
            assertThat(savedConsent.getConsentType()).isEqualTo("TERMS_OF_SERVICE");
            assertThat(savedConsent.getVersion()).isEqualTo("v1.0");
            assertThat(savedConsent.getConsentUrl()).isEqualTo("v1.0");
            assertThat(savedConsent.getAgreementAt()).isNotNull();
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
            Consent existingConsent = Consent.builder()
                    .id("existing-consent-id")
                    .consentType("TERMS_OF_SERVICE")
                    .version("v1.0")
                    .build();

            Auth auth = Auth.builder()
                    .id(userId)
                    .consent(new ArrayList<>(List.of(existingConsent)))
                    .build();
            existingConsent.setUser(auth);

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("PRIVACY_POLICY")
                            .version("v1.0")
                            .consented(true)
                            .build()
            );

            when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
            when(keyProvider.generateKey()).thenReturn("new-consent-id");
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when
            consentService.changeConsent(userId, requests);

            // then
            assertThat(auth.getConsent()).hasSize(2);
            assertThat(auth.getConsent())
                    .extracting(Consent::getConsentType)
                    .containsExactlyInAnyOrder("TERMS_OF_SERVICE", "PRIVACY_POLICY");

            verify(authRepository).findByIdWithConsent(userId);
            verify(authRepository).save(auth);
        }

        @Test
        @DisplayName("[성공] 기존 동의 제거")
        void changeConsent_removeExistingConsent_success() {
            // given
            String userId = "user-id-123";
            Consent existingConsent = Consent.builder()
                    .id("existing-consent-id")
                    .consentType("MARKETING")
                    .version("v1.0")
                    .build();

            Auth auth = Auth.builder()
                    .id(userId)
                    .consent(new ArrayList<>(List.of(existingConsent)))
                    .build();
            existingConsent.setUser(auth);

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("MARKETING")
                            .version("v1.0")
                            .consented(false)  // 동의 철회
                            .build()
            );

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
            Consent existingConsent = Consent.builder()
                    .id("existing-consent-id")
                    .consentType("TERMS_OF_SERVICE")
                    .version("v1.0")
                    .build();

            Auth auth = Auth.builder()
                    .id(userId)
                    .consent(new ArrayList<>(List.of(existingConsent)))
                    .build();
            existingConsent.setUser(auth);

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v1.0")
                            .consented(true)  // 이미 동의한 항목
                            .build()
            );

            when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when
            consentService.changeConsent(userId, requests);

            // then
            assertThat(auth.getConsent()).hasSize(1);  // 중복 추가 안 됨
            verify(keyProvider, never()).generateKey();  // 새 ID 생성 안 됨
        }

        @Test
        @DisplayName("[성공] 복합 시나리오: 추가, 제거, 유지")
        void changeConsent_complexScenario_success() {
            // given
            String userId = "user-id-123";
            Consent consent1 = Consent.builder()
                    .id("consent-1")
                    .consentType("TERMS_OF_SERVICE")
                    .build();
            Consent consent2 = Consent.builder()
                    .id("consent-2")
                    .consentType("MARKETING")
                    .build();

            Auth auth = Auth.builder()
                    .id(userId)
                    .consent(new ArrayList<>(List.of(consent1, consent2)))
                    .build();
            consent1.setUser(auth);
            consent2.setUser(auth);

            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .consented(true)  // 유지
                            .build(),
                    ConsentRequest.builder()
                            .consentName("MARKETING")
                            .consented(false)  // 제거
                            .build(),
                    ConsentRequest.builder()
                            .consentName("PRIVACY_POLICY")
                            .version("v1.0")
                            .consented(true)  // 추가
                            .build()
            );

            when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
            when(keyProvider.generateKey()).thenReturn("new-consent-id");
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when
            consentService.changeConsent(userId, requests);

            // then
            assertThat(auth.getConsent()).hasSize(2);
            assertThat(auth.getConsent())
                    .extracting(Consent::getConsentType)
                    .containsExactlyInAnyOrder("TERMS_OF_SERVICE", "PRIVACY_POLICY");
        }

        @Test
        @DisplayName("[실패] 사용자를 찾을 수 없음")
        void changeConsent_userNotFound_throwsException() {
            // given
            String userId = "non-existent-user";
            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS")
                            .consented(true)
                            .build()
            );

            when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> consentService.changeConsent(userId, requests))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorcode", ErrorCode.USER_NOT_FOUND);

            verify(authRepository, never()).save(any());
        }

        @Test
        @DisplayName("[검증] Fetch Join으로 N+1 문제 방지")
        void changeConsent_usesfetchJoin() {
            // given
            String userId = "user-id-123";
            Auth auth = Auth.builder()
                    .id(userId)
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = new ArrayList<>();

            when(authRepository.findByIdWithConsent(userId)).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when
            consentService.changeConsent(userId, requests);

            // then
            verify(authRepository).findByIdWithConsent(userId);  // Fetch Join 메서드 호출
            verify(authRepository, never()).findById(anyString());  // 일반 조회 안 함
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("[통합] 회원가입 시 동의 저장 후 변경")
        void scenario_saveOnSignupThenChange() {
            // given - 회원가입 시 동의 저장
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> initialRequests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v1.0")
                            .consented(true)
                            .build(),
                    ConsentRequest.builder()
                            .consentName("MARKETING")
                            .version("v1.0")
                            .consented(false)
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("consent-1", "consent-2");

            // when - 회원가입 시 동의 저장
            consentService.saveConsent(auth, initialRequests);

            // then
            assertThat(auth.getConsent()).hasSize(1);  // MARKETING은 false이므로 저장 안 됨

            // given - 동의 변경
            List<ConsentRequest> changeRequests = List.of(
                    ConsentRequest.builder()
                            .consentName("MARKETING")
                            .version("v1.0")
                            .consented(true)  // 추가
                            .build()
            );

            when(authRepository.findByIdWithConsent("user-id-123")).thenReturn(Optional.of(auth));
            when(keyProvider.generateKey()).thenReturn("consent-2");
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when - 동의 변경
            consentService.changeConsent("user-id-123", changeRequests);

            // then
            assertThat(auth.getConsent()).hasSize(2);
            assertThat(auth.getConsent())
                    .extracting(Consent::getConsentType)
                    .containsExactlyInAnyOrder("TERMS_OF_SERVICE", "MARKETING");
        }

        @Test
        @DisplayName("[통합] 모든 동의 철회 후 재동의")
        void scenario_revokeAllThenReConsent() {
            // given - 초기 동의 상태
            Consent consent1 = Consent.builder()
                    .id("consent-1")
                    .consentType("TERMS_OF_SERVICE")
                    .build();
            Consent consent2 = Consent.builder()
                    .id("consent-2")
                    .consentType("PRIVACY_POLICY")
                    .build();

            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>(List.of(consent1, consent2)))
                    .build();
            consent1.setUser(auth);
            consent2.setUser(auth);

            // given - 모든 동의 철회
            List<ConsentRequest> revokeRequests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .consented(false)
                            .build(),
                    ConsentRequest.builder()
                            .consentName("PRIVACY_POLICY")
                            .consented(false)
                            .build()
            );

            when(authRepository.findByIdWithConsent("user-id-123")).thenReturn(Optional.of(auth));
            when(authRepository.save(any(Auth.class))).thenReturn(auth);

            // when - 모든 동의 철회
            consentService.changeConsent("user-id-123", revokeRequests);

            // then
            assertThat(auth.getConsent()).isEmpty();

            // given - 재동의
            List<ConsentRequest> reConsentRequests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS_OF_SERVICE")
                            .version("v2.0")
                            .consented(true)
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("new-consent-id");

            // when - 재동의
            consentService.changeConsent("user-id-123", reConsentRequests);

            // then
            assertThat(auth.getConsent()).hasSize(1);
            assertThat(auth.getConsent().get(0).getConsentType()).isEqualTo("TERMS_OF_SERVICE");
            assertThat(auth.getConsent().get(0).getVersion()).isEqualTo("v2.0");
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("[경계] 다수의 동의 항목 처리")
        void boundary_manyConsents() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            List<ConsentRequest> requests = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                requests.add(ConsentRequest.builder()
                        .consentName("CONSENT_" + i)
                        .version("v1.0")
                        .consented(true)
                        .build());
            }

            when(keyProvider.generateKey()).thenAnswer(invocation -> "consent-id-" + System.nanoTime());

            // when
            consentService.saveConsent(auth, requests);

            // then
            assertThat(auth.getConsent()).hasSize(50);
        }

        @Test
        @DisplayName("[경계] 매우 긴 동의 타입 이름")
        void boundary_veryLongConsentName() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            String longConsentName = "VERY_LONG_CONSENT_NAME_" + "A".repeat(200);
            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName(longConsentName)
                            .version("v1.0")
                            .consented(true)
                            .build()
            );

            when(keyProvider.generateKey()).thenReturn("consent-id");

            // when
            consentService.saveConsent(auth, requests);

            // then
            assertThat(auth.getConsent()).hasSize(1);
            assertThat(auth.getConsent().get(0).getConsentType()).isEqualTo(longConsentName);
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("[예외] null Auth 처리")
        void saveConsent_nullAuth_throwsException() {
            // given
            List<ConsentRequest> requests = List.of(
                    ConsentRequest.builder()
                            .consentName("TERMS")
                            .consented(true)
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> consentService.saveConsent(null, requests))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("[예외] null 요청 리스트 처리")
        void saveConsent_nullRequests_throwsException() {
            // given
            Auth auth = Auth.builder()
                    .id("user-id-123")
                    .consent(new ArrayList<>())
                    .build();

            // when & then
            assertThatThrownBy(() -> consentService.saveConsent(auth, null))
                    .isInstanceOf(Exception.class);
        }
    }
}
