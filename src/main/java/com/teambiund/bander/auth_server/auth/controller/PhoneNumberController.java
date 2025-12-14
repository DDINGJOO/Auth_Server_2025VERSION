package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.service.auth_service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "휴대폰 번호 확인", description = "휴대폰 번호 인증 여부 확인 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/phone-number")
public class PhoneNumberController {

  private final AuthService authService;

  @Operation(
      summary = "휴대폰 번호 등록 여부 확인",
      description =
          "사용자 ID로 휴대폰 번호가 등록되어 있는지 확인합니다. "
              + "휴대폰 인증이 완료된 사용자는 true, 아직 인증하지 않은 사용자는 false를 반환합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = {
                      @ExampleObject(name = "인증 완료", value = "true"),
                      @ExampleObject(name = "인증 미완료", value = "false")
                    }))
      })
  @GetMapping("/{userId}")
  public ResponseEntity<Boolean> hasPhoneNumber(
      @Parameter(description = "확인할 사용자 ID", required = true, example = "12345")
          @PathVariable(name = "userId")
          String userId) {
    return ResponseEntity.ok(authService.hasPhoneNumber(userId));
  }
}