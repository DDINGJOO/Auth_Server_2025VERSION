package com.teambiund.bander.auth_server.service.update;


import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PhoneNumberUpdateService {

    private final AuthRepository authRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public void updatePhoneNumber(PhoneNumberUpdateRequest req) throws CustomException {
        validator.validatePhoneNumber(req.getPhoneNumber());
        Auth auth = authRepository.findById(req.getUserId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );


        auth.setPhoneNumber(passwordEncoder.encode(req.getPhoneNumber()));
        authRepository.save(auth);

    }
}
