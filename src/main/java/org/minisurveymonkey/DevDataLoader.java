package org.minisurveymonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevDataLoader.class);
    private final SurveyRepository surveyRepository;

    public DevDataLoader(SurveyRepository surveyRepository){
        this.surveyRepository =surveyRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        Survey survey = new Survey();
        survey.setTitle("Sample Survey");
        survey.setActive(true);
        survey = surveyRepository.save(survey);
        logger.info("Dev survey ready: http://localhost:8080/surveys/{}/questions", survey.getId());

    }
}
