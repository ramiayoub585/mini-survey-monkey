package org.minisurveymonkey;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;


public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findBySurvey(Survey survey);
}
