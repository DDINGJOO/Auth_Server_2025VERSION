package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.dto.request.SmsCodeRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SmsVerifyRequest;
import com.teambiund.bander.auth_server.auth.service.update.SmsConfirmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            description = "이미 발급된 인증 코드가 있음 또는 잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "SMS 인증 코드 발급 요청",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SmsCodeRequest.class),
              examples =
                  @ExampleObject(
                      value = "{\"userId\": \"user-id-123\", \"phoneNumber\": \"01012345678\"}")))
  @PostMapping("/request")
  public void generateCode(@Valid @RequestBody SmsCodeRequest request) {
    smsConfirmService.generateCode(request.getUserId(), request.getPhoneNumber());
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
            description = "잘못된 인증 코드 또는 잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "SMS 인증 확인 요청",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SmsVerifyRequest.class),
              examples =
                  @ExampleObject(
                      value =
                          "{\"userId\": \"user-id-123\", \"phoneNumber\": \"01012345678\", \"code\": \"123456\"}")))
  @PostMapping("/verify")
  public boolean confirmSms(@Valid @RequestBody SmsVerifyRequest request) {
    return smsConfirmService.confirmSms(
        request.getUserId(), request.getPhoneNumber(), request.getCode());
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
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(mediaType = "application/json"))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "SMS 인증 코드 재발신 요청",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SmsCodeRequest.class),
              examples =
                  @ExampleObject(
                      value = "{\"userId\": \"user-id-123\", \"phoneNumber\": \"01012345678\"}")))
  @PostMapping("/resend")
  public boolean resendSms(@Valid @RequestBody SmsCodeRequest request) {
    return smsConfirmService.resendSms(request.getUserId(), request.getPhoneNumber());
  }
}
