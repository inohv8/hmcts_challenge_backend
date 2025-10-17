package uk.gov.hmcts.reform.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.entities.Task;
import uk.gov.hmcts.reform.dev.entities.Task.TaskStatus;

import java.util.List;

/**
 * Repository interface for Task entity operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by status.
     *
     * @param status the task status
     * @return list of tasks with the given status
     */
    List<Task> findByStatus(TaskStatus status);
}
