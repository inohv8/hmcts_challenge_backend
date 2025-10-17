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
import uk.gov.hmcts.reform.dev.dto.TaskStatusUpdateRequest;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TaskController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Integration Test Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should fail to create task with past due date")
    void testCreateTaskWithPastDueDate() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Invalid Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().minusDays(1))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should fail to create task without required fields")
    void testCreateTaskWithoutRequiredFields() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .description("Test Description")
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById() throws Exception {
        // Create a task first
        TaskRequest request = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Get the task
        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void testGetTaskByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task Not Found"));
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void testGetAllTasks() throws Exception {
        // Create multiple tasks
        TaskRequest request1 = TaskRequest.builder()
                .title("Task 1")
                .description("Description 1")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        TaskRequest request2 = TaskRequest.builder()
                .title("Task 2")
                .description("Description 2")
                .status("IN_PROGRESS")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Get all tasks
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask() throws Exception {
        // Create a task first
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Task")
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

        // Update the task
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status("IN_PROGRESS")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus() throws Exception {
        // Create a task first
        TaskRequest createRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update only the status
        TaskStatusUpdateRequest statusRequest = TaskStatusUpdateRequest.builder()
                .status("COMPLETED")
                .build();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() throws Exception {
        // Create a task first
        TaskRequest request = TaskRequest.builder()
                .title("Task to Delete")
                .description("Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        String createResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete the task
        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent task")
    void testDeleteTaskNotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task Not Found"));
    }

    @Test
    @DisplayName("Should fail to create task with title exceeding 200 characters")
    void testCreateTaskWithTitleExceedingMaxLength() throws Exception {
        // Create a title with 201 characters
        String longTitle = "A".repeat(201);

        TaskRequest request = TaskRequest.builder()
                .title(longTitle)
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("title: Title must not exceed 200 characters"));
    }

    @Test
    @DisplayName("Should create task successfully with title at max length (200 characters)")
    void testCreateTaskWithTitleAtMaxLength() throws Exception {
        // Create a title with exactly 200 characters
        String maxLengthTitle = "A".repeat(200);

        TaskRequest request = TaskRequest.builder()
                .title(maxLengthTitle)
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(maxLengthTitle));
    }

    @Test
    @DisplayName("Should fail to create task with description exceeding 2000 characters")
    void testCreateTaskWithDescriptionExceedingMaxLength() throws Exception {
        // Create a description with 2001 characters
        String longDescription = "B".repeat(2001);

        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .description(longDescription)
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("description: Description must not exceed 2000 characters"));
    }

    @Test
    @DisplayName("Should create task successfully with description at max length (2000 characters)")
    void testCreateTaskWithDescriptionAtMaxLength() throws Exception {
        // Create a description with exactly 2000 characters
        String maxLengthDescription = "B".repeat(2000);

        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .description(maxLengthDescription)
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(maxLengthDescription));
    }

    @Test
    @DisplayName("Should fail to create task with empty title")
    void testCreateTaskWithEmptyTitle() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("title: Title is required"));
    }

    @Test
    @DisplayName("Should fail to create task with whitespace-only title")
    void testCreateTaskWithWhitespaceOnlyTitle() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("   ")
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("title: Title is required"));
    }

    @Test
    @DisplayName("Should fail to create task with null title")
    void testCreateTaskWithNullTitle() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title(null)
                .description("Test Description")
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("title: Title is required"));
    }

    @Test
    @DisplayName("Should create task successfully with null description")
    void testCreateTaskWithNullDescription() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .description(null)
                .status("PENDING")
                .dueDateTime(LocalDateTime.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Valid Title"));
    }

    @Test
    @DisplayName("Should fail to update task with title exceeding 200 characters")
    void testUpdateTaskWithTitleExceedingMaxLength() throws Exception {
        // Create a task first
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Task")
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

        // Try to update with invalid title
        String longTitle = "A".repeat(201);
        TaskRequest updateRequest = TaskRequest.builder()
                .title(longTitle)
                .description("Updated Description")
                .status("IN_PROGRESS")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("title: Title must not exceed 200 characters"));
    }

    @Test
    @DisplayName("Should fail to update task with description exceeding 2000 characters")
    void testUpdateTaskWithDescriptionExceedingMaxLength() throws Exception {
        // Create a task first
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Task")
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

        // Try to update with invalid description
        String longDescription = "B".repeat(2001);
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Valid Title")
                .description(longDescription)
                .status("IN_PROGRESS")
                .dueDateTime(LocalDateTime.now().plusDays(14))
                .build();

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("description: Description must not exceed 2000 characters"));
    }
}
