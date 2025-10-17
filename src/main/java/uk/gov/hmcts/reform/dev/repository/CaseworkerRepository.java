package uk.gov.hmcts.reform.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.entities.Caseworker;

import java.util.Optional;

/**
 * Repository interface for Caseworker entity.
 */
@Repository
public interface CaseworkerRepository extends JpaRepository<Caseworker, Long> {

    /**
     * Find a caseworker by email.
     *
     * @param email the email address
     * @return an Optional containing the caseworker if found
     */
    Optional<Caseworker> findByEmail(String email);
}
