package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.dto.TaskStatusUpdateRequest;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.exception.CaseworkerNotFoundException;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TaskService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CaseworkerRepository caseworkerRepository;

    @Mock
    private TaskHistoryService taskHistoryService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequest taskRequest;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDateTime.now().plusDays(7);

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(futureDate)
                .build();

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(futureDate)
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskResponse response = taskService.createTask(taskRequest);

        // Then
        assertNotNull(response);
        assertEquals(task.getId(), response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus().name(), response.getStatus());
        assertEquals(task.getDueDateTime(), response.getDueDateTime());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertNotNull(response);
        assertEquals(task.getId(), response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task not found")
    void testGetTaskByIdNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void testGetAllTasks() {
        // Given
        Task task2 = Task.builder()
                .id(2L)
                .title("Test Task 2")
                .description("Test Description 2")
                .status(TaskStatus.IN_PROGRESS)
                .dueDateTime(futureDate)
                .build();

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task, task2));

        // When
        List<TaskResponse> responses = taskService.getAllTasks();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Test Task", responses.get(0).getTitle());
        assertEquals("Test Task 2", responses.get(1).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask() {
        // Given
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status("IN_PROGRESS")
                .dueDateTime(futureDate)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Then
        assertNotNull(response);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when updating non-existent task")
    void testUpdateTaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, taskRequest));
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus() {
        // Given
        TaskStatusUpdateRequest statusRequest = TaskStatusUpdateRequest.builder()
                .status("COMPLETED")
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskResponse response = taskService.updateTaskStatus(1L, statusRequest);

        // Then
        assertNotNull(response);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when updating status of non-existent task")
    void testUpdateTaskStatusNotFound() {
        // Given
        TaskStatusUpdateRequest statusRequest = TaskStatusUpdateRequest.builder()
                .status("COMPLETED")
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTaskStatus(1L, statusRequest));
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when deleting non-existent task")
    void testDeleteTaskNotFound() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should throw CaseworkerNotFoundException when creating task with invalid caseworker ID")
    void testCreateTaskWithInvalidCaseworkerId() {
        // Given
        Long invalidCaseworkerId = 999L;
        TaskRequest requestWithInvalidCaseworker = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(futureDate)
                .assignedToId(invalidCaseworkerId)
                .build();

        when(caseworkerRepository.findById(invalidCaseworkerId)).thenReturn(Optional.empty());

        // When & Then
        CaseworkerNotFoundException exception = assertThrows(
                CaseworkerNotFoundException.class,
                () -> taskService.createTask(requestWithInvalidCaseworker)
        );

        assertEquals("Caseworker not found with id: " + invalidCaseworkerId, exception.getMessage());
        verify(caseworkerRepository, times(1)).findById(invalidCaseworkerId);
        verify(taskRepository, times(0)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw CaseworkerNotFoundException when updating task with invalid caseworker ID")
    void testUpdateTaskWithInvalidCaseworkerId() {
        // Given
        Long invalidCaseworkerId = 999L;
        TaskRequest updateRequestWithInvalidCaseworker = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status("IN_PROGRESS")
                .dueDateTime(futureDate)
                .assignedToId(invalidCaseworkerId)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(caseworkerRepository.findById(invalidCaseworkerId)).thenReturn(Optional.empty());

        // When & Then
        CaseworkerNotFoundException exception = assertThrows(
                CaseworkerNotFoundException.class,
                () -> taskService.updateTask(1L, updateRequestWithInvalidCaseworker)
        );

        assertEquals("Caseworker not found with id: " + invalidCaseworkerId, exception.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(caseworkerRepository, times(1)).findById(invalidCaseworkerId);
        verify(taskRepository, times(0)).save(any(Task.class));
    }
}
