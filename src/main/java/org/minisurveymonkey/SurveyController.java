package org.minisurveymonkey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SurveyController {

    private final SurveyRepository surveyRepository;

    public SurveyController(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    // Role chooser
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Surveyor dashboard
    @GetMapping("/surveyor")
    public String surveyor(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "surveyor";
    }

    // Respondent page: open surveys only
    @GetMapping("/respondent")
    public String respondent(Model model) {
        model.addAttribute("surveys", surveyRepository.findByActiveTrue());
        return "respondent";
    }

    @PostMapping("/surveys")
    public String createSurvey(@RequestParam String title) {
        Survey survey = new Survey();
        survey.setTitle(title.trim());
        survey.setActive(true);            // new surveys start open
        surveyRepository.save(survey);
        return "redirect:/surveyor";
    }

    @GetMapping("/surveys/{id}")
    public String viewSurvey(@PathVariable Long id, Model model) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("survey", survey);
        return "survey";
    }

    @PostMapping("/surveys/{id}/close")
    public String closeSurvey(@PathVariable Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        survey.setActive(false);
        surveyRepository.save(survey);
        return "redirect:/surveys/" + id;
    }
}