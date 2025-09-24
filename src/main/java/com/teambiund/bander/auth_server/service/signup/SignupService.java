package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;

import java.util.List;

public interface SignupService {
    Auth signup(String email, String password, String passConfirm, List<ConsentRequest> consentReqs);

    Auth signupFromOtherProvider(String email, Provider provider);
}
