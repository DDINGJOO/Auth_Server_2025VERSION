package com.teambiund.bander.auth_server.service.consent.impl;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsentManagementServiceImpl implements ConsentManagementService {
    private final AuthRepository authRepository;
    private final KeyProvider keyProvider;

    /**
     * 회원가입 시 동의 정보 저장
     * - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 설정
     * - Cascade 설정으로 Consent 엔티티 자동 저장
     */
    public void saveConsent(Auth auth, List<ConsentRequest> requests) throws CustomException {
        List<ConsentRequest> consents = requests.stream()
                .filter(ConsentRequest::isConsented)
                .toList();

        for (ConsentRequest request : consents) {
            Consent consent = Consent.builder()
                    .id(keyProvider.generateKey())
                    .agreementAt(LocalDateTime.now())
                    .version(request.getVersion())
                    .consentType(request.getConsentName())
                    .consentUrl(request.getVersion())
                    .build();

            // 편의 메서드 사용 - 양방향 연관관계 설정
            auth.addConsent(consent);
        }

        // CascadeType.ALL로 인해 auth의 consent 컬렉션도 자동 저장됨
        // 하지만 명시적으로 save를 호출하는 것이 더 명확함
        // authRepository.save(auth); // 이미 영속 상태라면 불필요
    }


    /**
     * 동의 정보 변경
     * - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 관리
     * - orphanRemoval=true 설정으로 삭제된 Consent 자동 제거
     * - Fetch Join을 사용하여 N+1 문제 방지
     */
    public void changeConsent(String userId, List<ConsentRequest> req) throws CustomException {

        // Fetch Join으로 사용자와 동의 정보를 한 번에 조회 (N+1 문제 방지)
        Auth auth = authRepository.findByIdWithConsent(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        // ConsentType -> Consent 맵 생성 (기존 동의 조회용)
        // auth.getConsent()는 이미 fetch join으로 로드된 상태
        Map<String, Consent> authConsentMap = auth.getConsent().stream()
                .collect(Collectors.toMap(Consent::getConsentType, c -> c));

        for (ConsentRequest r : req) {
            String type = r.getConsentName();
            boolean consented = r.isConsented();

            if (consented) {
                // consented = true 이면, 이미 존재하지 않는 경우에만 추가
                if (!authConsentMap.containsKey(type)) {
                    Consent newConsent = Consent.builder()
                            .id(keyProvider.generateKey())
                            .version(r.getVersion())
                            .consentUrl(r.getVersion())
                            .consentType(type)
                            .agreementAt(LocalDateTime.now())
                            .build();

                    // 편의 메서드 사용 - 양방향 연관관계 설정
                    auth.addConsent(newConsent);
                    authConsentMap.put(type, newConsent);
                }
            } else {
                // consented = false 이면, 기존 동의 삭제
                if (authConsentMap.containsKey(type)) {
                    Consent toRemove = authConsentMap.get(type);

                    // 편의 메서드 사용 - 양방향 연관관계 제거
                    auth.removeConsent(toRemove);
                    authConsentMap.remove(type);
                }
            }
        }

        // orphanRemoval=true로 인해 컬렉션에서 제거된 Consent는 자동 삭제됨
        // CascadeType.ALL로 인해 새로 추가된 Consent는 자동 저장됨
        authRepository.save(auth);
    }

}
