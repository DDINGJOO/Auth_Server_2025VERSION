package com.teambiund.bander.auth_server.validation;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.consents_name.ConsentsTable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static com.teambiund.bander.auth_server.util.data.ConsentTable_init.requiredConsents;

/**
 * 필수 동의항목 검증 구현체
 * - requiredConsents에 정의된 필수 항목들이 모두 동의되었는지 확인
 */
public class RequiredConsentsValidator implements ConstraintValidator<RequiredConsents, List<ConsentRequest>> {

    @Override
    public boolean isValid(List<ConsentRequest> consentRequests, ConstraintValidatorContext context) {
        if (consentRequests == null || consentRequests.isEmpty()) {
            return false;
        }

        // 모든 필수 동의항목이 동의되었는지 확인
        for (ConsentsTable required : requiredConsents) {
            boolean found = consentRequests.stream()
                    .anyMatch(req ->
                            req.getConsentName().equals(required.getConsentName()) &&
                            req.getVersion().equals(required.getVersion()) &&
                            req.isConsented()
                    );

            if (!found) {
                // 구체적인 에러 메시지 설정
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("필수 동의 항목 '%s' (버전: %s)에 동의하지 않았습니다",
                                required.getConsentName(), required.getVersion()))
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
