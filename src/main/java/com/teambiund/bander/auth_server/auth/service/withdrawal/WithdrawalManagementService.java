package com.teambiund.bander.auth_server.auth.service.withdrawal;

import com.teambiund.bander.auth_server.auth.exception.CustomException;

/** 회원 탈퇴 관리 서비스 인터페이스 - 회원 탈퇴 처리 - 회원 탈퇴 철회 */
public interface WithdrawalManagementService {

  /**
   * 회원 탈퇴 처리
   *
   * @param userId 사용자 ID
   * @param withdrawReason 탈퇴 사유
   * @throws CustomException 사용자를 찾을 수 없음
   */
  void withdraw(String userId, String withdrawReason) throws CustomException;

  /**
   * 회원 탈퇴 철회
   *
   * @param email 사용자 이메일
   * @throws CustomException 사용자를 찾을 수 없거나 탈퇴 정보가 없음
   */
  void withdrawRetraction(String email) throws CustomException;
}
