package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.event.events.CreateProfileRequest;
import com.teambiund.bander.auth_server.event.publish.CreateProfileRequestEventPub;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SignupServiceImpl implements SignupService {
    private final SignupStoreService signupStoreService;
    private final CreateProfileRequestEventPub publishEvent;
    private final Validator validator;

    @Override
    public Auth signup(String email, String password, String passConfirm, List<ConsentRequest> consentReqs) {
        validator(email, password, passConfirm, consentReqs);
        var auth = signupStoreService.signup(email, password, passConfirm);
        publishEvent.createProfileRequestPub(new CreateProfileRequest(
                auth.getId(),
                auth.getProvider().toString()
        ));
        return auth;
    }

    @Override
    public Auth signupFromOtherProvider(String email, Provider provider) {
        var auth = signupStoreService.signupFromOtherProvider(email, provider);
        publishEvent.createProfileRequestPub(new CreateProfileRequest(
                auth.getId(),
                auth.getProvider().toString()
        ));
        return auth;
    }

    private void validator(String email, String password, String passConfirm, List<ConsentRequest> consentReqs) {
        validator.emailValid(email);
        validator.passwordValid(password);
        validator.passConfirmValid(password, passConfirm);
        validator.validateConsentList(consentReqs);

    }
}
