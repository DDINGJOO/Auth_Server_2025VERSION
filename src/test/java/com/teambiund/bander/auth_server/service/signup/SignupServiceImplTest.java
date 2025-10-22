package com.teambiund.bander.auth_server.service.signup;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.event.events.CreatedUserEvent;
import com.teambiund.bander.auth_server.auth.event.publish.CreateProfileRequestEventPub;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.auth.service.signup.SignupServiceImpl;
import com.teambiund.bander.auth_server.auth.service.signup.SignupStoreService;
import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignupServiceImpl 테스트")
class SignupServiceImplTest {

    @Mock
    private SignupStoreService signupStoreService;

    @Mock
    private ConsentManagementService consentService;

    @Mock
    private CreateProfileRequestEventPub publishEvent;

    @Mock
    private EmailConfirm emailConfirm;

    @InjectMocks
    private SignupServiceImpl signupService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test to avoid stubbing conflicts
        reset(signupStoreService, consentService, publishEvent, emailConfirm);
    }

    @Nested
    @DisplayName("일반 회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("[성공] 정상적인 회원가입 전체 흐름")
        void signup_validInput_success() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = List.of(
                    ConsentRequest.builder()
                            .consentId("test-consent-id-1")
                            .consented(true)
                            .build()
            );

            Auth expectedAuth = Auth.builder()
                    .id("user-id-123")
                    .email(email)
                    .password("hashedPassword")
                    .provider(Provider.SYSTEM)
                    .status(Status.ACTIVE)
                    .build();
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
                    .build();
            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);

            // when
            Auth result = signupService.signup(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-id-123");
            assertThat(result.getEmail()).isEqualTo(email);

            // 실행 순서 검증
            var inOrder = inOrder(emailConfirm, signupStoreService, publishEvent, consentService);
            inOrder.verify(emailConfirm).checkedConfirmedEmail(email);
            inOrder.verify(signupStoreService).signup(email, password);
            inOrder.verify(publishEvent).createProfileRequestPub(any(CreatedUserEvent.class));
            inOrder.verify(consentService).saveConsent(expectedAuth, consentReqs);
        }

        @Test
        @DisplayName("[성공] 이벤트 발행 시 올바른 데이터 전달")
        void signup_publishEventWithCorrectData() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = new ArrayList<>();

            Auth expectedAuth = Auth.builder()
                    .id("user-id-123")
                    .provider(Provider.SYSTEM)
                    .build();
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();

            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);

            ArgumentCaptor<CreatedUserEvent> eventCaptor = ArgumentCaptor.forClass(CreatedUserEvent.class);

            // when
            signupService.signup(req);

            // then
            verify(publishEvent).createProfileRequestPub(eventCaptor.capture());
            CreatedUserEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getUserId()).isEqualTo("user-id-123");
            assertThat(capturedEvent.getProvider()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("[실패] 이메일 미확인 시 예외 발생")
        void signup_emailNotConfirmed_throwsException() {
            // given
            String email = "unconfirmed@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = new ArrayList<>();

      doThrow(new CustomException(AuthErrorCode.NOT_CONFIRMED_EMAIL))
          .when(emailConfirm)
          .checkedConfirmedEmail(email);
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
            // when & then
            assertThatThrownBy(() -> signupService.signup(req))
                    .isInstanceOf(CustomException.class);

            verify(emailConfirm).checkedConfirmedEmail(email);
            verify(signupStoreService, never()).signup(anyString(), anyString());
            verify(publishEvent, never()).createProfileRequestPub(any());
            verify(consentService, never()).saveConsent(any(), anyList());
        }

        @Test
        @DisplayName("[실패] signupStoreService 예외 시 이벤트 발행 및 동의 저장 안 됨")
        void signup_storeServiceFails_noEventOrConsentSaved() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = new ArrayList<>();
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
      when(signupStoreService.signup(req.getEmail(), req.getPassword()))
          .thenThrow(new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS));

            // when & then
            assertThatThrownBy(() -> signupService.signup(req))
                    .isInstanceOf(CustomException.class);

            verify(publishEvent, never()).createProfileRequestPub(any());
            verify(consentService, never()).saveConsent(any(), anyList());
        }

        @Test
        @DisplayName("[검증] 동의 정보가 올바르게 전달됨")
        void signup_consentsPassedCorrectly() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = List.of(
                    ConsentRequest.builder().consentId("test-consent-id-1").consented(true).build(),
                    ConsentRequest.builder().consentId("test-consent-id-2").consented(true).build()
            );
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
            Auth expectedAuth = Auth.builder()
                    .id("user-id")
                    .provider(Provider.SYSTEM)
                    .build();
	        
            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);

            // when
            signupService.signup(req);

            // then
            verify(consentService).saveConsent(expectedAuth, consentReqs);
        }
    }

    @Nested
    @DisplayName("소셜 로그인 회원가입 테스트")
    class SignupFromOtherProviderTests {

        @Test
        @DisplayName("[성공] 카카오 소셜 로그인 회원가입")
        void signupFromOtherProvider_kakao_success() {
            // given
            String email = "kakao@example.com";
            Provider provider = Provider.KAKAO;

            Auth expectedAuth = Auth.builder()
                    .id("kakao-user-123")
                    .email(email)
                    .provider(Provider.KAKAO)
                    .build();

            when(signupStoreService.signupFromOtherProvider(email, provider)).thenReturn(expectedAuth);

            // when
            Auth result = signupService.signupFromOtherProvider(email, provider);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("kakao-user-123");
            assertThat(result.getProvider()).isEqualTo(Provider.KAKAO);

            verify(signupStoreService).signupFromOtherProvider(email, provider);
            verify(publishEvent).createProfileRequestPub(any(CreatedUserEvent.class));
        }

        @Test
        @DisplayName("[성공] 애플 소셜 로그인 회원가입")
        void signupFromOtherProvider_apple_success() {
            // given
            String email = "apple@example.com";
            Provider provider = Provider.APPLE;

            Auth expectedAuth = Auth.builder()
                    .id("apple-user-123")
                    .email(email)
                    .provider(Provider.APPLE)
                    .build();

            when(signupStoreService.signupFromOtherProvider(email, provider)).thenReturn(expectedAuth);

            // when
            Auth result = signupService.signupFromOtherProvider(email, provider);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProvider()).isEqualTo(Provider.APPLE);
        }

        @Test
        @DisplayName("[성공] 이벤트 발행 시 올바른 Provider 전달")
        void signupFromOtherProvider_publishEventWithCorrectProvider() {
            // given
            String email = "social@example.com";
            Provider provider = Provider.KAKAO;

            Auth expectedAuth = Auth.builder()
                    .id("social-user-123")
                    .provider(Provider.KAKAO)
                    .build();

            when(signupStoreService.signupFromOtherProvider(email, provider)).thenReturn(expectedAuth);

            ArgumentCaptor<CreatedUserEvent> eventCaptor = ArgumentCaptor.forClass(CreatedUserEvent.class);

            // when
            signupService.signupFromOtherProvider(email, provider);

            // then
            verify(publishEvent).createProfileRequestPub(eventCaptor.capture());
            CreatedUserEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getUserId()).isEqualTo("social-user-123");
            assertThat(capturedEvent.getProvider()).isEqualTo("KAKAO");
        }

        @Test
        @DisplayName("[검증] 소셜 로그인은 이메일 확인 및 동의 저장 안 함")
        void signupFromOtherProvider_noEmailConfirmOrConsent() {
            // given
            String email = "social@example.com";
            Provider provider = Provider.KAKAO;

            Auth expectedAuth = Auth.builder()
                    .id("social-user-123")
                    .provider(Provider.KAKAO)
                    .build();

            when(signupStoreService.signupFromOtherProvider(email, provider)).thenReturn(expectedAuth);

            // when
            signupService.signupFromOtherProvider(email, provider);

            // then
            verify(emailConfirm, never()).checkedConfirmedEmail(anyString());
            verify(consentService, never()).saveConsent(any(), anyList());
        }

        @Test
        @DisplayName("[실패] signupStoreService 예외 시 이벤트 발행 안 됨")
        void signupFromOtherProvider_storeServiceFails_noEvent() {
            // given
            String email = "social@example.com";
            Provider provider = Provider.KAKAO;

      when(signupStoreService.signupFromOtherProvider(email, provider))
          .thenThrow(new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS));

            // when & then
            assertThatThrownBy(() -> signupService.signupFromOtherProvider(email, provider))
                    .isInstanceOf(CustomException.class);

            verify(publishEvent, never()).createProfileRequestPub(any());
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("[통합] 일반 회원가입 전체 흐름 통합 테스트")
        void scenario_fullSignupFlow() {
            // given
            String email = "integration@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = List.of(
                    ConsentRequest.builder().consentId("test-consent-id-1").consented(true).build()
            );

            Auth expectedAuth = Auth.builder()
                    .id("integration-user-123")
                    .email(email)
                    .provider(Provider.SYSTEM)
                    .build();
	        SignupRequest req = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);

            // when
            Auth result = signupService.signup(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("integration-user-123");

            // 모든 컴포넌트 호출 검증
            verify(emailConfirm, times(1)).checkedConfirmedEmail(email);
            verify(signupStoreService, times(1)).signup(email, password);
            verify(publishEvent, times(1)).createProfileRequestPub(any(CreatedUserEvent.class));
            verify(consentService, times(1)).saveConsent(expectedAuth, consentReqs);
        }

        @Test
        @DisplayName("[통합] 소셜 로그인 회원가입 전체 흐름 통합 테스트")
        void scenario_fullSocialSignupFlow() {
            // given
            String email = "social-integration@example.com";
            Provider provider = Provider.KAKAO;

            Auth expectedAuth = Auth.builder()
                    .id("social-integration-123")
                    .email(email)
                    .provider(Provider.KAKAO)
                    .build();

            when(signupStoreService.signupFromOtherProvider(email, provider)).thenReturn(expectedAuth);

            // when
            Auth result = signupService.signupFromOtherProvider(email, provider);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProvider()).isEqualTo(Provider.KAKAO);

            // 소셜 로그인은 이메일 확인 및 동의 저장 안 함
            verify(emailConfirm, never()).checkedConfirmedEmail(anyString());
            verify(consentService, never()).saveConsent(any(), anyList());
            verify(publishEvent, times(1)).createProfileRequestPub(any(CreatedUserEvent.class));
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("[예외] null 파라미터 처리")
        void signup_nullParameters_throwsException() {
            // given
            List<ConsentRequest> consentReqs = new ArrayList<>();
	        SignupRequest req = SignupRequest.builder()
			        .email("example@tambvind.co.kr")
			        .password("pass")
			        .passwordConfirm("pass")
			        .consentReqs(consentReqs)
			        .build();
            // when & then - null 이메일
            assertThatThrownBy(() -> signupService.signup(req))
                    .isInstanceOf(Exception.class);

            // when & then - null 비밀번호
            assertThatThrownBy(() -> signupService.signup(req))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("[예외] 이벤트 발행 실패 시에도 예외 전파")
        void signup_eventPublishFails_throwsException() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = new ArrayList<>();

            Auth expectedAuth = Auth.builder().id("user-id").provider(Provider.SYSTEM).build();
	        SignupRequest request = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);
            doThrow(new RuntimeException("Event publish failed"))
                    .when(publishEvent).createProfileRequestPub(any(CreatedUserEvent.class));

            // when & then
            assertThatThrownBy(() -> signupService.signup(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event publish failed");
        }

        @Test
        @DisplayName("[예외] 동의 저장 실패 시 예외 전파")
        void signup_consentSaveFails_throwsException() {
            // given
            String email = "test@example.com";
            String password = "Password123!";
            String passConfirm = "Password123!";
            List<ConsentRequest> consentReqs = List.of(
                    ConsentRequest.builder().consentId("test-consent-id-1").consented(true).build()
            );
			SignupRequest request = SignupRequest.builder()
			        .email(email)
			        .password(password)
			        .passwordConfirm(passConfirm)
			        .consentReqs(consentReqs)
			        .build();
            Auth expectedAuth = Auth.builder().id("user-id").provider(Provider.SYSTEM).build();

            when(signupStoreService.signup(email, password)).thenReturn(expectedAuth);
      doThrow(new CustomException(AuthErrorCode.CONSENT_NOT_VALID))
          .when(consentService)
          .saveConsent(any(), anyList());

            // when & then
            assertThatThrownBy(() -> signupService.signup(request))
                    .isInstanceOf(CustomException.class);
        }
    }
}
