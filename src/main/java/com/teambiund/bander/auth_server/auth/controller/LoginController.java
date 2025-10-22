package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.dto.request.LoginRequest;
import com.teambiund.bander.auth_server.auth.dto.request.TokenRefreshRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.service.login.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그인", description = "사용자 로그인 및 토큰 관리 API")
@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class LoginController {
  private final LoginService loginService;

  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인하여 JWT Access/Refresh 토큰을 발급받습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = LoginResponse.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "userId": 12345,
                        "email": "user@example.com",
                        "role": "USER"
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (이메일 형식 오류)",
          content = @Content(mediaType = "application/json")
      ),
      @ApiResponse(
          responseCode = "401",
          description = "인증 실패 (이메일 또는 비밀번호 불일치)",
          content = @Content(mediaType = "application/json")
      ),
      @ApiResponse(
          responseCode = "403",
          description = "계정 상태 이상 (정지, 탈퇴, 인증 미완료 등)",
          content = @Content(mediaType = "application/json")
      )
  })
  @PostMapping("")
  public LoginResponse login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "로그인 요청 정보",
          required = true,
          content = @Content(
              schema = @Schema(implementation = LoginRequest.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "email": "user@example.com",
                        "password": "password123"
                      }
                      """
              )
          )
      )
      @Valid @RequestBody LoginRequest loginRequest
  ) {
    return loginService.login(loginRequest.getEmail(), loginRequest.getPassword());
  }

  @Operation(
      summary = "토큰 갱신",
      description = "Refresh 토큰을 사용하여 새로운 Access 토큰과 Refresh 토큰을 발급받습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "토큰 갱신 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = LoginResponse.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "userId": 12345,
                        "email": "user@example.com",
                        "role": "USER"
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "유효하지 않은 Refresh 토큰",
          content = @Content(mediaType = "application/json")
      ),
      @ApiResponse(
          responseCode = "403",
          description = "계정 상태 이상 (정지, 탈퇴 등)",
          content = @Content(mediaType = "application/json")
      )
  })
  @PostMapping("/refreshToken")
  public LoginResponse refreshToken(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "토큰 갱신 요청 정보",
          required = true,
          content = @Content(
              schema = @Schema(implementation = TokenRefreshRequest.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "deviceId": "device-uuid-1234"
                      }
                      """
              )
          )
      )
      @Valid @RequestBody TokenRefreshRequest request
  ) {
    return loginService.refreshToken(request.getRefreshToken(), request.getDeviceId());
  }
}
