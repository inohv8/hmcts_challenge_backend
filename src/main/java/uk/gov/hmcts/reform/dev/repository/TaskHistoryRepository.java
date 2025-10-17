package uk.gov.hmcts.reform.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.entities.TaskHistory;

import java.util.List;

/**
 * Repository interface for TaskHistory entity.
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    /**
     * Find all history entries for a specific task, ordered by change time descending.
     *
     * @param taskId the task ID
     * @return list of history entries ordered by most recent first
     */
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(Long taskId);

    /**
     * Find all history entries for a specific task, ordered by change time ascending.
     *
     * @param taskId the task ID
     * @return list of history entries ordered chronologically
     */
    List<TaskHistory> findByTaskIdOrderByChangedAtAsc(Long taskId);
}
