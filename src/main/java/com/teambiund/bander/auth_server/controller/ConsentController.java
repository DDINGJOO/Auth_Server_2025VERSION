package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.consent.ConsentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "동의 관리", description = "사용자 동의서 관리 API")
@RestController
@RequestMapping("/api/auth/consent")
@RequiredArgsConstructor
public class ConsentController {
  private final ConsentManagementService consentService;

  @Operation(
      summary = "동의 정보 변경",
      description = "사용자의 동의 정보를 변경합니다. " +
          "이용약관, 개인정보 처리방침 등의 동의 상태를 업데이트할 수 있습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "동의 정보 변경 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Boolean.class),
              examples = @ExampleObject(value = "true")
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (필수 동의 항목 누락 등)",
          content = @Content(mediaType = "application/json")
      ),
      @ApiResponse(
          responseCode = "404",
          description = "사용자를 찾을 수 없음",
          content = @Content(mediaType = "application/json")
      )
  })
  @PutMapping("/{userId}")
  public ResponseEntity<Boolean> consent(
      @Parameter(description = "사용자 ID", required = true, example = "12345")
      @RequestParam(name = "userId") String userId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "변경할 동의 정보 목록",
          required = true,
          content = @Content(
              schema = @Schema(implementation = ConsentRequest.class),
              examples = @ExampleObject(
                  value = """
                      [
                        {
                          "consentId": 1,
                          "agreed": true
                        },
                        {
                          "consentId": 2,
                          "agreed": true
                        },
                        {
                          "consentId": 3,
                          "agreed": false
                        }
                      ]
                      """
              )
          )
      )
      @RequestBody List<ConsentRequest> requests
  ) throws CustomException {
    consentService.changeConsent(userId, requests);
    return ResponseEntity.ok(true);
  }
}
