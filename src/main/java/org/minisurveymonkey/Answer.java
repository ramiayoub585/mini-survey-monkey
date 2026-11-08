package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Answer {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Question question;

    private String answer;

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
