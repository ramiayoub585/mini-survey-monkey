package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class SurveyResponse {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Survey survey;

    @OneToMany (cascade = CascadeType.ALL)
    private List<Answer> answers;

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
}
