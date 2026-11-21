package org.minisurveymonkey;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptionParserTest {

    @Test
    void splitsOnCommas() {
        assertEquals(List.of("Red", "Blue", "Green"), OptionParser.parse("Red, Blue, Green"));
    }

    @Test
    void splitsOnNewLines() {
        assertEquals(List.of("Red", "Blue"), OptionParser.parse("Red\nBlue"));
        assertEquals(List.of("Red", "Blue"), OptionParser.parse("Red\r\nBlue"));
    }

    @Test
    void dropsBlanks() {
        assertEquals(List.of("Red", "Blue"), OptionParser.parse("Red,,Blue, "));
    }

    @Test
    void keepsOriginalCase() {
        assertEquals(List.of("Red", "Blue"), OptionParser.parse("  Red , Blue "));
    }

    @Test
    void rejectsDuplicatesIgnoringCase() {
        assertThrows(IllegalArgumentException.class, () -> OptionParser.parse("Red, red"));
    }

    @Test
    void requiresAtLeastTwoOptions() {
        assertThrows(IllegalArgumentException.class, () -> OptionParser.parse("Red"));
        assertThrows(IllegalArgumentException.class, () -> OptionParser.parse(" , ,"));
        assertThrows(IllegalArgumentException.class, () -> OptionParser.parse(null));
    }
}