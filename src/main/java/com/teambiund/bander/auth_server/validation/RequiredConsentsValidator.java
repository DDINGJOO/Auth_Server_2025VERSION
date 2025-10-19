package com.teambiund.bander.auth_server.validation;

import static com.teambiund.bander.auth_server.util.data.ConsentTableInit.requiredConsents;
import static com.teambiund.bander.auth_server.util.data.ConsentTableInit.consentsAllMaps;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.consentsname.ConsentsTable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 필수 동의항목 검증 구현체
 * - requiredConsents에 정의된 필수 항목들이 모두 동의되었는지 확인
 * - consentId로 조회하며, 버전 무관하게 consentName(consentType)만 검증
 */
public class RequiredConsentsValidator implements ConstraintValidator<RequiredConsents, List<ConsentRequest>> {

    @Override
    public boolean isValid(List<ConsentRequest> consentRequests, ConstraintValidatorContext context) {
        if (consentRequests == null || consentRequests.isEmpty()) {
            return false;
        }

        // 요청된 동의 항목들의 consentName 집합 (동의된 항목만)
        Set<String> consentedNames = consentRequests.stream()
                .filter(ConsentRequest::isConsented)
                .map(req -> {
                    ConsentsTable consent = consentsAllMaps.get(req.getConsentId());
                    return consent != null ? consent.getConsentName() : null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toSet());

        // 모든 필수 동의항목의 consentName이 동의되었는지 확인 (버전 무관)
        for (ConsentsTable required : requiredConsents) {
            if (!consentedNames.contains(required.getConsentName())) {
                // 구체적인 에러 메시지 설정
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("필수 동의 항목 '%s'에 동의하지 않았습니다",
                                required.getConsentName()))
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
