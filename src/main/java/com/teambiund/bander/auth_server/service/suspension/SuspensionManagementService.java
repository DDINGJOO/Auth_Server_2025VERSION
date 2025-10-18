package com.teambiund.bander.auth_server.service.suspension;

import com.teambiund.bander.auth_server.exceptions.CustomException;

/**
 * 사용자 정지 관리 서비스 인터페이스
 * - 사용자 정지 해제
 * - 사용자 정지 처리
 */
public interface SuspensionManagementService {

    /**
     * 사용자 정지 해제
     * @param userId 사용자 ID
     * @throws CustomException 사용자를 찾을 수 없거나 정지 상태가 아님
     */
    void release(String userId) throws CustomException;

    /**
     * 사용자 정지 처리
     * @param userId 사용자 ID
     * @param suspendReason 정지 사유
     * @param suspenderUserId 정지 처리자 ID (관리자)
     * @param suspendDate 정지 기간 (일)
     * @throws CustomException 사용자를 찾을 수 없거나, 정지 처리자가 관리자가 아니거나, 이미 차단된 사용자
     */
    void suspend(String userId, String suspendReason, String suspenderUserId, Long suspendDate) throws CustomException;
}
