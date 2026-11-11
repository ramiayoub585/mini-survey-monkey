package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

/**
 * The answer to a question, where a respondent answers the surveyors question(s) and the surveyor can generate
 * a result of the answers once the survey has been closed.
 */

@Entity
public class Answer {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Question question; // The question type to the answer

    private String answer; // The answer to the question

    public long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setQuestion(Question question) {this.question = question;}
    public void setAnswer(String answer) {this.answer = answer;}

}
