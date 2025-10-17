package uk.gov.hmcts.reform.dev.exception;

/**
 * Exception thrown when a task is not found.
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructs a new TaskNotFoundException with the specified task ID.
     *
     * @param id the ID of the task that was not found
     */
    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
