package org.minisurveymonkey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * The controller that handles the main page selecting the role of a surveyor or a respondent. It also handles the creating and handling of surveys in the survey page.
 */
@Controller
public class SurveyController {

    private final SurveyRepository surveyRepository;

    public SurveyController(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    // Home page to select the role
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // The surveyor side
    @GetMapping("/surveyor")
    public String surveyor(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "surveyor";
    }

    // The respondent side
    @GetMapping("/respondent")
    public String respondent(Model model) {
        model.addAttribute("surveys", surveyRepository.findByActiveTrue());
        return "respondent";
    }

    // Creating a new survey
    @PostMapping("/surveys")
    public String createSurvey(@RequestParam(defaultValue = "") String title, Model model) {
        String cleaned = title.trim();
        if (cleaned.isEmpty()) {
            // Show the page again with an error, keeping what was typed
            model.addAttribute("surveys", surveyRepository.findAll());
            model.addAttribute("title", title);
            model.addAttribute("error", "Survey title cannot be blank.");
            return "surveyor";
        }
        Survey survey = new Survey();
        survey.setTitle(cleaned);
        survey.setActive(true);
        surveyRepository.save(survey);
        return "redirect:/surveyor";
    }

    // Viewing the survey
    @GetMapping("/surveys/{id}")
    public String viewSurvey(@PathVariable Long id, Model model) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("survey", survey);
        return "survey";
    }

    // Closing the survey
    @PostMapping("/surveys/{id}/close")
    public String closeSurvey(@PathVariable Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        survey.setActive(false);
        surveyRepository.save(survey);
        return "redirect:/surveys/" + id;
    }
}