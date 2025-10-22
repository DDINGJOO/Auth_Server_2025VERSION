package com.teambiund.bander.auth_server.auth.service.consent;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import java.util.List;

/**
 * 동의 관리 서비스 인터페이스
 * - 회원가입 시 동의 정보 저장
 * - 동의 정보 변경
 */
public interface ConsentManagementService {

    /**
     * 회원가입 시 동의 정보 저장
     * @param auth 사용자 엔티티
     * @param requests 동의 요청 목록
     * @throws CustomException 유효하지 않은 동의 정보
     */
    void saveConsent(Auth auth, List<ConsentRequest> requests) throws CustomException;

    /**
     * 동의 정보 변경
     * @param userId 사용자 ID
     * @param req 변경할 동의 정보
     * @throws CustomException 사용자를 찾을 수 없거나 유효하지 않은 동의 정보
     */
    void changeConsent(String userId, List<ConsentRequest> req) throws CustomException;
	
	
	
}
