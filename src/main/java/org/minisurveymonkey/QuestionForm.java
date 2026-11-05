package org.minisurveymonkey;

import java.util.ArrayList;
import java.util.List;

public class QuestionForm {
    private QuestionType questionType = QuestionType.TEXT;
    private String text;
    private Integer max;
    private Integer min;
    private String optionsText;

    public QuestionType getQuestionType() {
        return questionType;
    }
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {this.text = text;}
    public Integer getMinValue() {
        return min;
    }
    public void setMinValue(Integer minValue) {
        min = minValue;
    }
    public Integer getMaxValue() {
        return max;
    }
    public void setMaxValue(Integer maxValue) {
        max = maxValue;
    }
    public String getOptionsText() {
        return optionsText;
    }
    public void setOptionsText(String optionsText) {
        this.optionsText =  optionsText;
    }


}
