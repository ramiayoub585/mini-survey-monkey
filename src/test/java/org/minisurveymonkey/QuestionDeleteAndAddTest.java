package org.minisurveymonkey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionEditDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    // Create a question for testing
    private Question question(String text, QuestionType type) {
        Question q = new Question();
        q.setText(text);
        q.setQuestionType(type);
        if (type == QuestionType.NUMBER) {
            q.setMinValue(1);
            q.setMaxValue(10);
        }
        return q;
    }

    // Create and save an open survey holding the given questions
    private Survey surveyWith(Question... questions) {
        Survey survey = new Survey();
        survey.setTitle("Edit Test");
        survey.setActive(true);
        for (Question q : questions) {
            survey.addQuestion(q);
        }
        return surveyRepository.save(survey);
    }

    private String questionUrl(Survey s, long questionId) {
        return "/surveys/" + s.getId() + "/questions/" + questionId;
    }

    // Edit page opens with the existing question filled in
    @Test
    void editFormIsPrefilled() throws Exception {
        Survey s = surveyWith(question("Favourite color?", QuestionType.TEXT));
        Question q = s.getQuestions().get(0);
        mockMvc.perform(get(questionUrl(s, q.getId()) + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attribute("question", q));
    }

    // Saving an edit updates the question and redirects to the questions page
    @Test
    void savesEdit() throws Exception {
        Survey s = surveyWith(question("Old text", QuestionType.TEXT));
        long qid = s.getQuestions().get(0).getId();
        mockMvc.perform(post(questionUrl(s, qid) + "/edit")
                        .param("text", "New text")
                        .param("questionType", "TEXT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/surveys/" + s.getId() + "/questions"));
        assertEquals("New text", surveyRepository.findById(s.getId()).get().getQuestions().get(0).getText());
    }

    // An invalid edit shows an error and leaves the saved question unchanged
    @Test
    void rejectsInvalidEdit() throws Exception {
        Survey s = surveyWith(question("Rate", QuestionType.NUMBER));
        long qid = s.getQuestions().get(0).getId();
        mockMvc.perform(post(questionUrl(s, qid) + "/edit")
                        .param("text", "Rate")
                        .param("questionType", "NUMBER")
                        .param("minValue", "10")
                        .param("maxValue", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attributeExists("error"));
        assertEquals(1, surveyRepository.findById(s.getId()).get().getQuestions().get(0).getMinValue());
    }


    // A question from another survey returns 404 for edit and delete
    @Test
    void wrongSurveyIs404() throws Exception {
        Survey mine = surveyWith(question("mine", QuestionType.TEXT));
        Survey other = surveyWith(question("not mine", QuestionType.TEXT));
        long otherQid = other.getQuestions().get(0).getId();
        mockMvc.perform(get(questionUrl(mine, otherQid) + "/edit"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(questionUrl(mine, otherQid) + "/delete"))
                .andExpect(status().isNotFound());
    }
}