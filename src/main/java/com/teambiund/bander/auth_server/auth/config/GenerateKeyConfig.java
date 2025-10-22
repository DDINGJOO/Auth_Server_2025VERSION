package com.teambiund.bander.auth_server.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambiund.bander.auth_server.auth.event.publish.EmailConfirmRequestEventPub;
import com.teambiund.bander.auth_server.auth.event.publish.EventPublisher;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import com.teambiund.bander.auth_server.auth.service.update.impl.EmailConfirmImpl;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.generate_code.EmailCodeGenerator;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.auth.util.generator.key.impl.Snowflake;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class GenerateKeyConfig {

  @Bean
  public KeyProvider keyGenerator() {
    return new Snowflake();
  }

  @Bean
  public EmailConfirm emailConfirm(
      StringRedisTemplate stringRedisTemplate,
      KafkaTemplate<String, Object> kafkaTemplate,
      ObjectMapper objectMapper,
      AuthRepository authRepository,
      @Qualifier("aesCipherStrategy") CipherStrategy emailCipher) {
    return new EmailConfirmImpl(
        new EmailCodeGenerator(authRepository, stringRedisTemplate, emailCipher),
        new EmailConfirmRequestEventPub(new EventPublisher(kafkaTemplate, objectMapper)));
  }
}
