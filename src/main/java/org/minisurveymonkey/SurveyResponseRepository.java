package org.minisurveymonkey;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * Database access for survey responses extending spring JPA.
 */
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findBySurvey(Survey survey);
}
