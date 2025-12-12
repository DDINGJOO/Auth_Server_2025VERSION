package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.service.update.SmsConfirmService;
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

@Tag(name = "SMS 인증", description = "SMS 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth/sms")
@RequiredArgsConstructor
public class SmsConfirmController {

  private final SmsConfirmService smsConfirmService;

  @Operation(summary = "SMS 인증 코드 발급", description = "SMS 인증을 위한 인증 코드를 생성하고 발송합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "인증 코드 발급 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "이미 발급된 인증 코드가 있음",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/{userId}/{phoneNumber}")
  public void generateCode(
      @Parameter(description = "사용자 ID", required = true, example = "user-id-123")
          @PathVariable(name = "userId")
          String userId,
      @Parameter(description = "전화번호", required = true, example = "01012345678")
          @PathVariable(name = "phoneNumber")
          String phoneNumber) {
    smsConfirmService.generateCode(userId, phoneNumber);
  }

  @Operation(
      summary = "SMS 인증 확인",
      description = "인증 코드를 사용하여 SMS 인증을 완료합니다. 인증 완료 시 전화번호가 암호화되어 저장됩니다.")
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
            content = @Content(mediaType = "application/json"))
      })
  @GetMapping("/{userId}/{phoneNumber}")
  public boolean confirmSms(
      @Parameter(description = "인증 코드", required = true, example = "123456") @RequestParam
          String code,
      @Parameter(description = "사용자 ID", required = true, example = "user-id-123")
          @PathVariable(name = "userId")
          String userId,
      @Parameter(description = "전화번호", required = true, example = "01012345678")
          @PathVariable(name = "phoneNumber")
          String phoneNumber) {
    return smsConfirmService.confirmSms(userId, phoneNumber, code);
  }

  @Operation(summary = "SMS 인증 코드 재발신", description = "SMS 인증 코드를 재발신합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "재발신 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true")))
      })
  @PutMapping("/{userId}/{phoneNumber}")
  public boolean resendSms(
      @Parameter(description = "사용자 ID", required = true, example = "user-id-123")
          @PathVariable(name = "userId")
          String userId,
      @Parameter(description = "전화번호", required = true, example = "01012345678")
          @PathVariable(name = "phoneNumber")
          String phoneNumber) {
    return smsConfirmService.resendSms(userId, phoneNumber);
  }
}