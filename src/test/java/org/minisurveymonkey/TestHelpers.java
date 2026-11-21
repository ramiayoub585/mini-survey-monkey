
package org.minisurveymonkey;

public final class TestHelpers {

    private TestHelpers() {
        // Prevent creating instances of this utility class
    }

    // Create and save an active survey for testing
    public static Survey createTestSurvey(SurveyRepository surveyRepository, String title) {
        Survey survey = new Survey();
        survey.setTitle(title);
        survey.setActive(true);

        return surveyRepository.save(survey);
    }

    // Create a question with default values for testing
    public static Question createTestQuestion(String text, QuestionType type) {
        Question question = new Question();
        question.setText(text);
        question.setQuestionType(type);

        if (type == QuestionType.NUMBER) {
            question.setMinValue(1);
            question.setMaxValue(10);
        }

        if (type == QuestionType.CHOICE) {
            question.getOptions().add("Yes");
            question.getOptions().add("No");
        }

        return question;
    }
}
