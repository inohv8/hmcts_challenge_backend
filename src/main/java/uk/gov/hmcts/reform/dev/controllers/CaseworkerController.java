package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
import uk.gov.hmcts.reform.dev.dto.CaseworkerResponse;
import uk.gov.hmcts.reform.dev.exception.ErrorResponse;
import uk.gov.hmcts.reform.dev.service.CaseworkerService;

import java.util.List;

/**
 * REST controller for caseworker operations.
 */
@RestController
@RequestMapping("/api/caseworkers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Caseworker Management", description = "APIs for managing caseworkers")
public class CaseworkerController {

    private final CaseworkerService caseworkerService;

    /**
     * Get all caseworkers.
     *
     * @return list of all caseworkers
     */
    @GetMapping
    @Operation(summary = "Get all caseworkers",
               description = "Retrieves a list of all caseworkers in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of caseworkers",
                content = @Content(schema = @Schema(implementation = CaseworkerResponse.class)))
    })
    public ResponseEntity<List<CaseworkerResponse>> getAllCaseworkers() {
        log.info("GET /api/caseworkers - Retrieving all caseworkers");
        List<CaseworkerResponse> caseworkers = caseworkerService.getAllCaseworkers();
        log.info("Retrieved {} caseworkers", caseworkers.size());
        return ResponseEntity.ok(caseworkers);
    }

    /**
     * Get a caseworker by ID.
     *
     * @param id the caseworker ID
     * @return the caseworker response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get caseworker by ID",
               description = "Retrieves a specific caseworker by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved caseworker",
                content = @Content(schema = @Schema(implementation = CaseworkerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Caseworker not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CaseworkerResponse> getCaseworkerById(@PathVariable Long id) {
        log.info("GET /api/caseworkers/{} - Retrieving caseworker", id);
        CaseworkerResponse caseworker = caseworkerService.getCaseworkerById(id);
        return ResponseEntity.ok(caseworker);
    }
}
