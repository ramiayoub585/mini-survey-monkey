package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

public class ResponseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyResponseRepository responseRepository;

    // Test that an active survey's response form can be accessed successfully
    @Test
    void openSurveyCanBeFilled() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository, "Fill Test");

        mockMvc.perform(get("/surveys/" + survey.getId() + "/fill"))
                .andExpect(status().isOk())
                .andExpect(view().name("fill"))
                .andExpect(model().attributeExists("fill"));
    }

    // Test that a respondent can submit an answer which is saved to the database and maps to the right survey and question
    @Test
    void submitSurveySavesAnswers() throws Exception {
        // Setup test
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Response Test");
        Question question = TestHelpers.createTestQuestion("What is your name?", QuestionType.TEXT);
        survey.addQuestion(question);
        surveyRepository.saveAndFlush(survey);

        Survey savedSurvey = surveyRepository.findById(survey.getId()).orElseThrow();
        long questionId = savedSurvey.getQuestions().get(0).getId();
        assertTrue(questionId > 0); // assert question was created

        long before = responseRepository.count();
        mockMvc.perform(post("/surveys/" + survey.getId() + "/fill").param("q_" + questionId, "Alice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/respondent"));
        assertEquals(before + 1, responseRepository.count()); // assert response was received

        var responses = responseRepository.findBySurvey(survey);
        assertEquals(1, responses.size());                                       // assert 1 response
        assertEquals(1, responses.get(0).getAnswers().size());                   // assert 1 answer
        assertEquals("Alice", responses.get(0).getAnswers().get(0).getAnswer()); // assert answer matches
    }

    // Test that submitting a response to a closed survey returns HTTP 403 and no new response is saved
    @Test
    void closedSurveyRejectsSubmission() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Closed Submission Test");

        survey.setActive(false);
        surveyRepository.save(survey);

        long before = responseRepository.count();
        mockMvc.perform(post("/surveys/" + survey.getId() + "/fill")).andExpect(status().isForbidden());
        assertEquals(before, responseRepository.count()); // assert no new response was saved
    }
}
