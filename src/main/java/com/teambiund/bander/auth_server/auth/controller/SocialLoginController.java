package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.dto.request.SocialLoginRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.service.social.SocialLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소셜 로그인", description = "카카오, 애플, 구글 소셜 로그인 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
public class SocialLoginController {

  private final SocialLoginService socialLoginService;

  @Operation(
      summary = "카카오 로그인",
      description = "카카오 OAuth Access Token으로 로그인하여 JWT 토큰을 발급받습니다. " + "신규 사용자인 경우 자동으로 회원가입됩니다.")
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
  @PostMapping("/kakao")
  public ResponseEntity<LoginResponse> kakaoLogin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "카카오 OAuth Access Token",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = SocialLoginRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                      {
                        "accessToken": "kakao_oauth_access_token_here"
                      }
                      """)))
          @Valid
          @RequestBody
          SocialLoginRequest request) {
    log.info("카카오 로그인 요청");
    LoginResponse response = socialLoginService.kakaoLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Operation(
      summary = "애플 로그인",
      description =
          "Apple Sign In Identity Token으로 로그인하여 JWT 토큰을 발급받습니다. " + "신규 사용자인 경우 자동으로 회원가입됩니다.")
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
  @PostMapping("/apple")
  public ResponseEntity<LoginResponse> appleLogin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Apple Sign In Identity Token",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = SocialLoginRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                      {
                        "accessToken": "apple_identity_token_here"
                      }
                      """)))
          @Valid
          @RequestBody
          SocialLoginRequest request) {
    log.info("애플 로그인 요청");
    LoginResponse response = socialLoginService.appleLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Operation(
      summary = "구글 로그인",
      description = "Google Sign In ID Token으로 로그인하여 JWT 토큰을 발급받습니다. " + "신규 사용자인 경우 자동으로 회원가입됩니다.")
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
  @PostMapping("/google")
  public ResponseEntity<LoginResponse> googleLogin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Google Sign In ID Token",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = SocialLoginRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                      {
                        "accessToken": "google_id_token_here"
                      }
                      """)))
          @Valid
          @RequestBody
          SocialLoginRequest request) {
    log.info("구글 로그인 요청");
    LoginResponse response = socialLoginService.googleLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
