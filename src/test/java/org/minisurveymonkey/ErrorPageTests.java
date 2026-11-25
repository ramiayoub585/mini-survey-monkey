package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorPageTests {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private SurveyRepository surveyRepository;

    private HttpResponse<String> getPage(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "text/html")
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Survey saveSurvey(boolean active) {
        Survey survey = new Survey();
        survey.setTitle("Error Page Test Survey");
        survey.setActive(active);
        return surveyRepository.save(survey);
    }

    private void assertStyledPage(HttpResponse<String> response) {
        assertFalse(response.body().contains("Whitelabel Error Page"));
        assertTrue(response.body().contains("<header>"));
        assertTrue(response.body().contains("/css/style.css"));
    }

    @Test
    void missingSurveyShowsNotFoundPage() throws Exception {
        HttpResponse<String> response = getPage("/surveys/999999");

        assertEquals(404, response.statusCode());
        assertStyledPage(response);
        assertTrue(response.body().contains("Page not found"));
    }

    @Test
    void closedSurveyShowsClosedPage() throws Exception {
        Survey closed = saveSurvey(false);
        HttpResponse<String> response = getPage("/surveys/" + closed.getId() + "/fill");

        assertEquals(403, response.statusCode());
        assertStyledPage(response);
        assertTrue(response.body().contains("This survey is closed"));
    }

    @Test
    void openSurveyResultsShowsNotReadyPage() throws Exception {
        Survey open = saveSurvey(true);
        HttpResponse<String> response = getPage("/surveys/" + open.getId() + "/results");

        assertEquals(403, response.statusCode());
        assertStyledPage(response);
        assertTrue(response.body().contains("Results are not ready yet"));
    }

    @Test
    void otherErrorsShowGenericPage() throws Exception {
        HttpResponse<String> response = getPage("/surveys/abc");

        assertEquals(400, response.statusCode());
        assertStyledPage(response);
        assertTrue(response.body().contains("Something went wrong"));
        assertTrue(response.body().contains("Error code: 400"));
    }
}
