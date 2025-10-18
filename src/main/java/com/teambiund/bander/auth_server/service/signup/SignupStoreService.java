package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignupStoreService {
  private final AuthRepository authRepository;
  private final KeyProvider keyProvider;
  private final CipherStrategy passwordEncoder;
  private final CipherStrategy emailCipher;

  public SignupStoreService(
      AuthRepository authRepository,
      KeyProvider keyProvider,
      @Qualifier("pbkdf2CipherStrategy") CipherStrategy passwordEncoder,
      @Qualifier("aesCipherStrategy") CipherStrategy emailCipher) {
    this.authRepository = authRepository;
    this.keyProvider = keyProvider;
    this.passwordEncoder = passwordEncoder;
    this.emailCipher = emailCipher;
  }

  public Auth signup(String email, String password, String passConfirm) throws CustomException {
    String encryptedEmail = emailCipher.encrypt(email);
    if (authRepository.findByEmail(encryptedEmail).isPresent()
        || authRepository.findByEmail(email).isPresent()) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    Auth auth =
        Auth.builder()
            .id(keyProvider.generateKey())
            .email(encryptedEmail)
            .password(passwordEncoder.encrypt(password))
            .provider(Provider.SYSTEM)
            .createdAt(LocalDateTime.now())
            .status(Status.ACTIVE)
            .userRole(Role.USER)
            .build();

    authRepository.save(auth);
    return auth;
  }

  public Auth signupFromOtherProvider(String email, Provider provider) {
    String encryptedEmail = emailCipher.encrypt(email);
    if (authRepository.findByEmail(encryptedEmail).isPresent()
        || authRepository.findByEmail(email).isPresent()) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    Auth auth =
        Auth.builder()
            .id(keyProvider.generateKey())
            .email(encryptedEmail)
            .provider(provider)
            .createdAt(LocalDateTime.now())
            .status(Status.ACTIVE)
            .userRole(Role.USER)
            .build();
    authRepository.save(auth);
    return auth;
  }
}
