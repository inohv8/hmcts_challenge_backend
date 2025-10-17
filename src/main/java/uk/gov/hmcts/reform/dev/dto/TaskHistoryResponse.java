package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for TaskHistory.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Timeline entry representing a change to a task")
public class TaskHistoryResponse {

    @Schema(description = "Unique identifier of the history entry", example = "1")
    private Long id;

    @Schema(description = "ID of the task this history entry belongs to", example = "1")
    private Long taskId;

    @Schema(description = "Type of change",
            example = "STATUS_CHANGE",
            allowableValues = {"CREATED", "STATUS_CHANGE", "ASSIGNMENT_CHANGE", "UPDATED"})
    private String changeType;

    @Schema(description = "Previous value before the change", example = "PENDING")
    private String oldValue;

    @Schema(description = "New value after the change", example = "IN_PROGRESS")
    private String newValue;

    @Schema(description = "User or system that made the change", example = "system")
    private String changedBy;

    @Schema(description = "Timestamp when the change occurred", example = "2025-10-16T10:30:00")
    private LocalDateTime changedAt;

    @Schema(description = "Human-readable description of the change",
            example = "Status changed from PENDING to IN_PROGRESS")
    private String description;
}
