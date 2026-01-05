package com.teambiund.bander.auth_server.auth.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "비밀번호 변경", description = "사용자 비밀번호 변경 API")
public interface PasswordChangeControllerSwagger {

  @Operation(
      summary = "비밀번호 변경",
      description = "사용자의 비밀번호를 변경합니다. 새 비밀번호와 확인 비밀번호가 일치해야 하며, 비밀번호 정책을 만족해야 합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "비밀번호 변경 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (비밀번호 불일치, 정책 위반 등)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> changePassword(
      @Parameter(description = "사용자 이메일", required = true, example = "user@example.com")
          String email,
      @Parameter(description = "새 비밀번호", required = true, example = "newPassword123")
          String newPassword,
      @Parameter(description = "비밀번호 확인", required = true, example = "newPassword123")
          String passConfirm)
      throws Exception;
}
