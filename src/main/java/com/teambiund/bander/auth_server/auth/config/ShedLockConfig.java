package com.teambiund.bander.auth_server.auth.config;

import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Spring 스케줄링 활성화
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S") // ShedLock 활성화
public class ShedLockConfig {

  /**
   * 데이터베이스를 잠금 저장소로 사용하는 LockProvider를 빈으로 등록합니다.
   *
   * @param dataSource 각 서비스의 DataSource가 자동으로 주입됩니다.
   * @return JDBC 기반 LockProvider
   */
  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime() // DB 서버 시간을 기준으로 잠금 관리
            .build());
  }
}
