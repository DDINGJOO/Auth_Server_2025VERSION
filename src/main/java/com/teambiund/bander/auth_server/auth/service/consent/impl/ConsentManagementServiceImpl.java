package com.teambiund.bander.auth_server.auth.service.consent.impl;

import static com.teambiund.bander.auth_server.auth.util.data.ConsentTableInit.consentsAllMaps;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.Consent;
import com.teambiund.bander.auth_server.auth.entity.consentsname.ConsentsTable;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsentManagementServiceImpl implements ConsentManagementService {
    private final AuthRepository authRepository;
    private final KeyProvider keyProvider;

    /**
     * 회원가입 시 동의 정보 저장
     * - consentId로 ConsentsTable 조회
     * - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 설정
     * - Cascade 설정으로 Consent 엔티티 자동 저장
     */
    public void saveConsent(Auth auth, List<ConsentRequest> requests) throws CustomException {
        List<ConsentRequest> consents = requests.stream()
                .filter(ConsentRequest::isConsented)
                .toList();

        for (ConsentRequest request : consents) {
            // consentId로 ConsentsTable 조회
            ConsentsTable consentTable = consentsAllMaps.get(request.getConsentId());
            if (consentTable == null) {
        throw new CustomException(AuthErrorCode.CONSENT_NOT_VALID);
            }
            // ID와 시간은 Service에서 생성하여 주입
            auth.addConsentWithTable(
                    keyProvider.generateKey(),
                    consentTable,
                    LocalDateTime.now()
            );
        }

        // CascadeType.ALL로 인해 auth의 consent 컬렉션도 자동 저장됨
        // 하지만 명시적으로 save를 호출하는 것이 더 명확함
         authRepository.save(auth);
    }


    /**
     * 동의 정보 변경
     * - consentId로 ConsentsTable 조회
     * - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 관리
     * - orphanRemoval=true 설정으로 삭제된 Consent 자동 제거
     * - Fetch Join을 사용하여 N+1 문제 방지
     */
    public void changeConsent(String userId, List<ConsentRequest> req) throws CustomException {

    // Fetch Join으로 사용자와 동의 정보를 한 번에 조회 (N+1 문제 방지)
    Auth auth =
        authRepository
            .findByIdWithConsent(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

		List<ConsentsTable> exsistConsentsTableList = auth.getConsent().stream()
		        .map(Consent::getConsentsTable)
				.toList();
		
		for(ConsentRequest request : req) {
			ConsentsTable consentTable = consentsAllMaps.get(request.getConsentId());
			if(consentTable == null) {
        throw new CustomException(AuthErrorCode.CONSENT_NOT_VALID);
			}

			if(!request.isConsented()) {
				// 동의 철회
				auth.removeConsentByTable(consentTable);
			}
			else
			{
				if(!auth.hasConsentForTable(consentTable.getId())) {
					// 새로운 동의 추가
					auth.addConsentWithTable(
							keyProvider.generateKey(),
							consentTable,
							LocalDateTime.now()
					);
				}
				else
				{
					// 기존 동의 버전 체크
					for(ConsentsTable exsistConsentsTable : exsistConsentsTableList) {
						if(exsistConsentsTable.getConsentName().equals(consentTable.getConsentName()))
						{
							if(consentTable.isNewerVersion(exsistConsentsTable.getVersion()))
							{
								// 버전 업그레이드: 기존 제거 후 새 버전 추가
								auth.removeConsentByTable(exsistConsentsTable);
								auth.addConsentWithTable(
										keyProvider.generateKey(),
										consentTable,
										LocalDateTime.now()
								);
							}
						}
					}
				}
			}
		}
		
        // orphanRemoval=true로 인해 컬렉션에서 제거된 Consent는 자동 삭제됨
        // CascadeType.ALL로 인해 새로 추가된 Consent는 자동 저장됨
        authRepository.save(auth);
    }
}
