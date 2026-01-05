package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.SocialLoginRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "소셜 로그인", description = "카카오, 애플, 구글 소셜 로그인 API")
public interface SocialLoginControllerSwagger {

  @Operation(
      summary = "카카오 로그인",
      description = "카카오 OAuth Access Token으로 로그인하여 JWT 토큰을 발급받습니다. 신규 사용자인 경우 자동으로 회원가입됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "소셜 로그인 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 카카오 Access Token",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<LoginResponse> kakaoLogin(SocialLoginRequest request);

  @Operation(
      summary = "애플 로그인",
      description =
          "Apple Sign In Identity Token으로 로그인하여 JWT 토큰을 발급받습니다. 신규 사용자인 경우 자동으로 회원가입됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "소셜 로그인 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 Apple Identity Token",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<LoginResponse> appleLogin(SocialLoginRequest request);

  @Operation(
      summary = "구글 로그인",
      description =
          "Google Sign In ID Token으로 로그인하여 JWT 토큰을 발급받습니다. 신규 사용자인 경우 자동으로 회원가입됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "소셜 로그인 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 Google ID Token",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<LoginResponse> googleLogin(SocialLoginRequest request);
}
