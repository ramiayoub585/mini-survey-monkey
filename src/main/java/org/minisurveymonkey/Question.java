package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Question {
    @Id
    @GeneratedValue
    private long id;
    @EnumeratedValue
    private QuestionType questionType;
    private String text;
    private String minValue;
    private String maxValue;
    private List<String> options;

    public Question() {
        options = new ArrayList<String>();
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public List<String> getOptions() {
        return options;
    }


}
