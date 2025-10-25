package com.teambiund.bander.auth_server.auth.service.signup;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.LoginStatus;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
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

  public Auth signup(String email, String password) throws CustomException {
    String encryptedEmail = emailCipher.encrypt(email);
    if (authRepository.findByEmail(encryptedEmail).isPresent()
        || authRepository.findByEmail(email).isPresent()) {
      throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
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

    // LoginStatus 생성 및 양방향 관계 설정
    LoginStatus loginStatus = LoginStatus.builder().build();
    auth.setLoginStatus(loginStatus);

    authRepository.save(auth);
    return auth;
  }

  public Auth signupFromOtherProvider(String email, Provider provider) {
    String encryptedEmail = emailCipher.encrypt(email);
    if (authRepository.findByEmail(encryptedEmail).isPresent()
        || authRepository.findByEmail(email).isPresent()) {
      throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
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

    // LoginStatus 생성 및 양방향 관계 설정
    LoginStatus loginStatus = LoginStatus.builder().build();
    auth.setLoginStatus(loginStatus);

    authRepository.save(auth);
    return auth;
  }
}
