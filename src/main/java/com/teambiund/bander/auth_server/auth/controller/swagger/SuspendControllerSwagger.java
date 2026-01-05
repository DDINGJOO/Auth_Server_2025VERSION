package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.SuspendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 정지 (관리자)", description = "관리자 전용 사용자 정지/해제 API")
public interface SuspendControllerSwagger {

  @Operation(
      summary = "사용자 정지",
      description =
          "관리자가 사용자를 정지시킵니다. "
              + "정지된 사용자는 로그인할 수 없으며, 정지 기간이 지나면 자동으로 해제됩니다. "
              + "(ADMIN 권한 필요)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "정지 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (ADMIN 권한 필요)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> suspend(SuspendRequest req) throws Exception;

  @Operation(
      summary = "사용자 정지 해제",
      description = "관리자가 정지된 사용자의 정지를 즉시 해제합니다. (ADMIN 권한 필요)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "정지 해제 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (ADMIN 권한 필요)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> unsuspend(
      @Parameter(description = "정지 해제할 사용자 ID", required = true, example = "12345") String userId)
      throws Exception;
}
