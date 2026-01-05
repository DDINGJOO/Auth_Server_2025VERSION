package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.RoleChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "역할 관리 (내부망)", description = "내부망 전용 사용자 역할 변경 API")
public interface RoleControllerSwagger {

  @Operation(
      summary = "사용자 역할 변경",
      description = "이메일로 사용자를 찾아 역할을 변경합니다. (내부망 전용, 권한 검증 없음)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> changeRole(RoleChangeRequest request);
}
