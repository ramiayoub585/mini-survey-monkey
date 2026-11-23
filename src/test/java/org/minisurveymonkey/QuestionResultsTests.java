package org.minisurveymonkey;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionResultTest {

    private static Question choiceQuestion(String... options) {
        Question q = new Question();
        q.setQuestionType(QuestionType.CHOICE);
        q.setText("Favourite colour?");
        q.getOptions().addAll(List.of(options));
        return q;
    }

    private static Question numberQuestion(int min, int max) {
        Question q = new Question();
        q.setQuestionType(QuestionType.NUMBER);
        q.setText("Rate it");
        q.setMinValue(min);
        q.setMaxValue(max);
        return q;
    }

    private static Question textQuestion() {
        Question q = new Question();
        q.setQuestionType(QuestionType.TEXT);
        q.setText("Any comments?");
        return q;
    }

    @Test
    void choiceListsEveryOptionEvenWithZeroAnswers() {
        QuestionResult result = new QuestionResult(choiceQuestion("Red", "Blue", "Green"),
                List.of("Red", "Red", "Blue"));

        assertEquals(List.of("Red", "Blue", "Green"), List.copyOf(result.getCounts().keySet()));
        assertEquals(2L, result.getCounts().get("Red"));
        assertEquals(1L, result.getCounts().get("Blue"));
        assertEquals(0L, result.getCounts().get("Green"));
    }

    @Test
    void choiceChartHasOneSlicePerOption() {
        QuestionResult result = new QuestionResult(choiceQuestion("Red", "Blue", "Green"),
                List.of("Red", "Red", "Blue"));

        assertEquals(List.of("Red", "Blue", "Green"), result.getChartLabels());
        assertEquals(List.of(2L, 1L, 0L), result.getChartValues());
        assertTrue(result.isChartable());
    }

    @Test
    void numberAnswersAreSortedNumericallyNotAlphabetically() {
        QuestionResult result = new QuestionResult(numberQuestion(0, 10),
                List.of("10", "2", "2", "1"));

        assertEquals(List.of("1", "2", "10"), List.copyOf(result.getCounts().keySet()));
        assertEquals(2L, result.getCounts().get("2"));
    }

    @Test
    void numberHistogramFillsZerosForValuesNobodyPicked() {
        QuestionResult result = new QuestionResult(numberQuestion(1, 5),
                List.of("2", "2", "5"));

        assertEquals(List.of("1", "2", "3", "4", "5"), result.getChartLabels());
        assertEquals(List.of(0L, 2L, 0L, 0L, 1L), result.getChartValues());
    }

    @Test
    void numberHistogramGroupsBigRangesIntoBins() {
        QuestionResult result = new QuestionResult(numberQuestion(1, 100),
                List.of("1", "5", "6", "100"));

        assertEquals(QuestionResult.MAX_BARS, result.getChartLabels().size());
        assertEquals("1-5", result.getChartLabels().get(0));
        assertEquals(2L, result.getChartValues().get(0));
        assertEquals("6-10", result.getChartLabels().get(1));
        assertEquals(1L, result.getChartValues().get(1));
        assertEquals("96-100", result.getChartLabels().get(19));
        assertEquals(1L, result.getChartValues().get(19));
    }

    @Test
    void numberHistogramIgnoresAnswersOutsideTheRangeOrNotNumbers() {
        QuestionResult result = new QuestionResult(numberQuestion(1, 5),
                List.of("0", "9", "abc"));

        assertTrue(result.getChartValues().stream().allMatch(v -> v == 0L));
    }

    @Test
    void noResponsesGivesNoChartAndNoNumberCounts() {
        QuestionResult number = new QuestionResult(numberQuestion(0, 3), List.of());
        assertTrue(number.getCounts().isEmpty());
        assertFalse(number.isChartable());

        QuestionResult choice = new QuestionResult(choiceQuestion("Yes", "No"), List.of());
        assertEquals(0L, choice.getCounts().get("Yes"));
        assertEquals(0L, choice.getCounts().get("No"));
        assertFalse(choice.isChartable());
    }

    @Test
    void textQuestionJustListsAnswersWithNoChart() {
        QuestionResult result = new QuestionResult(textQuestion(), List.of("Great", "Fine"));

        assertEquals(List.of("Great", "Fine"), result.getAnswers());
        assertTrue(result.getCounts().isEmpty());
        assertTrue(result.getChartLabels().isEmpty());
        assertFalse(result.isChartable());
    }
}