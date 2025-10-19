package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원가입", description = "사용자 회원가입 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignupController {
  private final SignupService signupStoreService;

  @Operation(
      summary = "회원가입",
      description = "이메일 기반 회원가입을 처리합니다. " +
          "회원가입 완료 후 이메일 인증을 완료해야 ACTIVE 상태로 전환됩니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "회원가입 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Boolean.class),
              examples = @ExampleObject(value = "true")
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (이메일 형식 오류, 비밀번호 불일치, 필수 동의 항목 누락 등)",
          content = @Content(mediaType = "application/json")
      ),
      @ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 이메일",
          content = @Content(mediaType = "application/json")
      )
  })
  @PostMapping("/signup")
  public ResponseEntity<Boolean> signup(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "회원가입 요청 정보",
          required = true,
          content = @Content(
              schema = @Schema(implementation = SignupRequest.class),
              examples = @ExampleObject(
                  name = "회원가입 예시",
                  value = """
                      {
                        "email": "user@example.com",
                        "password": "password123",
                        "passwordConfirm": "password123",
                        "consentIds": [1, 2, 3]
                      }
                      """
              )
          )
      )
      @Valid @RequestBody SignupRequest req
  ) throws CustomException {

    signupStoreService.signup(req);
    return ResponseEntity.ok(true);
  }
}
