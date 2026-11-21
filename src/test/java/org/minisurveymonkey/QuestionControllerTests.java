package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QuestionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    // Test that a TEXT question is saved with the correct text and type
    @Test
    void addTextQuestion() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository, "Text Question Test");

        // Create TEXT question
        mockMvc.perform(post("/surveys/" + survey.getId() + "/questions").param("text", "What is your name?").param("questionType", "TEXT"))
                .andExpect(status().is3xxRedirection());

        Survey saved = surveyRepository.findById(survey.getId()).orElseThrow();
        assertEquals(1, saved.getQuestions().size());                                   // assert 1 question
        assertEquals("What is your name?", saved.getQuestions().get(0).getText());      // assert text matches
        assertEquals(QuestionType.TEXT, saved.getQuestions().get(0).getQuestionType()); // assert type matches
    }

    // Test that a NUMBER question can be created with minimum and maximum values and that those values are saved correctly
    @Test
    void addNumberQuestion() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Number Question Test");

        // Create NUMBER question
        mockMvc.perform(post("/surveys/" + survey.getId() + "/questions")
                        .param("text", "Rate from 1 to 10")
                        .param("questionType", "NUMBER")
                        .param("minValue", "1")
                        .param("maxValue", "10"))
                .andExpect(status().is3xxRedirection());

        Survey saved = surveyRepository.findById(survey.getId()).orElseThrow();
        assertEquals(1, saved.getQuestions().size());
        Question question = saved.getQuestions().get(0);
        assertEquals(QuestionType.NUMBER, question.getQuestionType()); // assert type matches
        assertEquals(1, question.getMinValue());                       // assert min value
        assertEquals(10, question.getMaxValue());                      // assert max value
    }

    // Test that a CHOICE question can be created with options that are saved correctly
    @Test
    void addChoiceQuestion() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Choice Question Test");

        // Create CHOICE question
        mockMvc.perform(post("/surveys/" + survey.getId() + "/questions")
                        .param("text", "Do you like coffee?")
                        .param("questionType", "CHOICE")
                        .param("optionsText", "Yes\nNo"))
                .andExpect(status().is3xxRedirection());

        Survey saved = surveyRepository.findById(survey.getId()).orElseThrow();
        assertEquals(1, saved.getQuestions().size());
        Question question = saved.getQuestions().get(0);
        assertEquals(QuestionType.CHOICE, question.getQuestionType());
        assertEquals(2, question.getOptions().size());
        assertEquals("Yes", question.getOptions().get(0));
        assertEquals("No", question.getOptions().get(1));
    }

    // Test that a numeric question with an invalid range (minimum greater than maximum) is rejected and not added to the survey
    @Test
    void rejectInvalidNumberQuestion() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Invalid Question Test");

        // Create invalid NUMBER question
        mockMvc.perform(post("/surveys/" + survey.getId() + "/questions")
                        .param("text", "Invalid range")
                        .param("questionType", "NUMBER")
                        .param("minValue", "10")
                        .param("maxValue", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attributeExists("error"));

        Survey saved = surveyRepository.findById(survey.getId()).orElseThrow();

        assertEquals(0, saved.getQuestions().size()); // assert number of questions is 0 because question was rejected
    }
}
