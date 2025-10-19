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
public class ConsentTableUtils {
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
                String existingVersion = existing.getVersion();
                String newVersion = consent.getVersion();
                if (isNewerVersion(newVersion, existingVersion)) {
                    consentMaps.put(name, consent);
                }
            }
        }
		
		for(ConsentsTable req : requiredConsents)
		{
			requiredConsentTypes.add(req.getConsentName());
		}
    }
	
	private boolean checkAllRequiredConsents(List<ConsentsTable> requiredConsents)
	{
		for(ConsentsTable req : requiredConsents)
		{
			if(!consentMaps.containsKey(req.getConsentName()))
				return false;
		}
		return true;
	}

    /**
     * 주어진 두 버전 문자열을 비교하여 v1이 v2보다 최신인지 판단한다.
     * - "v" 또는 "V" 접두사는 무시한다.
     * - "v1.2" 형태를 "1.2"로 바꿔 각 숫자 파트를 비교한다.
     * - 숫자로 파싱할 수 없는 파트는 0으로 간주한다.
     *
     * @param v1 비교할 새로운 버전 (예: "v1.2")
     * @param v2 기존 버전 (예: "v1.1")
     * @return v1이 v2보다 최신이면 true, 같거나 이전이면 false
     */
    private boolean isNewerVersion(String v1, String v2) {
        if (Objects.equals(v1, v2)) return false;
        if (v1 == null) return false;
        if (v2 == null) return true;

        String n1 = stripVersionPrefix(v1);
        String n2 = stripVersionPrefix(v2);

        String[] p1 = n1.split("\\.");
        String[] p2 = n2.split("\\.");
        int max = Math.max(p1.length, p2.length);

        for (int i = 0; i < max; i++) {
            int a = i < p1.length ? parseNumberPart(p1[i]) : 0;
            int b = i < p2.length ? parseNumberPart(p2[i]) : 0;
            if (a != b) return a > b;
        }

        // 모든 숫자 파트가 동일한 경우, 더 많은 파트를 가진 쪽을 최신으로 간주
        if (p1.length != p2.length) return p1.length > p2.length;

        // 최후 수단: 사전적 문자열 비교 (드문 케이스)
        return n1.compareTo(n2) > 0;
    }

    private String stripVersionPrefix(String v) {
        if (v == null) return null;
        return v.replaceFirst("^[vV]", "");
    }

    private int parseNumberPart(String part) {
        if (part == null) return 0;
        String digits = part.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
