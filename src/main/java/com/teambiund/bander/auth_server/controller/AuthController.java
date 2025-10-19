package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.response.SimpleAuthResponse;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.auth_service.AuthService;
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

@Tag(name = "사용자 인증 정보", description = "사용자 인증 정보 조회 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @Operation(
      summary = "사용자 인증 정보 조회",
      description = "사용자 ID로 사용자의 기본 인증 정보를 조회합니다. " + "다른 마이크로서비스에서 사용자 정보를 확인할 때 사용됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimpleAuthResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                      {
                        "userId": "12345",
                        "email": "user@example.com",
                        "status": "ACTIVE",
                        "role": "USER",
                        "provider": "SYSTEM"
                      }
                      """))),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  @GetMapping("{userId}")
  public ResponseEntity<SimpleAuthResponse> getAuth(
      @Parameter(description = "조회할 사용자 ID", required = true, example = "12345")
          @PathVariable(name = "userId")
          String userId)
      throws CustomException {
    return ResponseEntity.ok(authService.getAuth(userId));
  }
}
