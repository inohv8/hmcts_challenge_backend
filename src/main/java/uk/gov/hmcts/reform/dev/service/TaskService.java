package uk.gov.hmcts.reform.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.dto.CaseworkerResponse;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.dto.TaskStatusUpdateRequest;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.exception.CaseworkerNotFoundException;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for task management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final CaseworkerRepository caseworkerRepository;
    private final TaskHistoryService taskHistoryService;

    /**
     * Create a new task.
     *
     * @param request the task request
     * @return the created task response
     */
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task with title: {}", request.getTitle());

        Caseworker assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = caseworkerRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new CaseworkerNotFoundException(
                            "Caseworker not found with id: " + request.getAssignedToId()));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.valueOf(request.getStatus()))
                .dueDateTime(request.getDueDateTime())
                .assignedTo(assignedTo)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {}", savedTask.getId());

        taskHistoryService.recordTaskCreation(savedTask);

        if (assignedTo != null) {
            taskHistoryService.recordAssignmentChange(savedTask, null, assignedTo);
        }

        return mapToResponse(savedTask);
    }

    /**
     * Retrieve a task by ID.
     *
     * @param id the task ID
     * @return the task response
     * @throws TaskNotFoundException if task not found
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.info("Retrieving task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return mapToResponse(task);
    }

    /**
     * Retrieve all tasks.
     *
     * @return list of all task responses
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        log.info("Retrieving all tasks");

        List<Task> tasks = taskRepository.findAll();
        log.info("Found {} tasks", tasks.size());

        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a task completely.
     *
     * @param id the task ID
     * @param request the task request
     * @return the updated task response
     * @throws TaskNotFoundException if task not found
     */
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        final String oldStatus = task.getStatus().name();
        final Caseworker oldAssignedTo = task.getAssignedTo();
        final String oldTitle = task.getTitle();
        final String oldDescription = task.getDescription();
        final LocalDateTime oldDueDateTime = task.getDueDateTime();

        Caseworker assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = caseworkerRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new CaseworkerNotFoundException(
                            "Caseworker not found with id: " + request.getAssignedToId()));
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.valueOf(request.getStatus()));
        task.setDueDateTime(request.getDueDateTime());
        task.setAssignedTo(assignedTo);

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated with id: {}", updatedTask.getId());

        String newStatus = updatedTask.getStatus().name();
        if (!oldStatus.equals(newStatus)) {
            taskHistoryService.recordStatusChange(updatedTask, oldStatus, newStatus);
        }

        boolean assignmentChanged = (oldAssignedTo == null && assignedTo != null)
                || (oldAssignedTo != null && assignedTo == null)
                || (oldAssignedTo != null && assignedTo != null
                && !oldAssignedTo.getId().equals(assignedTo.getId()));

        if (assignmentChanged) {
            taskHistoryService.recordAssignmentChange(updatedTask, oldAssignedTo, assignedTo);
        }
        if (!oldTitle.equals(updatedTask.getTitle())
                || !oldDescription.equals(updatedTask.getDescription())) {
            String description = String.format(
                    "Task details updated from Title: '%s', Description: '%s' to Title: '%s', Description: '%s'",
                    oldTitle, oldDescription, updatedTask.getTitle(), updatedTask.getDescription());
            taskHistoryService.recordTaskUpdate(updatedTask, description);
        }

        if (!oldDueDateTime.equals(updatedTask.getDueDateTime())) {
            String description = String.format("Due date changed from %s to %s",
                    oldDueDateTime, updatedTask.getDueDateTime());
            taskHistoryService.recordTaskUpdate(updatedTask, description);
        }

        return mapToResponse(updatedTask);
    }

    /**
     * Update only the status of a task.
     *
     * @param id the task ID
     * @param request the status update request
     * @return the updated task response
     * @throws TaskNotFoundException if task not found
     */
    public TaskResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request) {
        log.info("Updating status of task with id: {} to {}", id, request.getStatus());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        String oldStatus = task.getStatus().name();
        task.setStatus(TaskStatus.valueOf(request.getStatus()));

        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated with id: {}", updatedTask.getId());

        String newStatus = updatedTask.getStatus().name();
        taskHistoryService.recordStatusChange(updatedTask, oldStatus, newStatus);

        return mapToResponse(updatedTask);
    }

    /**
     * Delete a task by ID.
     *
     * @param id the task ID
     * @throws TaskNotFoundException if task not found
     */
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted with id: {}", id);
    }

    /**
     * Map Task entity to TaskResponse DTO.
     *
     * @param task the task entity
     * @return the task response DTO
     */
    private TaskResponse mapToResponse(Task task) {
        CaseworkerResponse caseworkerResponse = null;
        if (task.getAssignedTo() != null) {
            Caseworker caseworker = task.getAssignedTo();
            caseworkerResponse = CaseworkerResponse.builder()
                    .id(caseworker.getId())
                    .firstName(caseworker.getFirstName())
                    .lastName(caseworker.getLastName())
                    .email(caseworker.getEmail())
                    .role(caseworker.getRole())
                    .createdAt(caseworker.getCreatedAt())
                    .fullName(caseworker.getFullName())
                    .build();
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .dueDateTime(task.getDueDateTime())
                .assignedTo(caseworkerResponse)
                .build();
    }
}
