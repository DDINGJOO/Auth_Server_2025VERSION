package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Consent;
import com.teambiund.bander.auth_server.enums.ConsentType;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.ConsentRepository;
import com.teambiund.bander.auth_server.util.generator.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsentService {
    private final AuthRepository authRepository;
    private final ConsentRepository consentRepository;
    private final KeyProvider keyProvider;
    private final Validator validator;



    public void saveConsent(Auth auth, List<ConsentRequest> requests) throws CustomException {
        validator.validateConsentList(requests);
        List<ConsentRequest> consents = requests.stream().filter(
                ConsentRequest::isConsented).toList();


        List<Consent> consentList = new ArrayList<>();
        for (ConsentRequest request : consents) {
            consentList.add(Consent.builder()
                    .id(keyProvider.generateKey())
                    .agreementAt(LocalDateTime.now())
                    .consentType(request.getConsent())
                    .user(auth)
                    .consentUrl(request.getVersion())
                    .build());
        }
        consentRepository.saveAll(consentList);
    }



    public void changeConsent(String userId, List<ConsentRequest> req) throws CustomException {
        validator.validateConsentList(req);

        // 사용자 엔티티 조회 (연관된 Consent 컬렉션을 강제로 로드할 필요 없이 엔티티만 확보)
        Auth auth = authRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        // DB에서 해당 사용자의 consent만 조회
        List<Consent> consents = consentRepository.findByUserId(userId);

        // ConsentType -> Consent 맵 생성 (기존 동의 조회용)
        var authConsentMap = consents.stream()
                .collect(Collectors.toMap(Consent::getConsentType, c -> c));

        // 요청을 순회하며 추가/삭제할 Consent 결정
        List<Consent> pendingAdd = new ArrayList<>();
        List<Consent> pendingDelete = new ArrayList<>();

        for (ConsentRequest r : req) {
            ConsentType type = r.getConsent();
            boolean consented = r.isConsented();

            if (consented) {
                // consented = true 이면, 이미 존재하면 아무 작업도 하지 않음
                if (!authConsentMap.containsKey(type)) {
                    Consent newConsent = Consent.builder()
                            .id(keyProvider.generateKey())
                            .consentUrl(r.getVersion())
                            .consentType(type)
                            .agreementAt(LocalDateTime.now())
                            .user(auth)
                            .build();
                    pendingAdd.add(newConsent);
                    // 맵에 반영하여 동일 항목 중복 추가 방지
                    authConsentMap.put(type, newConsent);
                }
            } else {
                // consented = false 이면, 기존에 동의가 있어도 삭제
                if (authConsentMap.containsKey(type)) {
                    pendingDelete.add(authConsentMap.get(type));
                    authConsentMap.remove(type);
                }
            }
        }

        // DB 반영: 삭제 먼저 처리
        if (!pendingDelete.isEmpty()) {
            // auth의 컬렉션에서 제거 (연관관계 정리)
            consentRepository.deleteAll(pendingDelete);
            consentRepository.flush();
        }

        // 추가 처리
        if (!pendingAdd.isEmpty()) {
            consentRepository.saveAll(pendingAdd);
            consentRepository.flush();
        }

    }

}
