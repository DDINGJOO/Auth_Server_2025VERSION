package com.teambiund.bander.auth_server.service.consent;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.enums.ConsentType;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.ConsentRepository;
import com.teambiund.bander.auth_server.service.impl.SignupClientService;
import com.teambiund.bander.auth_server.service.signup.ConsentService;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
public class ConsentServiceTest {
    @Autowired
    private ConsentService consentService;
    @Autowired
    private ConsentRepository consentRepository;
    @Autowired
    private SignupService signupService;
    @Autowired
    private SignupClientService signupClientService;
    @Autowired
    private AuthRepository authRepository;


    @BeforeEach
    void setUp() {
        Auth auth = authRepository.save(
                Auth.builder()
                        .id("test")
                        .password("test")
                        .email("test@test.com")
                        .status(Status.ACTIVE)
                        .userRole(Role.USER)
                        .build()
        );

        List<Consent> consents = List.of(
                Consent.builder()
                        .id("1")
                        .consentUrl("www.url.comn")
                        .consentType(ConsentType.PERSONAL_INFO)
                        .version("1.0")
                        .agreementAt(LocalDateTime.now())
                        .build()
        );


        authRepository.save(auth);
        authRepository.flush();
        consentRepository.saveAll(consents);
        consentRepository.flush();
    }

    @AfterEach
    void tearDown() {
        consentRepository.deleteAll();
        authRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입시 동의항목이 정상적으로 저장이 된다. ")
    @Transactional
    void saveConsent() throws CustomException {

        //given
        List<ConsentRequest> reqCon = new ArrayList<>();
        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setConsent(ConsentType.PERSONAL_INFO);
        consentRequest.setConsentUrl("www.url.com");
        consentRequest.setConsented(true);
        reqCon.add(consentRequest);

        SignupRequest req = new SignupRequest();
        req.setPasswordConfirm("hoss1001");
        req.setPassword("hoss1001");
        req.setEmail("test1@test.com");
        req.setConsentReqs(reqCon);


        //when
        signupClientService.signup(req);

        //then

        Auth auth = authRepository.findByEmailWithConsent("test1@test.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        List<Consent> consents = consentRepository.findByUserId(auth.getId());


        assertEquals(1, consents.size());
        assertEquals(consents.getFirst().getUser().getId(), auth.getId());
        assertEquals(consents.getFirst().getUser().getEmail(), auth.getEmail());


    }

    @Test
    @DisplayName("동의 수정 테스트 : 동의 했던 항목을 동의하지 않음 으로 변환(필수 동의X")
    void updateConsentChangeFalse() {
    }

    @Test
    @DisplayName("동의 수정 테스트 : 동의하지 않은 항목을 동의함으로 변환 ")
    void updateConsentChangeTrue() {
    }


    @Test
    @DisplayName("동의 수정 테스트 : 필수 동의 항목을 동의하지 않음으로 변환")
    void updateConsentChangeTrue_case2() {
    }


    @Test
    @DisplayName("회원 기록 3년 이후 회원 기록이 삭제 되었을때 같이 삭제 ")
    void deleteUserRecord() {
    }
}
