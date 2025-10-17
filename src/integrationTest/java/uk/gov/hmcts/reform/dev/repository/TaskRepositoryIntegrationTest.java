package uk.gov.hmcts.reform.dev.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for TaskRepository.
 */
@DataJpaTest
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save task successfully")
    void testSaveTask() {
        Task task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("Test Task", savedTask.getTitle());
        assertEquals("Test Description", savedTask.getDescription());
        assertEquals(TaskStatus.PENDING, savedTask.getStatus());
    }

    @Test
    @DisplayName("Should find task by ID")
    void testFindById() {
        Task task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task savedTask = taskRepository.save(task);

        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        assertTrue(foundTask.isPresent());
        assertEquals(savedTask.getId(), foundTask.get().getId());
        assertEquals("Test Task", foundTask.get().getTitle());
    }

    @Test
    @DisplayName("Should return empty when task not found")
    void testFindByIdNotFound() {
        Optional<Task> foundTask = taskRepository.findById(999L);

        assertFalse(foundTask.isPresent());
    }

    @Test
    @DisplayName("Should find all tasks")
    void testFindAll() {
        Task task1 = Task.builder()
                .title("Task 1")
                .description("Description 1")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task task2 = Task.builder()
                .title("Task 2")
                .description("Description 2")
                .status(TaskStatus.IN_PROGRESS)
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> tasks = taskRepository.findAll();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }

    @Test
    @DisplayName("Should find tasks by status")
    void testFindByStatus() {
        Task task1 = Task.builder()
                .title("Task 1")
                .description("Description 1")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task task2 = Task.builder()
                .title("Task 2")
                .description("Description 2")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        Task task3 = Task.builder()
                .title("Task 3")
                .description("Description 3")
                .status(TaskStatus.IN_PROGRESS)
                .dueDateTime(LocalDateTime.now().plusDays(21))
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);

        assertNotNull(pendingTasks);
        assertEquals(2, pendingTasks.size());
        assertTrue(pendingTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.PENDING));
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask() {
        Task task = Task.builder()
                .title("Original Title")
                .description("Original Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task savedTask = taskRepository.save(task);

        savedTask.setTitle("Updated Title");
        savedTask.setStatus(TaskStatus.IN_PROGRESS);

        Task updatedTask = taskRepository.save(savedTask);

        assertEquals(savedTask.getId(), updatedTask.getId());
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() {
        Task task = Task.builder()
                .title("Task to Delete")
                .description("Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task savedTask = taskRepository.save(task);
        Long taskId = savedTask.getId();

        taskRepository.deleteById(taskId);

        Optional<Task> deletedTask = taskRepository.findById(taskId);

        assertFalse(deletedTask.isPresent());
    }

    @Test
    @DisplayName("Should check if task exists")
    void testExistsById() {
        Task task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        Task savedTask = taskRepository.save(task);

        assertTrue(taskRepository.existsById(savedTask.getId()));
        assertFalse(taskRepository.existsById(999L));
    }
}
