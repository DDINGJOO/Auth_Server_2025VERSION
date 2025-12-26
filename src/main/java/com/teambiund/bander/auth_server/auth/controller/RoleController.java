package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.dto.request.RoleChangeRequest;
import com.teambiund.bander.auth_server.auth.service.auth_service.AuthService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "역할 관리 (내부망)", description = "내부망 전용 사용자 역할 변경 API")
@RestController
@RequestMapping("/api/internal/v1/auth/role")
@RequiredArgsConstructor
public class RoleController {

  private final AuthService authService;

  @Operation(
      summary = "사용자 역할 변경",
      description = "이메일로 사용자를 찾아 역할을 변경합니다. (내부망 전용, 권한 검증 없음)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @PutMapping("")
  public ResponseEntity<Boolean> changeRole(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "역할 변경 요청 정보",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = RoleChangeRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                      {
                        "email": "user@example.com",
                        "role": "PLACE_OWNER"
                      }
                      """)))
          @Valid
          @RequestBody
          RoleChangeRequest request) {
    authService.changeRole(request.getEmail(), request.getRole());
    return ResponseEntity.ok(true);
  }
}
