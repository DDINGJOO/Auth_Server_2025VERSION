package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.LoginRequest;
import com.teambiund.bander.auth_server.auth.dto.request.TokenRefreshRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "로그인", description = "사용자 로그인 및 토큰 관리 API")
public interface LoginControllerSwagger {

  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인하여 JWT Access/Refresh 토큰을 발급받습니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "userId": 12345,
                      "email": "user@example.com",
                      "role": "USER"
                    }
                    """))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이메일 형식 오류)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "403",
            description = "계정 상태 이상 (정지, 탈퇴, 인증 미완료 등)",
            content = @Content(mediaType = "application/json"))
      })
  LoginResponse login(
      LoginRequest loginRequest,
      @Parameter(
              description = "앱 타입 (GENERAL: 일반 앱, PLACE_MANAGER: 공간관리자 앱). 미지정 시 GENERAL로 처리",
              example = "GENERAL")
          String appTypeHeader);

  @Operation(
      summary = "토큰 갱신",
      description = "Refresh 토큰을 사용하여 새로운 Access 토큰과 Refresh 토큰을 발급받습니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "userId": 12345,
                      "email": "user@example.com",
                      "role": "USER"
                    }
                    """))),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 Refresh 토큰",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "403",
            description = "계정 상태 이상 (정지, 탈퇴 등)",
            content = @Content(mediaType = "application/json"))
      })
  LoginResponse refreshToken(
      TokenRefreshRequest request,
      @Parameter(
              description = "앱 타입 (GENERAL: 일반 앱, PLACE_MANAGER: 공간관리자 앱). 미지정 시 GENERAL로 처리",
              example = "GENERAL")
          String appTypeHeader);
}
