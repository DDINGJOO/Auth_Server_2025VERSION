package com.teambiund.bander.auth_server.service.update;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 전화번호 업데이트 서비스 - 전화번호 유효성 검증 - AES-256 암호화를 통한 전화번호 저장 */
@Service
@Transactional
public class PhoneNumberUpdateService {

  private final AuthRepository authRepository;
  private final CipherStrategy phoneNumberCipher;

  public PhoneNumberUpdateService(
      AuthRepository authRepository,
      @Qualifier("aesCipherStrategy") CipherStrategy phoneNumberCipher) {
    this.authRepository = authRepository;
    this.phoneNumberCipher = phoneNumberCipher;
  }

  /**
   * 전화번호 업데이트
   *
   * @param req 전화번호 업데이트 요청 (userId, phoneNumber) - @PhoneNumber로 이미 검증됨
   * @throws CustomException 사용자를 찾을 수 없음
   */
  public void updatePhoneNumber(PhoneNumberUpdateRequest req) {
    // 사용자 조회
    Auth auth =
        authRepository
            .findById(req.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 전화번호 암호화 후 저장
    auth.setPhoneNumber(phoneNumberCipher.encrypt(req.getPhoneNumber()));
    authRepository.save(auth);
  }
}
