package com.teambiund.bander.auth_server.service.consent;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.ConsentRepository;
import com.teambiund.bander.auth_server.service.impl.SignupClientService;
import com.teambiund.bander.auth_server.service.signup.ConsentService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
public class ConsentServiceTest {
    @Autowired
    private ConsentService consentService;
    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private SignupClientService signupClientService;
    @Autowired
    private AuthRepository authRepository;


    @BeforeEach
    void setUp() {
        // 1) Auth를 생성하고 저장 후 반환된 managed 인스턴스를 사용
        Auth auth = Auth.builder()
                .id("test")   // 고유 id 사용 권장
                .password("test")
                .email("test@" + UUID.randomUUID() + ".com") // 테스트 격리 위해 이메일도 유니크 권장
                .status(Status.ACTIVE)
                .userRole(Role.USER)
                .build();
        auth = authRepository.saveAndFlush(auth); // DB에 반영된 managed 인스턴스

        // 2) Consent를 생성할 때는 위의 managed auth를 참조
        List<Consent> consents = new ArrayList<>();
        consents.add(Consent.builder()
                .id(UUID.randomUUID().toString())
                .consentUrl("www.url.comn")
                .consentName("PERSONAL_INFO")
                .user(auth)
                .agreementAt(LocalDateTime.now())
                .build());

        consents.add(Consent.builder()
                .id(UUID.randomUUID().toString())
                .consentUrl("www.url.com")
                .consentName("MARKETING")
                .user(auth)
                .agreementAt(LocalDateTime.now())
                .build());


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
        consentRequest.setConsentName("PERSONAL_INFO");
        consentRequest.setVersion("www.url.com");
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
        assertEquals("PERSONAL_INFO", consents.getFirst().getConsentName());

    }

    @Test
    @DisplayName("동의 수정 테스트 : 동의 했던 항목을 동의하지 않음 으로 변환(필수 동의X")
    void updateConsentChangeFalse() throws CustomException {

        //given
        List<ConsentRequest> reqCon2 = new ArrayList<>();
        ConsentRequest consentRequest2 = new ConsentRequest();
        consentRequest2.setConsentName("MARKETING");
        consentRequest2.setVersion("www.url.com");
        consentRequest2.setConsented(false);
        reqCon2.add(consentRequest2);


        //when
        consentService.changeConsent("test", reqCon2);

        //then
        Auth auth = authRepository.findById("test").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );


        List<Consent> consents = consentRepository.findByUserId(auth.getId());
        assertEquals(1, consents.size());
        assertEquals(consents.getFirst().getUser().getId(), auth.getId());
        assertEquals(consents.getFirst().getUser().getEmail(), auth.getEmail());
        assertEquals("PERSONAL_INFO", consents.getFirst().getConsentName());
        //마켓팅 삭제
        assertNotEquals("MARKETING", consents.getFirst().getConsentName());
    }

    @Test
    @DisplayName("동의 수정 테스트 : 동의하지 않은 항목을 동의함으로 변환 ")
    void updateConsentChangeTrue() throws CustomException {
        //given
        List<ConsentRequest> reqCon2 = new ArrayList<>();
        ConsentRequest consentRequest2 = new ConsentRequest();
        consentRequest2.setConsentName("MARKETING");
        consentRequest2.setVersion("www.url.com");
        consentRequest2.setConsented(true);
        reqCon2.add(consentRequest2);

        //when
        consentService.changeConsent("test", reqCon2);

        //then
        Auth auth = authRepository.findById("test").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        List<Consent> consents = consentRepository.findByUserId(auth.getId());
        assertEquals(2, consents.size());
        assertEquals(consents.getFirst().getUser().getId(), auth.getId());
        assertEquals(consents.getFirst().getUser().getEmail(), auth.getEmail());
        assertEquals("PERSONAL_INFO", consents.getFirst().getConsentName());
        assertEquals("MARKETING", consents.get(1).getConsentName());
    }


    @Test
    @DisplayName("동의 수정 테스트 : 필수 동의 항목을 동의하지 않음으로 변환")
    @Transactional
    void updateConsentChangeTrue_case2() {
        List<ConsentRequest> reqCon2 = new ArrayList<>();
        ConsentRequest consentRequest2 = new ConsentRequest();
        consentRequest2.setConsentName("PERSONAL_INFO");
        consentRequest2.setVersion("www.url.com");
        consentRequest2.setConsented(false);
        reqCon2.add(consentRequest2);

        //when
        consentService.changeConsent("test", reqCon2);

        List<Consent> consents = consentRepository.findByUserId("test");
        List<String> strings = consents.stream().map(Consent::getConsentName).toList();
        assertFalse(strings.contains("PERSONAL_INFO"));
    }


    @Test
    @DisplayName("회원 기록 3년 이후 회원 기록이 삭제 되었을때 같이 삭제 ")
    void deleteUserRecord() {

        authRepository.deleteById("test");
        List<Consent> consents = consentRepository.findByUserId("test");
        assertEquals(0, consents.size());
    }
}
