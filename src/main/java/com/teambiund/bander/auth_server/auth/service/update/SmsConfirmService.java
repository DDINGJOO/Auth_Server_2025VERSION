package com.teambiund.bander.auth_server.auth.service.update;

public interface SmsConfirmService {

  /**
   * SMS 인증 코드 발급 및 이벤트 발행
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   */
  void generateCode(String userId, String phoneNumber);

  /**
   * SMS 인증 코드 확인
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @param code 인증 코드
   * @return 인증 성공 여부
   */
  boolean confirmSms(String userId, String phoneNumber, String code);

  /**
   * SMS 인증 코드 재발신
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @return 재발신 성공 여부
   */
  boolean resendSms(String userId, String phoneNumber);

  /**
   * 인증 완료 후 전화번호 저장
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   */
  void savePhoneNumber(String userId, String phoneNumber);
}