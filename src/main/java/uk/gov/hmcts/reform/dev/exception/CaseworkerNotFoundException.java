package uk.gov.hmcts.reform.dev.exception;

/**
 * Exception thrown when a caseworker is not found.
 */
public class CaseworkerNotFoundException extends RuntimeException {

    /**
     * Constructs a new CaseworkerNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public CaseworkerNotFoundException(String message) {
        super(message);
    }
}
