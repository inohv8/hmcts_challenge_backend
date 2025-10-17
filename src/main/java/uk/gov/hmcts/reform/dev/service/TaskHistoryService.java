package uk.gov.hmcts.reform.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.dto.TaskHistoryResponse;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.TaskHistory;
import uk.gov.hmcts.reform.dev.repository.TaskHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing task history and audit trail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    /**
     * Record task creation in history.
     *
     * @param task the created task
     */
    public void recordTaskCreation(Task task) {
        String description = String.format("Task '%s' created", task.getTitle());

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changeType("CREATED")
                .newValue(task.getStatus().name())
                .changedBy("system")
                .changedAt(LocalDateTime.now())
                .description(description)
                .build();

        taskHistoryRepository.save(history);
        log.info("Recorded task creation for task id: {}", task.getId());
    }

    /**
     * Record status change in history.
     *
     * @param task the task
     * @param oldStatus the previous status
     * @param newStatus the new status
     */
    public void recordStatusChange(Task task, String oldStatus, String newStatus) {
        String description = String.format("Status changed from %s to %s", oldStatus, newStatus);

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changeType("STATUS_CHANGE")
                .oldValue(oldStatus)
                .newValue(newStatus)
                .changedBy("system")
                .changedAt(LocalDateTime.now())
                .description(description)
                .build();

        taskHistoryRepository.save(history);
        log.info("Recorded status change for task id: {} from {} to {}",
                task.getId(), oldStatus, newStatus);
    }

    /**
     * Record assignment change in history.
     *
     * @param task the task
     * @param oldCaseworker the previous caseworker (null if unassigned)
     * @param newCaseworker the new caseworker (null if unassigning)
     */
    public void recordAssignmentChange(Task task, Caseworker oldCaseworker, Caseworker newCaseworker) {
        String oldValue = oldCaseworker != null ? oldCaseworker.getFullName() : "Unassigned";
        String newValue = newCaseworker != null ? newCaseworker.getFullName() : "Unassigned";

        String description;
        if (oldCaseworker == null && newCaseworker != null) {
            description = String.format("Task assigned to %s", newValue);
        } else if (oldCaseworker != null && newCaseworker == null) {
            description = String.format("Task unassigned from %s", oldValue);
        } else {
            description = String.format("Task reassigned from %s to %s", oldValue, newValue);
        }

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changeType("ASSIGNMENT_CHANGE")
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy("system")
                .changedAt(LocalDateTime.now())
                .description(description)
                .build();

        taskHistoryRepository.save(history);
        log.info("Recorded assignment change for task id: {} from {} to {}",
                task.getId(), oldValue, newValue);
    }

    /**
     * Record task update (general changes).
     *
     * @param task the task
     * @param changes description of what changed
     */
    public void recordTaskUpdate(Task task, String changes) {
        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changeType("UPDATED")
                .changedBy("system")
                .changedAt(LocalDateTime.now())
                .description(changes)
                .build();

        taskHistoryRepository.save(history);
        log.info("Recorded task update for task id: {}", task.getId());
    }

    /**
     * Get task history timeline ordered by most recent first.
     *
     * @param taskId the task ID
     * @return list of history entries
     */
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskTimeline(Long taskId) {
        log.info("Retrieving timeline for task id: {}", taskId);

        List<TaskHistory> history = taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);

        return history.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get task history timeline ordered chronologically (oldest first).
     *
     * @param taskId the task ID
     * @return list of history entries
     */
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskTimelineChronological(Long taskId) {
        log.info("Retrieving chronological timeline for task id: {}", taskId);

        List<TaskHistory> history = taskHistoryRepository.findByTaskIdOrderByChangedAtAsc(taskId);

        return history.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map TaskHistory entity to TaskHistoryResponse DTO.
     *
     * @param history the history entity
     * @return the history response DTO
     */
    private TaskHistoryResponse mapToResponse(TaskHistory history) {
        return TaskHistoryResponse.builder()
                .id(history.getId())
                .taskId(history.getTask().getId())
                .changeType(history.getChangeType())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .description(history.getDescription())
                .build();
    }
}
