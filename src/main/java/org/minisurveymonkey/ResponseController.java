package org.minisurveymonkey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * The controller that handles the page where the respondents answer questions and how it gets the results of the questions.
 */
@Controller
public class ResponseController {
    private static final int MAX_TEXT_LENGTH = 255;
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
        model.addAttribute("answers", new HashMap<String, String>());
        model.addAttribute("errors", new HashMap<Long, String>());
        return "fill";
    }

    // Confirm that the data gets received
    @PostMapping("/surveys/{id}/fill")
    public String submitSurvey(@PathVariable Long id,
                               @RequestParam Map<String, String> params,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Survey fill = surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!fill.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey is closed");
        }

        // Store validation errors by question ID
        Map<Long, String> errors = new LinkedHashMap<>();

        // Validate every question belonging to this survey
        for (Question question : fill.getQuestions()) {
            String key = "q_" + question.getId();
            String value = params.get(key);

            // All questions are required
            if (value == null || value.isBlank()) {
                errors.put(question.getId(), "This question is required.");
                continue;
            }

            if (question.getQuestionType() == null) {
                errors.put(question.getId(), "Invalid question type.");
                continue;
            }

            // Check question based on type
            switch (question.getQuestionType()) {
                case TEXT:
                    if (value.length() > MAX_TEXT_LENGTH) {
                        errors.put(question.getId(), "Text must be " + MAX_TEXT_LENGTH + " characters or fewer.");
                    }
                    break;

                case NUMBER:
                    try {
                        int number = Integer.parseInt(value);

                        Integer min = question.getMinValue();
                        Integer max = question.getMaxValue();

                        if (min == null || max == null) {
                            errors.put(question.getId(), "This question has an invalid range.");
                        } else if (number < min || number > max) {
                            errors.put(question.getId(), "Enter a number between " + min + " and " + max + ".");
                        }

                    } catch (NumberFormatException e) {
                        errors.put(question.getId(), "Please enter a valid whole number.");
                    }
                    break;

                case CHOICE:
                    if (question.getOptions() == null || !question.getOptions().contains(value)) {
                        errors.put(question.getId(), "Please select a valid option.");
                    }
                    break;

                default:
                    errors.put(question.getId(), "Unsupported question type.");
            }
        }

        // If validation fails, show the form again.
        // Do not save any responses.
        if (!errors.isEmpty()) {
            model.addAttribute("fill", fill);
            model.addAttribute("errors", errors);
            model.addAttribute("answers", params);

            return "fill";
        }

        // If all answers are valid create one complete response
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setSurvey(fill);

        for (Question question : fill.getQuestions()) {
            String key = "q_" + question.getId();
            String value = params.get(key);
            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setAnswer(value);
            surveyResponse.addAnswer(answer);
        }

        // Save survey
        surveyResponseRepository.save(surveyResponse);
        redirectAttributes.addFlashAttribute("submissionSuccess", true);

        return "redirect:/surveys/" + id + "/confirmation";
    }

    // Show confirmation after successful submission
    @GetMapping("/surveys/{id}/confirmation")
    public String confirmation(@PathVariable Long id, Model model) {
        Survey survey = surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Prevent direct access without a successful submission
        if (!Boolean.TRUE.equals(model.getAttribute("submissionSuccess"))) {
            return "redirect:/respondent";
        }

        model.addAttribute("survey", survey);
        return "confirmation";
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
