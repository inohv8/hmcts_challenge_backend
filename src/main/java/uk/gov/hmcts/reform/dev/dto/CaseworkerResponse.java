package uk.gov.hmcts.reform.dev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Caseworker.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseworkerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private String fullName;
}
