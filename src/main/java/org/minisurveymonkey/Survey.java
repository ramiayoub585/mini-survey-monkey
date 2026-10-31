package org.minisurveymonkey;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Survey {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    private boolean active;

    @OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

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
