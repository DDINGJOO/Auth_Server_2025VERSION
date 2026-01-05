package com.teambiund.bander.auth_server.auth.controller.swagger;

import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "동의 관리", description = "사용자 동의서 관리 API")
public interface ConsentControllerSwagger {

  @Operation(
      summary = "동의 정보 변경",
      description = "사용자의 동의 정보를 변경합니다. 이용약관, 개인정보 처리방침 등의 동의 상태를 업데이트할 수 있습니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "동의 정보 변경 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(value = "true"))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 동의 항목 누락 등)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<Boolean> consent(
      @Parameter(description = "사용자 ID", required = true, example = "12345") String userId,
      List<ConsentRequest> requests)
      throws CustomException;
}
