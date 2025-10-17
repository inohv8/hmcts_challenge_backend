package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating or updating a task")
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "The title of the task",
            example = "Review case documents",
            required = true,
            maxLength = 200)
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Optional description of the task",
            example = "Review all documents for case #12345",
            maxLength = 2000)
    private String description;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED",
             message = "Status must be one of: PENDING, IN_PROGRESS, COMPLETED")
    @Schema(description = "The status of the task",
            example = "PENDING",
            allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"},
            required = true)
    private String status;

    @NotNull(message = "Due date/time is required")
    @Future(message = "Due date/time must be in the future")
    @Schema(description = "The due date and time for the task",
            example = "2025-12-31T23:59:59",
            required = true)
    private LocalDateTime dueDateTime;

    @Schema(description = "The ID of the caseworker assigned to this task", example = "1")
    private Long assignedToId;
}
