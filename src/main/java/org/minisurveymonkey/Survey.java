package org.minisurveymonkey;

import jakarta.persistence.*;

import java.util.*;

/**
 * The survey that is created by the surveyor. When the survey is open the respondants can answer the questions of that survey. When its closed they can no longer answer the survey.
 */
@Entity
public class Survey {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    private boolean active; // The status of the survey as open or close.

    @OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn
    private List<Question> questions; // The questions associated with the survey.

    public Survey() {
        questions = new ArrayList<Question>();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
    }
}
