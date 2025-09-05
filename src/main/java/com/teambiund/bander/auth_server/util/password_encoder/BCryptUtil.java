package com.teambiund.bander.auth_server.util.password_encoder;

import jakarta.persistence.OneToOne;
import org.springframework.stereotype.Component;

import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.mindrot.jbcrypt.BCrypt.hashpw;


@Component
public class BCryptUtil implements PasswordEncoder {
    @Override
    public  String encode(String plain) {
        return hashpw(plain, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }



    @Override
    public  boolean matches(String plain, String hashed) {
        return checkpw(plain, hashed);
    }
}
