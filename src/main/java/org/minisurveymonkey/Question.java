package org.minisurveymonkey;

import jakarta.persistence.*;
import java.util.*;

/**
 * The Questions that the surveyor can create in a survey. There are three types: TEXT (free-text answer), NUMBER (answer between minValue and maxValue) and
 * CHOICE (pick one of the options).
 */

@Entity
public class Question {
    @Id
    @GeneratedValue
    private long id;
    @Enumerated(EnumType.STRING)
    private QuestionType questionType; // The type of question for the three choices mentioned above
    private String text; // The question being show to the respondent
    private Integer minValue; // Min value for Number questions only
    private Integer maxValue; // Max value for Number questions only
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

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public QuestionType getType() { return questionType; }   // the controller calls this
    public List<String> getOptions() { return options; }

    /**
     * This check ensures that question has all complete attributes, ie. a type and text, min and max for number values, etc.
     * @return An error message to the user or null if the question is complete.
     */
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
