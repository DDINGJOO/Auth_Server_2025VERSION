package com.teambiund.bander.auth_server.util.key_gerneratre;


import org.springframework.stereotype.Component;

@Component
public interface KeyProvider {

    String generateKey();
}
