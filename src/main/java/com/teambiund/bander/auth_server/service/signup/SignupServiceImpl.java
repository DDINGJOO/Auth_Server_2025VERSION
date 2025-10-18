package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.event.events.CreateProfileRequest;
import com.teambiund.bander.auth_server.event.publish.CreateProfileRequestEventPub;
import com.teambiund.bander.auth_server.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.service.update.EmailConfirm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class SignupServiceImpl implements SignupService {
  private final SignupStoreService signupStoreService;
  private final ConsentManagementService consentService;
  private final CreateProfileRequestEventPub publishEvent;
  private final EmailConfirm emailConfirm;

  @Override
  public Auth signup(
      String email, String password, String passConfirm, List<ConsentRequest> consentReqs) {
    emailConfirm.checkedConfirmedEmail(email);

    var auth = signupStoreService.signup(email, password, passConfirm);
    publishEvent.createProfileRequestPub(
        new CreateProfileRequest(auth.getId(), auth.getProvider().toString()));

    consentService.saveConsent(auth, consentReqs);
    return auth;
  }

  @Override
  public Auth signupFromOtherProvider(String email, Provider provider) {
    var auth = signupStoreService.signupFromOtherProvider(email, provider);
    publishEvent.createProfileRequestPub(
        new CreateProfileRequest(auth.getId(), auth.getProvider().toString()));
    return auth;
  }
}
