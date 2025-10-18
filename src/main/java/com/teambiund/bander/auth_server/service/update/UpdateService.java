package com.teambiund.bander.auth_server.service.update;

import com.teambiund.bander.auth_server.dto.request.HistoryRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateService
{
    private final AuthRepository authRepository;
    private final HistoryService historyService;
    private final CipherStrategy passwordEncoder;
    private final CipherStrategy emailCipher;

    public UpdateService(
            AuthRepository authRepository,
            HistoryService historyService,
            @Qualifier("pbkdf2CipherStrategy") CipherStrategy passwordEncoder,
            @Qualifier("aesCipherStrategy") CipherStrategy emailCipher
    ) {
        this.authRepository = authRepository;
        this.historyService = historyService;
        this.passwordEncoder = passwordEncoder;
        this.emailCipher = emailCipher;
    }

    // 2025-09-21 기존 로직 백업 용
    public void EmailConfirm(String userId) throws CustomException {
        Auth auth = authRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        auth.setStatus(Status.ACTIVE);
        auth.setUserRole(Role.USER);
        authRepository.save(auth);
    }

    public void updateEmail(String userId, String newEmail) throws CustomException {
        Auth auth = authRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        changeEmail(auth, newEmail);
        authRepository.save(auth);
        changeEmailHistory(auth);
    }

    public void changePassword(String email, String newPassword, String passConfirm) throws CustomException {
        String encryptedEmail = emailCipher.encrypt(email);
        Auth auth = authRepository.findByEmailWithHistory(encryptedEmail)
                .or(() -> authRepository.findByEmailWithHistory(email)) // Backward-compatibility for legacy plaintext rows
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        changePassword(auth, newPassword);
    }

    private void changePassword(Auth auth, String newPassword) {
        auth.setPassword(passwordEncoder.encrypt(newPassword));
        authRepository.save(auth);
        historyService.createHistory(HistoryRequest.builder()
                .auth(auth)
                .afterValue(auth.getPassword())
                .beforeValue(null)
                .updatedColumn("password")
                .build());
    }

    private void changeEmailHistory(Auth auth) {
        historyService.createHistory(HistoryRequest.builder()
                .auth(auth)
                .afterValue(auth.getEmail())
                .beforeValue(null)
                .updatedColumn("email")
                .build());
    }

    private Auth changeEmail(Auth auth, String newEmail) {
        String encryptedEmail = emailCipher.encrypt(newEmail);
        auth.setEmail(encryptedEmail);
        auth.setStatus(Status.UNCONFIRMED);
        return auth;
    }
}
