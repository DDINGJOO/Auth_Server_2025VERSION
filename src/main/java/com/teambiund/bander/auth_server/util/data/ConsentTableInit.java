package com.teambiund.bander.auth_server.util.data;


import com.teambiund.bander.auth_server.entity.consentsname.ConsentsTable;
import com.teambiund.bander.auth_server.repository.ConsentTableRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Consent 테이블 초기화 유틸리티.
 * - DB에 존재하는 consents_name 레코드를 모두 읽어와 static Map(consentMaps)에 저장한다.
 * - 같은 consentName이 여러 개 존재하면 version 필드(v1.1, v1.2 등)를 비교하여 최신 버전만 남긴다.
 * - version 형식은 "v1.1" 또는 "1.1" 형태를 가정하고, 접두사 'v'를 무시한 뒤 도트(.)로 구분된 숫자 파트를 좌측부터 비교한다.
 */
@Component
@RequiredArgsConstructor
public class ConsentTableInit {
    public static final HashMap<String, ConsentsTable> consentMaps = new HashMap<>();
    public static final List<ConsentsTable> requiredConsents = new ArrayList<>();
    public static final HashMap<String, ConsentsTable> consentsAllMaps = new HashMap<>();
	public static final List<String> requiredConsentTypes= new ArrayList<>();
    private final ConsentTableRepository consentTableRepository;

    @PostConstruct
    public void init() {
        List<ConsentsTable> consents = consentTableRepository.findAll();

        // 같은 이름을 가지고 있을땐 버전 정보가 나중인 consent만 채워야함
        for (ConsentsTable consent : consents) {
            consentsAllMaps.put(consent.getId(), consent);
            if (consent.isRequired()) {
                requiredConsents.add(consent);
            }
            String name = consent.getConsentName();
            if (name == null) {
                continue;
            }
            ConsentsTable existing = consentMaps.get(name);
            if (existing == null) {
                consentMaps.put(name, consent);
            } else {
        if (consent.isNewerVersion(existing.getVersion())
            || Objects.equals(existing.getVersion(),consent.getVersion())) {
                    consentMaps.put(name, consent);
                }
            }
        }
		
		for(ConsentsTable req : requiredConsents)
		{
			requiredConsentTypes.add(req.getConsentName());
		}
    }


}
