package com.teambiund.bander.auth_server.auth.aop.statistics;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(10) // 다른 AOP와의 순서가 필요하면 조정
public class ApiRequestCountAspect {

  private final ApiRequestStat apiRequestStat;

  // RestController 내 모든 public 메서드 대상
  @Around("@within(restController) && execution(public * *(..))")
  public Object countApiCalls(ProceedingJoinPoint pjp, RestController restController)
      throws Throwable {
    String apiName = resolveApiName();
    try {
      return pjp.proceed();
    } finally {
      if (apiName != null && !isHealthCheck(apiName)) {
        // 비동기로 통계 증분 (실패해도 요청 흐름에 영향 주지 않음)
        CompletableFuture.runAsync(
            () -> {
              try {
                apiRequestStat.increment(apiName);
              } catch (Exception ex) {
                log.error("사용량 증가를 실패했습니다.={}", apiName, ex);
              }
            });
      }
    }
  }

  private String resolveApiName() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (!(attrs instanceof ServletRequestAttributes)) return null;
    HttpServletRequest req = ((ServletRequestAttributes) attrs).getRequest();
    if (req == null) return null;
    return req.getMethod() + " " + req.getRequestURI();
  }

  private boolean isHealthCheck(String apiName) {
    return apiName.contains("/health");
  }
}
