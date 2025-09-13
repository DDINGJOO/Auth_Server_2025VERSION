package com.teambiund.bander.auth_server.service.Auth;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.History;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.HistoryRepository;
import com.teambiund.bander.auth_server.service.update.PhoneNumberUpdateService;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
@RequiredArgsConstructor
@Log4j2
class UpdateServiceTest {
    @Autowired
    private UpdateService updateService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private KeyProvider keyProvider;
    @Autowired
    private PhoneNumberUpdateService phoneNumberUpdateService;

    @Autowired
    private HistoryRepository historyRepository;

    private final PasswordEncoder encoder = new BCryptUtil();

    @BeforeEach
    void setUp() {
        authRepository.save(
                Auth.builder()
                        .id(keyProvider.generateKey())
                        .email("test@example.com")
                        .password(encoder.encode("password"))
                        .status(Status.UNCONFIRMED)
                        .version(0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        historyRepository.deleteAll();
        authRepository.deleteAll();
    }


    @Test
    @DisplayName("PasswordChangeService : 비밀번호 변경 서비스 (비정상 비밀번호)")
    void passwordChange() throws CustomException {
        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String newPassword = "hos";
        assertThrows(CustomException.class, () -> updateService.changePassword(auth.getEmail(),
                newPassword, newPassword));

    }

    @Test
    @DisplayName("PasswordChangeService : 비밀번호 변경 서비스 (정상 비밀번호 ")
    void passwordChange_case1() throws CustomException {

        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String newPassword = "hoss1001";


        updateService.changePassword(auth.getEmail(), newPassword, newPassword);


        auth = authRepository.findByEmailWithHistory(auth.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        List<History> histories = auth.getHistory();
        assertEquals(1, histories.size());
        assertEquals(histories.getFirst().getAfterColumnValue(), auth.getPassword());
        assertTrue(encoder.matches(newPassword, auth.getPassword()));
    }

    @Test
    @DisplayName("Phone Number Update Service : 핸드폰 번호 변경 서비스 테스트 (비정상 핸드폰 번호 )")
    void phoneNumberChange_case1() throws CustomException {
        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String phoneNumber = "12345678901";
        PhoneNumberUpdateRequest req = new PhoneNumberUpdateRequest(auth.getId(), phoneNumber);
        assertThrows(CustomException.class, () -> phoneNumberUpdateService.updatePhoneNumber(req));
    }

    @Test
    @DisplayName("Phone Number Update Service : 핸드폰 번호 변경 서비스 테스트 (정상 핸드폰 번호 )")
    void phoneNumberChange_case2() throws CustomException {
        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String phoneNumber = "01082089961";
        PhoneNumberUpdateRequest req = new PhoneNumberUpdateRequest(auth.getId(), phoneNumber);
        phoneNumberUpdateService.updatePhoneNumber(req);
    }

}
