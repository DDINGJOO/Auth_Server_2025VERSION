package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.service.withdrawal.WithdrawalManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 탈퇴", description = "회원 탈퇴 및 철회 API")
@RestController
@RequestMapping("/api/v1/auth/withdraw")
@RequiredArgsConstructor
public class WithdrawController {
  private final WithdrawalManagementService withdrawService;

  @Operation(
      summary = "회원 탈퇴",
      description = "사용자가 회원 탈퇴를 요청합니다. " + "탈퇴 후 3년간 데이터가 보관되며, 이 기간이 지나면 자동으로 완전히 삭제됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "탈퇴 요청 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/{userId}")
  public ResponseEntity<Boolean> withdraw(
      @Parameter(description = "탈퇴할 사용자 ID", required = true, example = "12345")
          @PathVariable(name = "userId")
          String userId,
      @Parameter(description = "탈퇴 사유", required = true, example = "서비스가 마음에 들지 않음") @RequestParam
          String withdrawReason)
      throws CustomException {
    withdrawService.withdraw(userId, withdrawReason);
    return ResponseEntity.ok(true);
  }

  @Operation(
      summary = "회원 탈퇴 철회",
      description = "탈퇴를 철회하고 계정을 다시 활성화합니다. " + "3년 보관 기간 내에만 철회가 가능합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "탈퇴 철회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "철회 불가능 (보관 기간 만료 등)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/withdrawRetraction")
  public ResponseEntity<Boolean> withdrawRetraction(
      @Parameter(description = "철회할 사용자 이메일", required = true, example = "user@example.com")
          @RequestParam
          String email)
      throws CustomException {
    withdrawService.withdrawRetraction(email);
    return ResponseEntity.ok(true);
  }
}
