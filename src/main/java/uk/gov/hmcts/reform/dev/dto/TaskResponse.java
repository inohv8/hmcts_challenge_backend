package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing task details")
public class TaskResponse {

    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;

    @Schema(description = "The title of the task", example = "Review case documents")
    private String title;

    @Schema(description = "Optional description of the task", example = "Review all documents for case #12345")
    private String description;

    @Schema(description = "The status of the task", example = "PENDING")
    private String status;

    @Schema(description = "The due date and time for the task", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDateTime;

    @Schema(description = "The caseworker assigned to this task")
    private CaseworkerResponse assignedTo;
}
