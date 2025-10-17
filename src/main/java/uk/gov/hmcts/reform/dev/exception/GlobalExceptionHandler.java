package uk.gov.hmcts.reform.dev.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle TaskNotFoundException.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(
            TaskNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Task Not Found")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle CaseworkerNotFoundException.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(CaseworkerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaseworkerNotFoundException(
            CaseworkerNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Caseworker Not Found")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation errors.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .details(errors)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
