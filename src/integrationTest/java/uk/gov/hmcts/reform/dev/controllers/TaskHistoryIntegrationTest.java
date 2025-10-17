package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;
import uk.gov.hmcts.reform.dev.repository.TaskHistoryRepository;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Task History / Timeline functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Task History Integration Tests")
class TaskHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CaseworkerRepository caseworkerRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    private Caseworker caseworker1;
    private Caseworker caseworker2;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
        caseworkerRepository.deleteAll();

        caseworker1 = Caseworker.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.test@hmcts.gov.uk")
                .role("Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        caseworker1 = caseworkerRepository.save(caseworker1);

        caseworker2 = Caseworker.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob.test@hmcts.gov.uk")
                .role("Senior Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        caseworker2 = caseworkerRepository.save(caseworker2);
    }

    @Test
    @DisplayName("Should create history entry when task is created")
    void shouldCreateHistoryEntryOnTaskCreation() throws Exception {
        // Given
        TaskRequest request = TaskRequest.builder()
                .title("New Task")
                .description("Task description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        // When - Create task
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(response).get("id").asLong();

        // Then - Check timeline
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].changeType").value("CREATED"))
                .andExpect(jsonPath("$[0].newValue").value("PENDING"))
                .andExpect(jsonPath("$[0].description").value("Task 'New Task' created"));
    }

    @Test
    @DisplayName("Should create history entry when task is assigned")
    void shouldCreateHistoryEntryOnAssignment() throws Exception {
        // Given
        TaskRequest request = TaskRequest.builder()
                .title("Task to Assign")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(5))
                .assignedToId(caseworker1.getId())
                .build();

        // When
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(response).get("id").asLong();

        // Then - Should have 2 entries: CREATED and ASSIGNMENT_CHANGE
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].changeType").value("ASSIGNMENT_CHANGE"))
                .andExpect(jsonPath("$[0].newValue").value("Alice Johnson"))
                .andExpect(jsonPath("$[1].changeType").value("CREATED"));
    }

    @Test
    @DisplayName("Should create history entry when status is updated")
    void shouldCreateHistoryEntryOnStatusUpdate() throws Exception {
        // Given - Create task
        Task task = Task.builder()
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(3))
                .build();
        task = taskRepository.save(task);

        // Clear initial creation history
        taskHistoryRepository.deleteAll();

        // When - Update status
        String statusUpdate = "{\"status\":\"IN_PROGRESS\"}";
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdate))
                .andExpect(status().isOk());

        // Then
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].changeType").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[0].oldValue").value("PENDING"))
                .andExpect(jsonPath("$[0].newValue").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should create history entry when task is reassigned")
    void shouldCreateHistoryEntryOnReassignment() throws Exception {
        // Given - Create task assigned to caseworker1
        Task task = Task.builder()
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(3))
                .assignedTo(caseworker1)
                .build();
        task = taskRepository.save(task);

        // Clear initial history
        taskHistoryRepository.deleteAll();

        // When - Reassign to caseworker2
        TaskRequest updateRequest = TaskRequest.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .dueDateTime(task.getDueDateTime())
                .assignedToId(caseworker2.getId())
                .build();

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].changeType").value("ASSIGNMENT_CHANGE"))
                .andExpect(jsonPath("$[0].oldValue").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].newValue").value("Bob Smith"));
    }

    @Test
    @DisplayName("Should return 404 when requesting timeline for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/999/timeline"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty timeline for task with no history")
    void shouldReturnEmptyTimelineForTaskWithNoHistory() throws Exception {
        // Given - Create task
        Task task = Task.builder()
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.PENDING)
                .dueDateTime(LocalDateTime.now().plusDays(3))
                .build();
        task = taskRepository.save(task);

        // Clear all history
        taskHistoryRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should create history entry when task title is updated")
    void shouldCreateHistoryEntryWhenTitleIsUpdated() throws Exception {
        // Given - Create task
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update task title
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then - Check timeline includes update entry
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].changeType").value("UPDATED"))
                .andExpect(jsonPath("$[0].description").exists());
    }

    @Test
    @DisplayName("Should create history entry when task description is updated")
    void shouldCreateHistoryEntryWhenDescriptionIsUpdated() throws Exception {
        // Given - Create task
        TaskRequest createRequest = TaskRequest.builder()
                .title("Task Title")
                .description("Original Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update task description
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Task Title")
                .description("Updated Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then - Check timeline includes update entry
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].changeType").value("UPDATED"));
    }

    @Test
    @DisplayName("Should create history entry when task due date is updated")
    void shouldCreateHistoryEntryWhenDueDateIsUpdated() throws Exception {
        // Given - Create task
        LocalDateTime originalDueDate = LocalDateTime.now().plusDays(7);
        TaskRequest createRequest = TaskRequest.builder()
                .title("Task Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(originalDueDate)
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update due date
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(14);
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Task Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(newDueDate)
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then - Check timeline includes update entry
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].changeType").value("UPDATED"))
                .andExpect(jsonPath("$[0].changedBy").value("system"));
    }

    @Test
    @DisplayName("Should create history entry when multiple fields are updated")
    void shouldCreateHistoryEntryWhenMultipleFieldsAreUpdated() throws Exception {
        // Given - Create task
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update multiple fields
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then - Check timeline includes update entry
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].changeType").value("UPDATED"))
                .andExpect(jsonPath("$[0].description").isNotEmpty());
    }

    @Test
    @DisplayName("Should create multiple history entries for consecutive updates")
    void shouldCreateMultipleHistoryEntriesForConsecutiveUpdates() throws Exception {
        // Given - Create task
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Perform first update
        TaskRequest update1 = TaskRequest.builder()
                .title("Updated Title 1")
                .description("Description")
                .status("IN_PROGRESS")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk());

        // And - Perform second update
        TaskRequest update2 = TaskRequest.builder()
                .title("Updated Title 2")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk());

        // Then - Check timeline has multiple update entries
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Should order history entries correctly with mixed change types")
    void shouldOrderHistoryEntriesCorrectlyWithMixedChangeTypes() throws Exception {
        // Given - Create task with assignment
        TaskRequest createRequest = TaskRequest.builder()
                .title("Task Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .assignedToId(caseworker1.getId())
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Update status
        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        // And - Update task fields
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .assignedToId(caseworker1.getId())
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then - Check timeline has all entries ordered by most recent first
        mockMvc.perform(get("/api/tasks/" + taskId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].changedAt").exists());
    }
}
