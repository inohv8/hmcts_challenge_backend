package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.dto.TaskStatusUpdateRequest;
import uk.gov.hmcts.reform.dev.dto.TaskHistoryResponse;
import uk.gov.hmcts.reform.dev.exception.ErrorResponse;
import uk.gov.hmcts.reform.dev.service.TaskService;
import uk.gov.hmcts.reform.dev.service.TaskHistoryService;

import java.util.List;

/**
 * REST Controller for task management operations.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "APIs for managing caseworker tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskHistoryService taskHistoryService;

    /**
     * Create a new task.
     *
     * @param request the task request
     * @return the created task
     */
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task for caseworkers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("Received request to create task: {}", request.getTitle());
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieve a task by ID.
     *
     * @param id the task ID
     * @return the task
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task found",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task with id: {}", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all tasks.
     *
     * @return list of all tasks
     */
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.info("Received request to get all tasks");
        List<TaskResponse> responses = taskService.getAllTasks();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a task completely.
     *
     * @param id the task ID
     * @param request the task request
     * @return the updated task
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates all fields of an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        log.info("Received request to update task with id: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update only the status of a task.
     *
     * @param id the task ID
     * @param request the status update request
     * @return the updated task
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates only the status of an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        log.info("Received request to update status of task with id: {}", id);
        TaskResponse response = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task by ID.
     *
     * @param id the task ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get task history timeline.
     *
     * @param id the task ID
     * @return list of history entries ordered by most recent first
     */
    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get task timeline",
               description = "Retrieves the complete history timeline for a task "
                       + "including status changes and assignments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Timeline retrieved successfully",
                content = @Content(schema = @Schema(implementation = TaskHistoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TaskHistoryResponse>> getTaskTimeline(@PathVariable Long id) {
        log.info("Received request to get timeline for task with id: {}", id);

        taskService.getTaskById(id);

        List<TaskHistoryResponse> timeline = taskHistoryService.getTaskTimeline(id);
        return ResponseEntity.ok(timeline);
    }
}
