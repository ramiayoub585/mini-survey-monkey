package org.minisurveymonkey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Question Controller for handling the page where a surveyor add questions for the respondents.
 */
@Controller
public class QuestionController {
    private final SurveyRepository surveyRepository;

    public QuestionController(SurveyRepository surveyRepository){
        this.surveyRepository = surveyRepository;
    }
    // Locates the survey based on ID, error 404 if the survey is not found.
    private Survey findSurvey(long surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
    }

    // Shows all questions in the survey and the add questions form.
    @GetMapping("/surveys/{surveyId}/questions")
    public String showQuestions(@PathVariable("surveyId") long surveyId, Model model ){
        model.addAttribute("survey",findSurvey(surveyId));
        model.addAttribute("question",new Question());
        model.addAttribute("questionTypes",QuestionType.values());
        return "questions";
    }
    // Adds a question(s) to the survey and saves it.
    @PostMapping("/surveys/{surveyId}/questions")
    public String addQuestion(@PathVariable("surveyId") long surveyId, @ModelAttribute Question question,
                              @RequestParam(name = "optionsText", defaultValue = "") String optionsText, Model model){
        Survey survey = findSurvey(surveyId);

        if (question.getText() != null){
            question.setText(question.getText().trim());
        }
        if(question.getType() == QuestionType.CHOICE){
            for(String line: optionsText.split("\\R")){
                if(!line.isBlank()){
                    question.getOptions().add(line.trim());
                }
            }
        }
        if(question.getType() != QuestionType.NUMBER){
            question.setMaxValue(null);
            question.setMinValue(null);
        }
        String error = question.validate();
        if (error != null){
            //Show page again with the error displayed
            model.addAttribute("survey",survey);
            model.addAttribute("question", question);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("optionsText", optionsText);
            model.addAttribute("error", error);
            return "questions";
        }
        survey.addQuestion(question);
        surveyRepository.save(survey);
        return "redirect:/surveys/" + surveyId + "/questions";
    }

}
