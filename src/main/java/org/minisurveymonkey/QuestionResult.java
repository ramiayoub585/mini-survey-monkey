package org.minisurveymonkey;

import java.util.*;

public class QuestionResult {
    private final Question question;
    private final List<String> answers;
    private final Map<String, Long> counts;

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
