package com.teambiund.bander.auth_server.auth.controller.enums;

import static com.teambiund.bander.auth_server.auth.util.data.ConsentTableInit.consentMaps;
import static com.teambiund.bander.auth_server.auth.util.data.ConsentTableInit.consentsAllMaps;

import com.teambiund.bander.auth_server.auth.entity.consentsname.ConsentsTable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "동의서 정보 조회", description = "동의서 목록 및 정보 조회 API")
@RestController
@RequestMapping("/api/auth/enums")
public class ConsentsController {

  @Operation(
      summary = "동의서 목록 조회",
      description = "시스템에서 사용 가능한 동의서 목록을 조회합니다. " + "필수 동의서만 조회하거나 모든 동의서를 조회할 수 있습니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)))
      })
  @GetMapping("/consents")
  public ResponseEntity<Map<String, ConsentsTable>> getAllConsents(
      @Parameter(
              description = "모든 동의서 조회 여부 (true: 전체, false: 필수만)",
              required = true,
              example = "false")
          @RequestParam(name = "all")
          Boolean value) {
    if (value) {
      return new ResponseEntity<>(consentsAllMaps, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(consentMaps, HttpStatus.OK);
    }
  }
}
