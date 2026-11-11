package org.minisurveymonkey;

/**
 * Database access for survey extending spring JPA.
 */
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByActiveTrue(); // When the survey is open.
}
