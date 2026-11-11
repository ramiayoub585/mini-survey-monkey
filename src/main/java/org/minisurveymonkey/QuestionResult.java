package org.minisurveymonkey;

import java.util.*;

/**
 * The combined results of answers to a question. Once the surveyor closes a survey, all responses recorded by the respondents are saved for that question.
 * The question and all its corresponding answers from the respondents are saved.
 * Format:
 * Every answer given (as text) and, for NUMBER and CHOICE questions, a count per answer.
 * CHOICE lists every option (even with 0 answers).
 * NUMBER lists each distinct answer in numeric order.
 * TEXT questions just list their answers.
 */
public class QuestionResult {
    private final Question question; // The question
    private final List<String> answers; // The set of answers
    private final Map<String, Long> counts; // Counts for the number and choice answers

    // Builds the results for the questions based on the question type.
    public QuestionResult(Question question, List<String> answers) {
        this.question = question;
        this.answers = answers;
        this.counts = new LinkedHashMap<>();

        if (question.getQuestionType() == QuestionType.CHOICE) {
            for (String option : question.getOptions()) {
                counts.put(option, 0L);
            }
            for (String answer : answers) {
                counts.merge(answer, 1L, Long::sum);
            }
        }
        if (question.getQuestionType() == QuestionType.NUMBER) {
            List<String> sorted = new ArrayList<>(answers);
            sorted.sort(Comparator.comparing(QuestionResult::toNumber, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Comparator.naturalOrder()));
            for (String answer : sorted) {
                counts.merge(answer, 1L, Long::sum);
            }
        }
    }

    private static Double toNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Question getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }
}
