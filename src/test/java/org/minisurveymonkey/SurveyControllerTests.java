
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
public class SurveyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    // ===== Survey Creation and Listing =====

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
        Survey survey = TestHelpers.createTestSurvey(surveyRepository, "View Test");

        mockMvc.perform(get("/surveys/" + survey.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("survey"))
                .andExpect(model().attributeExists("survey"));
    }

    // Test that the surveyor dashboard loads and provides the list of surveys
    @Test
    void surveyorPageLoads() throws Exception {
        TestHelpers.createTestSurvey(surveyRepository, "Dashboard Test");

        mockMvc.perform(get("/surveyor"))
                .andExpect(status().isOk())
                .andExpect(view().name("surveyor"))
                .andExpect(model().attributeExists("surveys"));
    }

    // Test that a GET request to /respondent returns active surveys but not closed surveys
    @Test
    void respondentPageOnlyShowsOpenSurveys() throws Exception {
        Survey open = TestHelpers.createTestSurvey(surveyRepository, "Open Test");
        Survey closed = TestHelpers.createTestSurvey(surveyRepository, "Closed Test");

        closed.setActive(false);       // close 'Closed Survey'
        surveyRepository.save(closed);

        mockMvc.perform(get("/respondent"))
                .andExpect(status().isOk())
                .andExpect(view().name("respondent"))
                .andExpect(model().attribute("surveys", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(open.getId())))))
                .andExpect(model().attribute("surveys", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(closed.getId()))))));
    }

    // Test that a surveyor can close a survey and its active status changes to false
    @Test
    void closeSurvey() throws Exception {
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Close Test");

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
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Results Test");

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
        Survey survey = TestHelpers.createTestSurvey(surveyRepository,"Open Results Test");

        mockMvc.perform(get("/surveys/" + survey.getId() + "/results")).andExpect(status().isForbidden());
    }
}
