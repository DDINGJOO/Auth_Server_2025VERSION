package com.teambiund.bander.auth_server.aop.statictics;

import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.HistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiRequestCountAspectTest.TestConfig.class)
class ApiRequestCountAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiRequestStat apiRequestStat; // AOP가 이 빈을 주입받아 호출함

    // 더 이상 @MockBean을 사용하지 않고 테스트용 빈을 TestConfig에서 제공
    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private AuthRepository authRepository;


    @Test
    @DisplayName("일반 API 호출 시 AOP가 사용량 증가를 기록한다")
    void aspect_increments_on_normal_api() throws Exception {
        mockMvc.perform(get("/test/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        // 비동기 호출이므로 짧게 대기 (더 견고하게는 Executor 주입/동기화 방법 권장)
        Thread.sleep(50);

        verify(apiRequestStat, times(1)).increment("GET /test/hello");
    }

    @Test
    @DisplayName("헬스체크 API(/health)는 카운트 대상에서 제외된다")
    void aspect_skips_health_api() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());

        Thread.sleep(50);

        verify(apiRequestStat, never()).increment(anyString());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/hello")
        public String hello() {
            return "ok";
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        ApiRequestStat apiRequestStat() {
            return mock(ApiRequestStat.class);
        }

        @Bean
        HistoryRepository historyRepository() {
            return mock(HistoryRepository.class);
        }

        @Bean
        AuthRepository authRepository() {
            return mock(AuthRepository.class);
        }

        // 헬스체크 엔드포인트를 테스트 컨텍스트에 추가하여 /health 요청이 200을 반환하도록 함
        @RestController
        static class HealthController {
            @GetMapping("/health")
            public String health() {
                return "ok";
            }
        }

        // 테스트용 컨트롤러를 TestConfig 내부에 두어 Spring 컨텍스트에 등록되도록 함
        @RestController
        @RequestMapping("/test")
        static class TestController {
            @GetMapping("/hello")
            public String hello() {
                return "ok";
            }
        }
    }
}
