package com.teambiund.bander.auth_server.util.password_encoder;


public interface PasswordEncoder {
    public String encode(String plain);
    public boolean matches(String plain, String encoded);
}
