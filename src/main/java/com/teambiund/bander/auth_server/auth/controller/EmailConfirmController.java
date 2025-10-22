package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이메일 인증", description = "이메일 인증 관련 API")
@RestController
@RequestMapping("/api/auth/emails")
@RequiredArgsConstructor
public class EmailConfirmController {
  private final EmailConfirm emailConfirm;

  @Operation(
      summary = "이메일 인증 확인",
      description = "인증 코드를 사용하여 이메일 인증을 완료합니다. " + "인증 완료 시 사용자 상태가 GUEST에서 USER로 변경됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "인증 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 인증 코드",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "이메일을 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @GetMapping("/{email}")
  public boolean confirmEmail(
      @Parameter(description = "인증 코드", required = true, example = "123456") @RequestParam
          String code,
      @Parameter(description = "인증할 이메일 주소", required = true, example = "user@example.com")
          @PathVariable(name = "email")
          String email) {
    return emailConfirm.confirmEmail(code, email);
  }

  @Operation(summary = "인증 코드 발급", description = "이메일 인증을 위한 인증 코드를 생성하고 이메일로 발송합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "인증 코드 발급 성공",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "이메일을 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/{email}")
  public void generateCode(
      @Parameter(description = "인증 코드를 받을 이메일 주소", required = true, example = "user@example.com")
          @PathVariable(name = "email")
          String email) {
    emailConfirm.generateCode(email);
  }
}
