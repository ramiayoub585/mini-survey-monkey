
package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SurveyMonkeyApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyResponseRepository responseRepository;

    // Create a survey for testing
    private Survey createTestSurvey(String title) {
        Survey survey = new Survey();
        survey.setTitle(title);
        survey.setActive(true);
        return surveyRepository.save(survey);
    }

    // Create a question for testing
    private Question createTestQuestion(String text, QuestionType type) {
        Question question = new Question();
        question.setText(text);
        question.setQuestionType(type);

        if (type == QuestionType.NUMBER) {
            question.setMinValue(1);
            question.setMaxValue(10);
        }

        if (type == QuestionType.CHOICE) {
            question.getOptions().add("Yes");
            question.getOptions().add("No");
        }

        return question;
    }

    // ===== Test Group 1 - Application Startup =====

    // Test that the Spring Boot application context starts successfully, including its configured components and dependencies
    @Test
    void contextLoads() {
    }

    // Test that a GET request to '/' returns the home view
    @Test
    void homePageLoads() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }

    // ===== Test Group 2 - Survey Creation and Listing =====

    // Test that a POST request creates a new survey, saves it in the database, sets it as active, and redirects to /surveyor
    @Test
    void createSurvey() throws Exception {
        mockMvc.perform(post("/surveys").param("title", "Integration Test Survey")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/surveyor"));
        boolean exists = surveyRepository.findAll().stream().anyMatch(s -> s.getTitle().equals("Integration Test Survey") && s.isActive());

        assertTrue(exists);
    }

    // Test that an existing survey can be viewed by its ID
    @Test
    void viewSurvey() throws Exception {
        Survey survey = createTestSurvey("View Test");

        mockMvc.perform(get("/surveys/" + survey.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("survey"))
                .andExpect(model().attributeExists("survey"));
    }

    // Test that the surveyor dashboard loads and provides the list of surveys
    @Test
    void surveyorPageLoads() throws Exception {
        createTestSurvey("Dashboard Test");

        mockMvc.perform(get("/surveyor"))
                .andExpect(status().isOk())
                .andExpect(view().name("surveyor"))
                .andExpect(model().attributeExists("surveys"));
    }

    // Test that a GET request to /respondent returns active surveys but not closed surveys
    @Test
    void respondentPageOnlyShowsOpenSurveys() throws Exception {
        Survey open = createTestSurvey("Open Survey");
        Survey closed = createTestSurvey("Closed Survey");

        closed.setActive(false);       // close 'Closed Survey'
        surveyRepository.save(closed);

        mockMvc.perform(get("/respondent"))
                .andExpect(status().isOk())
                .andExpect(view().name("respondent"))
                .andExpect(model().attribute("surveys", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(open.getId())))))
                .andExpect(model().attribute("surveys", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(closed.getId()))))));
    }

    // ===== Test Group 3 - Add Questions =====

    // Test that a TEXT question is saved with the correct text and type
    @Test
    void addTextQuestion() throws Exception {
        Survey survey = createTestSurvey("Text Question Test");

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
        Survey survey = createTestSurvey("Number Question Test");

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
        Survey survey = createTestSurvey("Choice Question Test");

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
        Survey survey = createTestSurvey("Invalid Question Test");

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

    // ===== Test Group 4 - Fill Out Surveys =====

    // Test that an active survey's response form can be accessed successfully
    @Test
    void openSurveyCanBeFilled() throws Exception {
        Survey survey = createTestSurvey("Fill Test");

        mockMvc.perform(get("/surveys/" + survey.getId() + "/fill"))
                .andExpect(status().isOk())
                .andExpect(view().name("fill"))
                .andExpect(model().attributeExists("fill"));
    }

    // Test than a respondent can submit an answer which is saved to the database and maps to the right survey and question
    @Test
    void submitSurveySavesAnswers() throws Exception {
        // Setup test
        Survey survey = createTestSurvey("Response Test");
        Question question = createTestQuestion("What is your name?", QuestionType.TEXT);
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
        Survey survey = createTestSurvey("Closed Submission Test");

        survey.setActive(false);
        surveyRepository.save(survey);

        long before = responseRepository.count();
        mockMvc.perform(post("/surveys/" + survey.getId() + "/fill")).andExpect(status().isForbidden());
        assertEquals(before, responseRepository.count()); // assert no new response was saved
    }

    // ===== Test Group 5 - Close Surveys and View Results =====

    // Test that a surveyor can close a survey and its active status changes to false
    @Test
    void closeSurvey() throws Exception {
        Survey survey = createTestSurvey("Close Test");

        // Close survey
        mockMvc.perform(post("/surveys/" + survey.getId() + "/close"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/surveys/" + survey.getId()));

        Survey saved = surveyRepository.findById(survey.getId()).orElseThrow();
        assertFalse(saved.isActive()); // assert status is False
    }

    // Test that the results page for a closed survey loads the results and response count
    @Test
    void closedSurveyResultsCanBeViewed() throws Exception {
        Survey survey = createTestSurvey("Results Test");

        survey.setActive(false); // close survey
        surveyRepository.save(survey);

        // Load survey results and response count
        mockMvc.perform(get("/surveys/" + survey.getId() + "/results"))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attributeExists(
                        "survey", "results", "responseCount"));
    }

    // Test that viewing results for an active survey returns HTTP 403 Forbidden
    @Test
    void openSurveyResultsCannotBeViewed() throws Exception {
        Survey survey = createTestSurvey("Open Results Test");

        mockMvc.perform(get("/surveys/" + survey.getId() + "/results")).andExpect(status().isForbidden());
    }

    // ===== Test Group 6 - Error Handling =====

    // Test that requesting a survey ID that doesn't exist returns HTTP 404 Not Found
    @Test
    void nonexistentSurveyReturns404() throws Exception {
        mockMvc.perform(get("/surveys/999999")).andExpect(status().isNotFound());
    }
}
