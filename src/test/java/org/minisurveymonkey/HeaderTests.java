package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HeaderTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    private Survey saveSurvey(boolean active) {
        Survey survey = new Survey();
        survey.setTitle("Header Test Survey");
        survey.setActive(active);
        return surveyRepository.save(survey);
    }

    private void assertHeader(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<header>")))
                .andExpect(content().string(containsString("href=\"/\"")))
                .andExpect(content().string(containsString("href=\"/surveyor\"")))
                .andExpect(content().string(containsString("href=\"/respondent\"")));
    }

    @Test
    void everyPageShowsHeader() throws Exception {
        Survey open = saveSurvey(true);
        Survey closed = saveSurvey(false);

        assertHeader("/");
        assertHeader("/surveyor");
        assertHeader("/respondent");
        assertHeader("/surveys/" + open.getId());
        assertHeader("/surveys/" + open.getId() + "/questions");
        assertHeader("/surveys/" + open.getId() + "/fill");
        assertHeader("/surveys/" + closed.getId() + "/results");
    }

    @Test
    void headerMarksCurrentSection() throws Exception {
        mockMvc.perform(get("/surveyor"))
                .andExpect(content().string(containsString("href=\"/surveyor\" aria-current=\"page\"")));
        mockMvc.perform(get("/respondent"))
                .andExpect(content().string(containsString("href=\"/respondent\" aria-current=\"page\"")));
        mockMvc.perform(get("/"))
                .andExpect(content().string(not(containsString("aria-current"))));
    }

    @Test
    void fillPageDoesNotLinkToSurveyorPage() throws Exception {
        Survey open = saveSurvey(true);

        mockMvc.perform(get("/surveys/" + open.getId() + "/fill"))
                .andExpect(content().string(not(containsString("href=\"/surveys/" + open.getId() + "\""))));
    }
}
