package com.teambiund.bander.auth_server.auth.util.generator.key;

import org.springframework.stereotype.Component;

@Component
public interface KeyProvider {

  String generateKey();

  Long generateLongKey();
}
