package org.minisurveymonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dev-only sample data: seeds one OPEN survey and one CLOSED survey, each with
 * a TEXT, NUMBER and CHOICE question, so the fill-survey flow can be tested
 * without creating surveys by hand. Runs only with the "dev" profile.
 */
@Component
@Profile("dev")
public class DevSampleSurveys implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevSampleSurveys.class);
    private final SurveyRepository surveyRepository;

    public DevSampleSurveys(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public void run(String... args) {
        Survey open = buildSurvey("Coffee Habits (open)", true);
        Survey closed = buildSurvey("Old Feedback Survey (closed)", false);

        open = surveyRepository.save(open);
        closed = surveyRepository.save(closed);

        logger.info("Open sample survey:   http://localhost:8080/surveys/{}/fill", open.getId());
        logger.info("Closed sample survey: http://localhost:8080/surveys/{}/fill", closed.getId());
    }

    private Survey buildSurvey(String title, boolean active) {
        Survey survey = new Survey();
        survey.setTitle(title);
        survey.setActive(active);

        Question text = new Question();
        text.setQuestionType(QuestionType.TEXT);
        text.setText("What's your favourite drink?");
        survey.addQuestion(text);

        Question number = new Question();
        number.setQuestionType(QuestionType.NUMBER);
        number.setText("How many cups do you drink per day?");
        number.setMinValue(0);
        number.setMaxValue(10);
        survey.addQuestion(number);

        Question choice = new Question();
        choice.setQuestionType(QuestionType.CHOICE);
        choice.setText("When do you usually drink it?");
        choice.getOptions().addAll(List.of("Morning", "Afternoon", "Evening"));
        survey.addQuestion(choice);

        return survey;
    }
}
