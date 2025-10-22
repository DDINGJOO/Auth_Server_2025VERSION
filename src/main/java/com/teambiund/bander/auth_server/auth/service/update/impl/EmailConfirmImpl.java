package com.teambiund.bander.auth_server.auth.service.update.impl;

import com.teambiund.bander.auth_server.auth.event.events.EmailConfirmRequest;
import com.teambiund.bander.auth_server.auth.event.publish.EmailConfirmRequestEventPub;
import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import com.teambiund.bander.auth_server.auth.util.generator.generate_code.EmailCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailConfirmImpl implements EmailConfirm {

    public final EmailCodeGenerator emailCodeGenerator;
    public final EmailConfirmRequestEventPub emailConfirmRequestEventPub;


    //TODO : KAFKA EVENT PUBLISHING
    @Override
    public boolean confirmEmail(String code, String email) {
        return emailCodeGenerator.checkCode(code, email);
    }

    @Override
    public Boolean resendEmail(String email) {
        if (emailCodeGenerator.resendEmail(email)) {
            generateCode(email);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void generateCode(String email) {
        String code = emailCodeGenerator.generateCode(email);
        emailConfirmRequestEventPub.emailConfirmReq(new EmailConfirmRequest(email, code));
    }

    @Override
    public boolean checkedConfirmedEmail(String email) {
        return emailCodeGenerator.checkCode("confirmed", email);
    }
}
