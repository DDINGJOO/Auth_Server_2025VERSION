package com.teambiund.bander.auth_server.auth.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "헬스체크", description = "서버 상태 확인 API")
public interface HealthCheckControllerSwagger {

  @Operation(
      summary = "서버 상태 확인",
      description = "서버가 정상적으로 실행 중인지 확인합니다. 로드밸런서 및 모니터링 시스템에서 사용됩니다.")
  @ApiResponse(
      responseCode = "200",
      description = "서버 정상 동작 중",
      content =
          @Content(
              mediaType = "text/plain",
              examples = @ExampleObject(value = "Server is up and running")))
  String healthCheck();
}
