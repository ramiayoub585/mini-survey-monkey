package org.minisurveymonkey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller that handles the page where the respondents answer questions and how it gets the results of the questions.
 */
@Controller
public class ResponseController {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository  surveyResponseRepository;

    public ResponseController(SurveyRepository surveyRepository, SurveyResponseRepository surveyResponseRepository) {
        this.surveyRepository = surveyRepository;
        this.surveyResponseRepository = surveyResponseRepository;
    }

    // Make a new template that shows the survey title and lists each question’s text
    @GetMapping("/surveys/{id}/fill")
    public String takeSurvey(@PathVariable Long id, Model model) {
        Survey fill = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!fill.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey is closed");
        }
        model.addAttribute("fill", fill);
        return "fill";
    }

    // Confirm that the data gets received
    @PostMapping("/surveys/{id}/fill")
    public String submitSurvey(@PathVariable Long id,
                               @RequestParam Map<String, String> params) {
        Survey fill = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!fill.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey is closed");
        }

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setSurvey(fill);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();      // e.g. "q_2"
            String value = entry.getValue();  // e.g. "10"

            // 1. skip anything that isn't an answer
            if (!key.startsWith("q_")) continue;

            // 2. "q_2" → 2L
            Long questionId = Long.parseLong(key.substring(2));

            // 3. find that question in THIS survey
            // loop over fill.getQuestions(), match on getId()
            for (Question question : fill.getQuestions()) {
                if (questionId.equals(question.getId())) {
                    Answer answer = new Answer();
                    answer.setQuestion(question);
                    answer.setAnswer(value);
                    surveyResponse.addAnswer(answer);
                }
            }
        }

        SurveyResponse saved = surveyResponseRepository.save(surveyResponse);
        return "redirect:/respondent";
    }

    // Get the results of the respondents once the survey is closed.
    @GetMapping("/surveys/{id}/results")
    public String viewResults(@PathVariable Long id, Model model) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (survey.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey is still open");
        }

        List<SurveyResponse> responses = surveyResponseRepository.findBySurvey(survey);

        Map<Long, List<String>> answersByQuestion = new HashMap<>();
        for (SurveyResponse response : responses) {
            for (Answer answer : response.getAnswers()) {
                answersByQuestion.computeIfAbsent(answer.getQuestion().getId(), k -> new ArrayList<>())
                        .add(answer.getAnswer());
            }
        }

        List<QuestionResult> results = new ArrayList<>();
        for (Question question : survey.getQuestions()) {
            results.add(new QuestionResult(question, answersByQuestion.getOrDefault(question.getId(), List.of())));
        }

        model.addAttribute("survey", survey);
        model.addAttribute("responseCount", responses.size());
        model.addAttribute("results", results);
        return "results";
    }
}
