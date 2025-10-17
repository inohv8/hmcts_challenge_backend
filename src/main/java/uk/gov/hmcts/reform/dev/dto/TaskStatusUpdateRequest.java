package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating task status only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for updating task status")
public class TaskStatusUpdateRequest {

    @NotNull(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED",
             message = "Status must be one of: PENDING, IN_PROGRESS, COMPLETED")
    @Schema(description = "The new status of the task",
            example = "IN_PROGRESS",
            allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"},
            required = true)
    private String status;
}
