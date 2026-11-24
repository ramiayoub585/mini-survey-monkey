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
    private final SurveyResponseRepository responseRepository;

    public QuestionController(SurveyRepository surveyRepository, SurveyResponseRepository responseRepository){
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
    }

    // Locates the survey based on ID, error 404 if the survey is not found.
    private Survey findSurvey(long surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
    }

    // Shows all questions in the survey and the add questions form.
    @GetMapping("/surveys/{surveyId}/questions")
    public String showQuestions(@PathVariable("surveyId") long surveyId, Model model ){
        Survey survey = findSurvey(surveyId);
        model.addAttribute("survey",survey);
        model.addAttribute("locked", lockReason(survey));
        model.addAttribute("question",new Question());
        model.addAttribute("questionTypes",QuestionType.values());
        return "questions";
    }

    // Adds a question(s) to the survey and saves it.
    @PostMapping("/surveys/{surveyId}/questions")
    public String addQuestion(@PathVariable("surveyId") long surveyId, @ModelAttribute Question question,
                              @RequestParam(name = "optionsText", defaultValue = "") String optionsText, Model model){
        Survey survey = findSurvey(surveyId);
        requireUnlocked(survey);
        if (question.getText() != null){
            question.setText(question.getText().trim());
        }

        String error = null;
        if(question.getType() == QuestionType.CHOICE){
            // Split on commas and new lines, trim, drop blanks, reject duplicates, need at least 2
            try {
                question.getOptions().addAll(OptionParser.parse(optionsText));
            } catch (IllegalArgumentException e) {
                error = e.getMessage();
            }
        }
        if(question.getType() != QuestionType.NUMBER){
            question.setMaxValue(null);
            question.setMinValue(null);
        }
        if (error == null){
            error = question.validate();
        }
        if (error != null){
            //Show page again with the error displayed (what the user typed is kept)
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

    /**
     * shows the edit page for a question with its current values filled in.
     * @param surveyId ID of the survey that holds the question.
     * @param questionId ID of the question being edited.
     * @param model Model that holds the attributes for the page.
     * @return The edit question page.
     */
    @GetMapping("/surveys/{surveyId}/questions/{questionId}/edit")
    public String editForm(@PathVariable long surveyId, @PathVariable long questionId, Model model){
        Survey survey = findSurvey(surveyId);
        requireUnlocked(survey);
        Question q = findQuestion(survey,questionId);
        model.addAttribute("survey", survey);
        model.addAttribute("question",q);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("optionsText", String.join("\n",q.getOptions()));
        model.addAttribute("questionId", questionId);
        return "edit-question";
    }
    /**
     * saves the changes to a question and validates using the same validation as adding a question.
     * if the edit is invalid, the edit page is shown again with the error and the saved question isn't changed.
     * @param surveyId ID of the survey that holds the question.
     * @param questionId ID of the question being edited.
     * @param form the submitted values of the question.
     * @param optionsText the options typed in, separated by commas or new lines.
     * @param model model that holds the attributes for the page.
     * @return redirect to the questions page, or the edit page again if there is an error.
     */
    @PostMapping("/surveys/{surveyId}/questions/{questionId}/edit")
    public String editQuestions(@PathVariable long surveyId, @PathVariable long questionId, @ModelAttribute("question") Question form,
                                @RequestParam(name = "optionsText", defaultValue ="") String optionsText, Model model){
        Survey survey = findSurvey(surveyId);
        requireUnlocked(survey);
        Question q = findQuestion(survey,questionId);
        //Validating copy first, so bad edit does not change the saved question
        Question checking = new Question();

        String error;
        try {
            applyForm(checking, form, optionsText);
            error = checking.validate();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }
        if(error!= null){
            model.addAttribute("survey", survey);
            model.addAttribute("question",form);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("optionsText", optionsText);
            model.addAttribute("error", error);
            model.addAttribute("questionId", questionId);
            return "edit-question";
        }
        applyForm(q,form,optionsText);
        surveyRepository.save(survey);
        return "redirect:/surveys/" + surveyId + "/questions";
    }
    /**
     * deletes a question from the survey and keeps the order of the other questions.
     * @param surveyId ID of the survey that holds the question.
     * @param questionId ID of the question being deleted.
     * @return redirect to the questions page.
     */
    @PostMapping("/surveys/{surveyId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable long surveyId, @PathVariable long questionId){
        Survey survey = findSurvey(surveyId);
        requireUnlocked(survey);
        survey.removeQuestion(findQuestion(survey,questionId));
        surveyRepository.save(survey);
        return "redirect:/surveys/" + surveyId + "/questions";
    }



    /**
     * Helper method to find a question given the ID.
     * @param survey Survey that page that holds the question.
     * @param questionID ID of question
     * @return The question that has same ID as questionID.
     */
    private Question findQuestion(Survey survey, long questionID){
        for (Question q : survey.getQuestions()){
            if(q.getId()== questionID){
                return q;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
    }
    /**
     * helper method that copies the submitted values onto a question and cleans them up.
     * @param target question that gets the new values.
     * @param form the submitted values of the question.
     * @param optionsText the options typed in, separated by commas or new lines.
     * @throws IllegalArgumentException If the options are not valid.
     */
    private void applyForm(Question target, Question form, String optionsText){
        target.setText(form.getText() == null ? null : form.getText().trim());
        target.setQuestionType(form.getQuestionType());
        target.setMinValue((form.getMinValue()));
        target.setMaxValue(form.getMaxValue());
        target.getOptions().clear();
        if(target.getType() == QuestionType.CHOICE){
            target.getOptions().addAll(OptionParser.parse(optionsText));
        }
        if(target.getType() != QuestionType.NUMBER){
            target.setMaxValue(null) ;
            target.setMinValue(null);
        }
    }

    /**
     * Returns the reason why the survey may be locked
     * @param survey Survey that may be locked.
     * @return Explanation of why it is locked/
     */
    private String lockReason(Survey survey){
        if(!survey.isActive())return "Survey is closed, questions cannot be changed";
        if(responseRepository.existsBySurvey(survey))return "The survey already has responses, questions cannot be changed";
        return null;
    }
    /**
     * stops the request with error if the survey's questions can't be changed.
     * @param survey Survey that may be locked.
     */
    private void requireUnlocked(Survey survey){
        String locked = lockReason(survey);
        if (locked != null) throw new ResponseStatusException( HttpStatus.FORBIDDEN, locked);
    }



}
