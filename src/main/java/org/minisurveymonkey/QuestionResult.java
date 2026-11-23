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
 * For the charts, NUMBER and CHOICE questions also get a list of labels and a list of counts.
 */
public class QuestionResult {
    // A number of bars/bins for the graph.
    static final int MAX_BARS = 20;

    private final Question question; // The question
    private final List<String> answers; // The set of answers
    private final Map<String, Long> counts; // Counts for the number and choice answers
    private final List<String> chartLabels = new ArrayList<>(); // Bar labels (NUMBER) or slice names (CHOICE)
    private final List<Long> chartValues = new ArrayList<>(); // How many answers for each label

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
            chartLabels.addAll(counts.keySet());
            chartValues.addAll(counts.values());
        }
        if (question.getQuestionType() == QuestionType.NUMBER) {
            List<String> sorted = new ArrayList<>(answers);
            sorted.sort(Comparator.comparing(QuestionResult::toNumber, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Comparator.naturalOrder()));
            for (String answer : sorted) {
                counts.merge(answer, 1L, Long::sum);
            }
            buildHistogram(answers);
        }
    }

    // Counts the answers for every value from min to max, with 0 for values nobody picked.
    // If the range is bigger than MAX_BARS the values are grouped into bins.
    // Answers that are not numbers or are outside the range are left out of the chart but still show in the table.
    private void buildHistogram(List<String> answers) {
        Integer min = question.getMinValue();
        Integer max = question.getMaxValue();
        if (min == null || max == null || min > max) {
            return;
        }
        long range = (long) max - min + 1;
        long binSize = (range + MAX_BARS - 1) / MAX_BARS; // 1 when the range fits in MAX_BARS
        int bins = (int) ((range + binSize - 1) / binSize);
        long[] totals = new long[bins];
        for (String answer : answers) {
            Double number = toNumber(answer);
            if (number == null || number < min || number > max) {
                continue;
            }
            totals[(int) ((Math.floor(number) - min) / binSize)]++;
        }
        for (int i = 0; i < bins; i++) {
            long low = min + i * binSize;
            long high = Math.min(low + binSize - 1, max);
            chartLabels.add(low == high ? String.valueOf(low) : low + "-" + high);
            chartValues.add(totals[i]);
        }
    }

    // Turns an answer into a number so answers sort numerically; null if it is not a number
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

    public List<String> getChartLabels() {
        return chartLabels;
    }

    public List<Long> getChartValues() {
        return chartValues;
    }

    // True when this question gets a chart: NUMBER or CHOICE, and at least one answer
    public boolean isChartable() {
        QuestionType type = question.getQuestionType();
        return (type == QuestionType.CHOICE || type == QuestionType.NUMBER)
                && !answers.isEmpty() && !chartLabels.isEmpty();
    }
}