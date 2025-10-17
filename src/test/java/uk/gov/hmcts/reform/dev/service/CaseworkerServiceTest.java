package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.CaseworkerResponse;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.exception.CaseworkerNotFoundException;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CaseworkerService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaseworkerService Unit Tests")
class CaseworkerServiceTest {

    @Mock
    private CaseworkerRepository caseworkerRepository;

    @InjectMocks
    private CaseworkerService caseworkerService;

    private Caseworker testCaseworker;

    @BeforeEach
    void setUp() {
        testCaseworker = Caseworker.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@hmcts.gov.uk")
                .role("Senior Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve all caseworkers successfully")
    void shouldRetrieveAllCaseworkers() {
        // Given
        Caseworker caseworker2 = Caseworker.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@hmcts.gov.uk")
                .role("Caseworker")
                .createdAt(LocalDateTime.now())
                .build();
        List<Caseworker> caseworkers = Arrays.asList(testCaseworker, caseworker2);
        when(caseworkerRepository.findAll()).thenReturn(caseworkers);

        // When
        List<CaseworkerResponse> result = caseworkerService.getAllCaseworkers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(caseworkerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve caseworker by ID successfully")
    void shouldRetrieveCaseworkerById() {
        // Given
        when(caseworkerRepository.findById(1L)).thenReturn(Optional.of(testCaseworker));

        // When
        CaseworkerResponse result = caseworkerService.getCaseworkerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@hmcts.gov.uk", result.getEmail());
        assertEquals("Senior Caseworker", result.getRole());
        assertEquals("John Doe", result.getFullName());
        verify(caseworkerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw CaseworkerNotFoundException when caseworker not found")
    void shouldThrowExceptionWhenCaseworkerNotFound() {
        // Given
        when(caseworkerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        CaseworkerNotFoundException exception = assertThrows(
                CaseworkerNotFoundException.class,
                () -> caseworkerService.getCaseworkerById(999L)
        );
        assertEquals("Caseworker not found with id: 999", exception.getMessage());
        verify(caseworkerRepository, times(1)).findById(999L);
    }
}
