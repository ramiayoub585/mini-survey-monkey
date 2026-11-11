package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

/**
 * The response of the respondent to a question on a survey.
 */
@Entity
public class SurveyResponse {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Survey survey; // The survey that was answered

    @OneToMany (cascade = CascadeType.ALL)
    private List<Answer> answers; // The answers given by respondent

    public SurveyResponse() {
        answers = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public Survey getSurvey() {
        return survey;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void addAnswer(Answer a){
        answers.add(a);
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }
}
