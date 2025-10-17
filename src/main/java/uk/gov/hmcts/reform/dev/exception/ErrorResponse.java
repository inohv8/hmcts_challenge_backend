package uk.gov.hmcts.reform.dev.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error response structure for API exceptions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> details;
    private String path;
}
