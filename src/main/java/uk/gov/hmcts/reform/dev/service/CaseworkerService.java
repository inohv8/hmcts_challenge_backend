package uk.gov.hmcts.reform.dev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.dto.CaseworkerResponse;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.exception.CaseworkerNotFoundException;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing caseworkers.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CaseworkerService {

    private final CaseworkerRepository caseworkerRepository;

    /**
     * Retrieves all caseworkers.
     *
     * @return list of all caseworkers
     */
    @Transactional(readOnly = true)
    public List<CaseworkerResponse> getAllCaseworkers() {
        return caseworkerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a caseworker by ID.
     *
     * @param id the caseworker ID
     * @return the caseworker response
     * @throws CaseworkerNotFoundException if caseworker not found
     */
    @Transactional(readOnly = true)
    public CaseworkerResponse getCaseworkerById(Long id) {
        Caseworker caseworker = caseworkerRepository.findById(id)
                .orElseThrow(() -> new CaseworkerNotFoundException("Caseworker not found with id: " + id));
        return mapToResponse(caseworker);
    }

    /**
     * Maps a Caseworker entity to a CaseworkerResponse DTO.
     *
     * @param caseworker the caseworker entity
     * @return the caseworker response DTO
     */
    private CaseworkerResponse mapToResponse(Caseworker caseworker) {
        return CaseworkerResponse.builder()
                .id(caseworker.getId())
                .firstName(caseworker.getFirstName())
                .lastName(caseworker.getLastName())
                .email(caseworker.getEmail())
                .role(caseworker.getRole())
                .createdAt(caseworker.getCreatedAt())
                .fullName(caseworker.getFullName())
                .build();
    }
}
