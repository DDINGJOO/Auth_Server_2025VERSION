package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;

import com.teambiund.bander.auth_server.AuthServerApplication;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(
        classes = AuthServerApplication.class,
        properties = {
                // 프로파일이 dev로 지정되어 있으면 AuthServerApplication의 @Profile("dev")와 맞춤
                "spring.profiles.active=dev",
                // 테스트용 인메모리 H2 설정 (MySQL 모드로 동작하게 하여 일부 SQL 호환성 확보)
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                // 스키마 자동 초기화 동작 방식: 필요시 always로 schema.sql 실행, 호환성 문제 발생하면 never로 변경
                "spring.sql.init.mode=always"
        }
)

class SignupServiceTest {


    @Autowired
    private SignupService signupService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private BCryptUtil encoder;

    @AfterEach
    void tearDown() {
        authRepository.deleteAll();
    }

    @Test
    @DisplayName("SignupTest : 회원가입 확인 테스트")
    void signup() throws CustomException {
        SignupRequest req = new SignupRequest();
        req.setEmail("test@test.com");
        req.setPassword("hoss1001!");
        req.setPasswordConfirm("hoss1001!");


        signupService.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm());
        Auth  auth = authRepository.findByEmail(req.getEmail()).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        assertEquals(req.getEmail(), auth.getEmail());
        assertTrue(encoder.matches(req.getPassword(), auth.getPassword()));
        assertEquals(Status.UNCONFIRMED, auth.getStatus());
        assertEquals(0, auth.getVersion());
    }






}
