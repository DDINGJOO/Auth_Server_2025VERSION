package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.SmsCodeRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SmsVerifyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SMS 인증", description = "SMS 인증 관련 API")
public interface SmsConfirmControllerSwagger {

  @Operation(summary = "SMS 인증 코드 발급", description = "SMS 인증을 위한 인증 코드를 생성하고 발송합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "인증 코드 발급 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "이미 발급된 인증 코드가 있음 또는 잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  void generateCode(SmsCodeRequest request);

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
            description = "잘못된 인증 코드 또는 잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  boolean confirmSms(SmsVerifyRequest request);

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
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  boolean resendSms(SmsCodeRequest request);
}
