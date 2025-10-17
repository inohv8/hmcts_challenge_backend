package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.TaskHistoryResponse;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.entities.TaskHistory;
import uk.gov.hmcts.reform.dev.repository.TaskHistoryRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TaskHistoryService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskHistoryService Unit Tests")
class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    @Captor
    private ArgumentCaptor<TaskHistory> historyCaptor;

    private Task testTask;
    private Caseworker testCaseworker1;
    private Caseworker testCaseworker2;

    @BeforeEach
    void setUp() {
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(1))
                .build();

        testCaseworker1 = Caseworker.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@hmcts.gov.uk")
                .role("Caseworker")
                .createdAt(LocalDateTime.now())
                .build();

        testCaseworker2 = Caseworker.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@hmcts.gov.uk")
                .role("Senior Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should record task creation successfully")
    void shouldRecordTaskCreation() {
        // When
        taskHistoryService.recordTaskCreation(testTask);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("CREATED", captured.getChangeType());
        assertEquals("PENDING", captured.getNewValue());
        assertNull(captured.getOldValue());
        assertEquals("system", captured.getChangedBy());
        assertNotNull(captured.getChangedAt());
        assertEquals("Task 'Test Task' created", captured.getDescription());
    }

    @Test
    @DisplayName("Should record status change successfully")
    void shouldRecordStatusChange() {
        // When
        taskHistoryService.recordStatusChange(testTask, "PENDING", "IN_PROGRESS");

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("STATUS_CHANGE", captured.getChangeType());
        assertEquals("PENDING", captured.getOldValue());
        assertEquals("IN_PROGRESS", captured.getNewValue());
        assertEquals("system", captured.getChangedBy());
        assertEquals("Status changed from PENDING to IN_PROGRESS", captured.getDescription());
    }

    @Test
    @DisplayName("Should record assignment to caseworker")
    void shouldRecordAssignmentToCaseworker() {
        // When
        taskHistoryService.recordAssignmentChange(testTask, null, testCaseworker1);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("ASSIGNMENT_CHANGE", captured.getChangeType());
        assertEquals("Unassigned", captured.getOldValue());
        assertEquals("John Doe", captured.getNewValue());
        assertEquals("Task assigned to John Doe", captured.getDescription());
    }

    @Test
    @DisplayName("Should record unassignment from caseworker")
    void shouldRecordUnassignmentFromCaseworker() {
        // When
        taskHistoryService.recordAssignmentChange(testTask, testCaseworker1, null);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("ASSIGNMENT_CHANGE", captured.getChangeType());
        assertEquals("John Doe", captured.getOldValue());
        assertEquals("Unassigned", captured.getNewValue());
        assertEquals("Task unassigned from John Doe", captured.getDescription());
    }

    @Test
    @DisplayName("Should record reassignment between caseworkers")
    void shouldRecordReassignment() {
        // When
        taskHistoryService.recordAssignmentChange(testTask, testCaseworker1, testCaseworker2);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("ASSIGNMENT_CHANGE", captured.getChangeType());
        assertEquals("John Doe", captured.getOldValue());
        assertEquals("Jane Smith", captured.getNewValue());
        assertEquals("Task reassigned from John Doe to Jane Smith", captured.getDescription());
    }

    @Test
    @DisplayName("Should retrieve task timeline ordered by most recent")
    void shouldRetrieveTaskTimeline() {
        // Given
        TaskHistory history1 = TaskHistory.builder()
                .id(1L)
                .task(testTask)
                .changeType("CREATED")
                .newValue("PENDING")
                .changedBy("system")
                .changedAt(LocalDateTime.now().minusHours(2))
                .description("Task created")
                .build();

        TaskHistory history2 = TaskHistory.builder()
                .id(2L)
                .task(testTask)
                .changeType("STATUS_CHANGE")
                .oldValue("PENDING")
                .newValue("IN_PROGRESS")
                .changedBy("system")
                .changedAt(LocalDateTime.now().minusHours(1))
                .description("Status changed")
                .build();

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(1L))
                .thenReturn(Arrays.asList(history2, history1));

        // When
        List<TaskHistoryResponse> timeline = taskHistoryService.getTaskTimeline(1L);

        // Then
        assertNotNull(timeline);
        assertEquals(2, timeline.size());
        assertEquals("STATUS_CHANGE", timeline.get(0).getChangeType());
        assertEquals("CREATED", timeline.get(1).getChangeType());
        verify(taskHistoryRepository, times(1)).findByTaskIdOrderByChangedAtDesc(1L);
    }

    @Test
    @DisplayName("Should retrieve chronological timeline")
    void shouldRetrieveChronologicalTimeline() {
        // Given
        TaskHistory history1 = TaskHistory.builder()
                .id(1L)
                .task(testTask)
                .changeType("CREATED")
                .changedAt(LocalDateTime.now().minusHours(2))
                .build();

        TaskHistory history2 = TaskHistory.builder()
                .id(2L)
                .task(testTask)
                .changeType("STATUS_CHANGE")
                .changedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(taskHistoryRepository.findByTaskIdOrderByChangedAtAsc(1L))
                .thenReturn(Arrays.asList(history1, history2));

        // When
        List<TaskHistoryResponse> timeline = taskHistoryService.getTaskTimelineChronological(1L);

        // Then
        assertNotNull(timeline);
        assertEquals(2, timeline.size());
        assertEquals("CREATED", timeline.get(0).getChangeType());
        assertEquals("STATUS_CHANGE", timeline.get(1).getChangeType());
    }

    @Test
    @DisplayName("Should record task update with changes description")
    void shouldRecordTaskUpdate() {
        // Given
        String changes = "Title updated from 'Old Title' to 'New Title'";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals("system", captured.getChangedBy());
        assertEquals(changes, captured.getDescription());
        assertNotNull(captured.getChangedAt());
        assertEquals(testTask, captured.getTask());
        assertNull(captured.getOldValue());
        assertNull(captured.getNewValue());
    }

    @Test
    @DisplayName("Should record task update with multiple field changes")
    void shouldRecordTaskUpdateWithMultipleChanges() {
        // Given
        String changes = "Updated title and description";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals(changes, captured.getDescription());
        assertNotNull(captured.getChangedAt());
    }

    @Test
    @DisplayName("Should record task update with empty changes description")
    void shouldRecordTaskUpdateWithEmptyDescription() {
        // Given
        String changes = "";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals("", captured.getDescription());
        assertEquals("system", captured.getChangedBy());
    }

    @Test
    @DisplayName("Should record task update with null changes description")
    void shouldRecordTaskUpdateWithNullDescription() {
        // Given
        String changes = null;

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertNull(captured.getDescription());
        assertEquals("system", captured.getChangedBy());
    }

    @Test
    @DisplayName("Should record task update with detailed change description")
    void shouldRecordTaskUpdateWithDetailedDescription() {
        // Given
        String changes = "Due date changed from 2025-01-01T10:00:00 to 2025-01-15T10:00:00";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals(changes, captured.getDescription());
        assertEquals(testTask.getId(), captured.getTask().getId());
    }

    @Test
    @DisplayName("Should record multiple task updates sequentially")
    void shouldRecordMultipleTaskUpdates() {
        // Given
        String changes1 = "First update";
        String changes2 = "Second update";
        String changes3 = "Third update";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes1);
        taskHistoryService.recordTaskUpdate(testTask, changes2);
        taskHistoryService.recordTaskUpdate(testTask, changes3);

        // Then
        verify(taskHistoryRepository, times(3)).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record task update with special characters in description")
    void shouldRecordTaskUpdateWithSpecialCharacters() {
        // Given
        String changes = "Updated: 'Title' with \"quotes\" & symbols: @#$%^&*()";

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals(changes, captured.getDescription());
    }

    @Test
    @DisplayName("Should record task update with very long description")
    void shouldRecordTaskUpdateWithLongDescription() {
        // Given
        String changes = "Updated multiple fields: " + "A".repeat(500);

        // When
        taskHistoryService.recordTaskUpdate(testTask, changes);

        // Then
        verify(taskHistoryRepository, times(1)).save(historyCaptor.capture());
        TaskHistory captured = historyCaptor.getValue();

        assertEquals("UPDATED", captured.getChangeType());
        assertEquals(changes, captured.getDescription());
        assertEquals(testTask, captured.getTask());
    }
}
