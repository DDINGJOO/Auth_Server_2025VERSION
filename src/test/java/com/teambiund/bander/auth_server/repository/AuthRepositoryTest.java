package com.teambiund.bander.auth_server.repository;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.entity.History;
import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
@RequiredArgsConstructor
@Log4j2
public class AuthRepositoryTest {
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private SuspendRepository suspendRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ConsentRepository consentRepository;


    @BeforeEach
    void setUp() {
        Auth user = (
                Auth.builder()
                        .id("test")
                        .status(Status.ACTIVE)
                        .userRole(Role.USER)
                        .email("test@example.com")
                        .password("password")
                        .createdAt(LocalDateTime.now())
                        .deletedAt(LocalDateTime.now().minusYears(4))
                        .build()
        );

        List<Consent> consents = new ArrayList<>();
        consents.add(Consent.builder()
                .id("consent1")
                .consentUrl("www.url.com")
                .agreementAt(LocalDateTime.now())
                .consentType(String.PERSONAL_INFO)
                .user(user)
                .build());
        user.setConsent(consents);

        List<History> histories = new ArrayList<>();
        histories.add(History.builder()
                .id("history1")
                .updatedAt(LocalDateTime.now())
                .updatedColumn("password")
                .afterColumnValue("newPassword")
                .user(user)
                .build());
        user.setHistory(histories);

        Suspend suspend = Suspend.builder()
                .id("suspend1")
                .suspendUntil(LocalDate.now().plusDays(1))
                .reason("dsa")
                .suspendedUserId(user.getId())
                .suspenderUserId("suspenderId")
                .build();


        suspendRepository.save(suspend);
        authRepository.save(user);

    }

    @AfterEach
    void tearDown() {
        suspendRepository.deleteAll();
        authRepository.deleteAll();
        historyRepository.deleteAll();
        consentRepository.deleteAll();
    }

    @Test
    @DisplayName("커스텀 쿼리문 테스트(1) : findByEmailWithHistory")
    void findByEmailWithHistory() throws CustomException {
        Auth auth = authRepository.findByEmailWithHistory("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        assertTrue(!auth.getHistory().isEmpty());

    }

    @Test
    @DisplayName("커스텀 쿼리문 테스트(2) : findByEmailWithConsent")
    void findByEmailWithConsent() throws CustomException {
        Auth auth = authRepository.findByEmailWithConsent("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        assertTrue(!auth.getConsent().isEmpty());

    }

    @Test
    @DisplayName("커스텀 쿼리문 테스트(3) :findAllBySuspendUntilIsBefore")
    void findAllBySuspendUntilIsBefore() {
        List<Suspend> suspends = suspendRepository.findAllBySuspendUntilIsBefore(LocalDate.now().plusDays(3L));
        assertTrue(!suspends.isEmpty());
    }

    @Test
    @DisplayName("커스텀 쿼리문 테스트(4) :deleteByDeletedAtAfter")
    @Transactional
    void deleteByDeletedAtAfter() {
        authRepository.deleteByDeletedAtBefore(LocalDateTime.now().plusYears(3));
        assertTrue(authRepository.findAll().isEmpty());

    }

    @Test
    @DisplayName("커스텀 쿼리문 테스트(5) : findByEmail")
    void findByEmail() throws CustomException {
        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        assertTrue(auth.getEmail().equals("test@example.com"));

    }

    @Test
    @DisplayName("커그텀 쿼리문 테스트 : findByUserId")
    void findByUserId() {
        List<Consent> consents = consentRepository.findByUserId("test");
        assertTrue(!consents.isEmpty());

    }
}
