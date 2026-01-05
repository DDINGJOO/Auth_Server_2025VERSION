package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "회원가입", description = "사용자 회원가입 관련 API")
public interface SignupControllerSwagger {

  @Operation(
      summary = "회원가입",
      description = "이메일 기반 회원가입을 처리합니다. 회원가입 완료 후 이메일 인증을 완료해야 ACTIVE 상태로 전환됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이메일 형식 오류, 비밀번호 불일치, 필수 동의 항목 누락 등)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 이메일",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> signup(SignupRequest req) throws CustomException;
}
