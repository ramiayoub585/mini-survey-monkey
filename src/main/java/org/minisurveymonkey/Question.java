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
    private Integer minValue;
    private Integer maxValue;
    @ElementCollection
    @OrderColumn
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

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public List<String> getOptions() {
        return options;
    }
    public void setOptions() {
        this.options = new ArrayList<String>(options);
    }
    public QuestionType getType() {
        return questionType;
    }

    public String validate(){
        if(questionType == null){
            return "Please choose a question type";
        }
        if(text == null || text.isBlank()){
            return "Cannot have blank question";
        }
        if (questionType == QuestionType.NUMBER){
            if (minValue == null || maxValue == null){
                return "Questions must have both a maximum and minimum number";
            }
            if(minValue >= maxValue){
                return "Max value must be larger than minimum value";
            }
        }
        if(questionType == QuestionType.CHOICE && options.size() < 2){
            return "Choice style questions must have at least 2 options";
        }
        return null;

    }

}
