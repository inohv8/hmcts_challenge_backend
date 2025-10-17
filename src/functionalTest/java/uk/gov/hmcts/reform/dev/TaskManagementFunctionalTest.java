package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Functional tests for Task Management API.
 * These tests verify the end-to-end functionality of the API.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Task Management Functional Tests")
class TaskManagementFunctionalTest {

    @LocalServerPort
    private int port;

    @Value("${TEST_HOST:http://localhost}")
    private String testHost;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = testHost;
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Should create, retrieve, update, and delete a task")
    void testCompleteTaskLifecycle() {
        // Create task
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("title", "Functional Test Task");
        createRequest.put("description", "Testing complete lifecycle");
        createRequest.put("status", "PENDING");
        createRequest.put(
            "dueDateTime",
            LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/tasks")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("Functional Test Task"))
            .body("status", equalTo("PENDING"))
            .extract().response();

        int taskId = createResponse.path("id");

        // Retrieve task
        given()
            .when()
            .get("/api/tasks/" + taskId)
            .then()
            .statusCode(200)
            .body("id", equalTo(taskId))
            .body("title", equalTo("Functional Test Task"));

        // Update task
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Functional Test Task");
        updateRequest.put("description", "Updated description");
        updateRequest.put("status", "IN_PROGRESS");
        updateRequest.put(
            "dueDateTime",
            LocalDateTime.now().plusDays(45).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/tasks/" + taskId)
            .then()
            .statusCode(200)
            .body("title", equalTo("Updated Functional Test Task"))
            .body("status", equalTo("IN_PROGRESS"));

        // Update status only
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "COMPLETED");

        given()
            .contentType(ContentType.JSON)
            .body(statusUpdate)
            .when()
            .patch("/api/tasks/" + taskId + "/status")
            .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));

        // Delete task
        given()
            .when()
            .delete("/api/tasks/" + taskId)
            .then()
            .statusCode(204);

        // Verify deletion
        given()
            .when()
            .get("/api/tasks/" + taskId)
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should get all tasks")
    void testGetAllTasks() {
        Response response = given()
            .when()
            .get("/api/tasks")
            .then()
            .statusCode(200)
            .extract().response();

        Assertions.assertNotNull(response.body());
    }

    @Test
    @DisplayName("Should validate task creation with invalid data")
    void testCreateTaskValidation() {
        // Test without required fields
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("description", "Missing required fields");

        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
            .when()
            .post("/api/tasks")
            .then()
            .statusCode(400)
            .body("error", equalTo("Validation Failed"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent task")
    void testGetNonExistentTask() {
        given()
            .when()
            .get("/api/tasks/99999")
            .then()
            .statusCode(404)
            .body("error", equalTo("Task Not Found"));
    }

    @Test
    @DisplayName("Should verify API documentation is accessible")
    void testSwaggerUIAccessible() {
        given()
            .when()
            .get("/swagger-ui.html")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should verify OpenAPI docs are accessible")
    void testOpenAPIDocsAccessible() {
        given()
            .when()
            .get("/v3/api-docs")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }
}
