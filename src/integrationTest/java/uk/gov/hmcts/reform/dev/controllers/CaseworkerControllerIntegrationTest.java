package uk.gov.hmcts.reform.dev.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CaseworkerController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CaseworkerController Integration Tests")
class CaseworkerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaseworkerRepository caseworkerRepository;

    @BeforeEach
    void setUp() {
        caseworkerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should retrieve all caseworkers successfully")
    void shouldRetrieveAllCaseworkers() throws Exception {
        // Given
        Caseworker caseworker1 = Caseworker.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.johnson@hmcts.gov.uk")
                .role("Senior Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        Caseworker caseworker2 = Caseworker.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob.smith@hmcts.gov.uk")
                .role("Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        caseworkerRepository.save(caseworker1);
        caseworkerRepository.save(caseworker2);

        // When & Then
        mockMvc.perform(get("/api/caseworkers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].lastName").value("Johnson"))
                .andExpect(jsonPath("$[0].email").value("alice.johnson@hmcts.gov.uk"))
                .andExpect(jsonPath("$[0].role").value("Senior Caseworker"))
                .andExpect(jsonPath("$[0].fullName").value("Alice Johnson"))
                .andExpect(jsonPath("$[1].firstName").value("Bob"))
                .andExpect(jsonPath("$[1].fullName").value("Bob Smith"));
    }

    @Test
    @DisplayName("Should retrieve empty list when no caseworkers exist")
    void shouldRetrieveEmptyListWhenNoCaseworkersExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/caseworkers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should retrieve caseworker by ID successfully")
    void shouldRetrieveCaseworkerById() throws Exception {
        // Given
        Caseworker caseworker = Caseworker.builder()
                .firstName("Carol")
                .lastName("Williams")
                .email("carol.williams@hmcts.gov.uk")
                .role("Lead Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        Caseworker saved = caseworkerRepository.save(caseworker);

        // When & Then
        mockMvc.perform(get("/api/caseworkers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Carol"))
                .andExpect(jsonPath("$.lastName").value("Williams"))
                .andExpect(jsonPath("$.email").value("carol.williams@hmcts.gov.uk"))
                .andExpect(jsonPath("$.role").value("Lead Caseworker"))
                .andExpect(jsonPath("$.fullName").value("Carol Williams"));
    }

    @Test
    @DisplayName("Should return 404 when caseworker not found by ID")
    void shouldReturn404WhenCaseworkerNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/caseworkers/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("Caseworker Not Found"))
                .andExpect(jsonPath("$.details[0]").value("Caseworker not found with id: 999"));
    }
}
