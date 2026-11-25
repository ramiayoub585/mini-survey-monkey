package org.minisurveymonkey;

import java.util.*;

/**
 * The options parser class handles the text typed in the add-question form for an option question into a clean list.
 * Options can be separated by commas or new lines. Each option is trimmed, blanks are dropped,
 * duplicates (for both upper/lower case) are rejected, and at least 2 options are required.
 */
public final class OptionParser {

    private OptionParser() {
    }

    /**
     * Returns the clean options, or throws IllegalArgumentException with a message to show the user.
     * @param text the text options that the user inputs.
     * @return returns a clean list of the options.
     * */
    public static List<String> parse(String text) {
        List<String> options = new ArrayList<>(); // Stores the options
        Set<String> seen = new HashSet<>(); // Handles duplicates
        if (text != null) {
            for (String part : text.split("[,\\r\\n]+")) { // Splits the options through the commas
                String option = part.trim();
                if (option.isEmpty()) {
                    continue;
                }
                if (!seen.add(option.toLowerCase(Locale.ROOT))) { // Handles duplivate options.
                    throw new IllegalArgumentException("Duplicate option: " + option);
                }
                options.add(option); // Adds each option
            }
        }
        if (options.size() < 2) { // If the options are not at least two choices throws and exception.
            throw new IllegalArgumentException("A multiple-choice question needs at least 2 options.");
        }
        return options; // Returns the options.
    }
}