package com.teambiund.bander.auth_server.util;

import org.springframework.stereotype.Component;

import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.mindrot.jbcrypt.BCrypt.hashpw;


@Component
public class BCryptUtil {
    public static String hash(String plain) {
        return hashpw(plain, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    public static boolean verify(String plain, String hashed) {
        return checkpw(plain, hashed);
    }
}
