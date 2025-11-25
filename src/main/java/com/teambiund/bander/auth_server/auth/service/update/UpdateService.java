package com.teambiund.bander.auth_server.auth.service.update;

import com.teambiund.bander.auth_server.auth.dto.request.HistoryRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateService {
  private final AuthRepository authRepository;
  private final HistoryService historyService;
  private final CipherStrategy passwordEncoder;
  private final CipherStrategy emailCipher;

  public UpdateService(
      AuthRepository authRepository,
      HistoryService historyService,
      @Qualifier("pbkdf2CipherStrategy") CipherStrategy passwordEncoder,
      @Qualifier("aesCipherStrategy") CipherStrategy emailCipher) {
    this.authRepository = authRepository;
    this.historyService = historyService;
    this.passwordEncoder = passwordEncoder;
    this.emailCipher = emailCipher;
  }

  public void updateEmail(String userId, String newEmail) throws CustomException {
    Auth auth =
        authRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    changeEmail(auth, newEmail);
    authRepository.save(auth);
    changeEmailHistory(auth);
  }

  public void changePassword(String email, String newPassword, String passConfirm)
      throws CustomException {
    String encryptedEmail = emailCipher.encrypt(email);
    Auth auth =
        authRepository
            .findByEmailWithHistory(encryptedEmail)
            .or(
                () ->
                    authRepository.findByEmailWithHistory(
                        email)) // Backward-compatibility for legacy plaintext rows
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

    changePassword(auth, newPassword);
  }

  private void changePassword(Auth auth, String newPassword) {
    auth.setPassword(passwordEncoder.encrypt(newPassword));
    authRepository.save(auth);
    historyService.createHistory(
        HistoryRequest.builder()
            .auth(auth)
            .afterValue(auth.getPassword())
            .beforeValue(null)
            .updatedColumn("password")
            .build());
  }

  private void changeEmailHistory(Auth auth) {
    historyService.createHistory(
        HistoryRequest.builder()
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
