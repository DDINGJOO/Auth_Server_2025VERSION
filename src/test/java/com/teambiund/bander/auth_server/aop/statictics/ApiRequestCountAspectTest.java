package com.teambiund.bander.auth_server.aop.statictics;

import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.HistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiRequestCountAspectTest.TestController.class)
@Import(ApiRequestCountAspect.class)
class ApiRequestCountAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiRequestStat apiRequestStat; // AOP가 이 빈을 주입받아 호출함

    // 애플리케이션의 다른 서비스/리포지토리 의존성으로 인한 컨텍스트 로드 실패를 막기 위해 모킹
    @MockBean
    private HistoryRepository historyRepository;

    @MockBean
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
}
