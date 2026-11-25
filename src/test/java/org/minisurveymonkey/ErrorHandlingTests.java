package org.minisurveymonkey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ErrorHandlingTests {

    @Autowired
    private MockMvc mockMvc;

    // Test that requesting a survey ID that doesn't exist returns HTTP 404 Not Found
    @Test
    void nonexistentSurveyReturns404() throws Exception {
        mockMvc.perform(get("/surveys/999999")).andExpect(status().isNotFound());
    }
}
