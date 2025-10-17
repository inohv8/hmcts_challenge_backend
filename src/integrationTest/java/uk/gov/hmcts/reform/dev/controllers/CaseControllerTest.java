package uk.gov.hmcts.reform.dev.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CaseController.
 */
@WebMvcTest(CaseController.class)
@DisplayName("CaseController Integration Tests")
class CaseControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    @Test
    @DisplayName("Should return example case with 200 response code")
    void shouldReturnExampleCase() throws Exception {
        MvcResult response = mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        assertThat(jsonResponse).isNotEmpty();
        assertThat(jsonResponse).contains("ABC12345");
        assertThat(jsonResponse).contains("Case Title");
        assertThat(jsonResponse).contains("Case Description");
    }

    @Test
    @DisplayName("Should return case with correct structure")
    void shouldReturnCaseWithCorrectStructure() throws Exception {
        mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.caseNumber").value("ABC12345"))
                .andExpect(jsonPath("$.title").value("Case Title"))
                .andExpect(jsonPath("$.description").value("Case Description"))
                .andExpect(jsonPath("$.status").value("Case Status"))
                .andExpect(jsonPath("$.createdDate").exists());
    }

    @Test
    @DisplayName("Should return case with all required fields")
    void shouldReturnCaseWithAllRequiredFields() throws Exception {
        MvcResult response = mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();

        // Verify all fields are present
        assertThat(jsonResponse).contains("\"id\"");
        assertThat(jsonResponse).contains("\"caseNumber\"");
        assertThat(jsonResponse).contains("\"title\"");
        assertThat(jsonResponse).contains("\"description\"");
        assertThat(jsonResponse).contains("\"status\"");
        assertThat(jsonResponse).contains("\"createdDate\"");
    }

    @Test
    @DisplayName("Should return proper content type header")
    void shouldReturnProperContentType() throws Exception {
        mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("Should return case number in correct format")
    void shouldReturnCaseNumberInCorrectFormat() throws Exception {
        mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNumber").value("ABC12345"))
                .andExpect(jsonPath("$.caseNumber").isString());
    }

    @Test
    @DisplayName("Should return numeric ID")
    void shouldReturnNumericId() throws Exception {
        mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Should return created date as timestamp")
    void shouldReturnCreatedDateAsTimestamp() throws Exception {
        mockMvc.perform(get("/get-example-case"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.createdDate").isNotEmpty());
    }
}

