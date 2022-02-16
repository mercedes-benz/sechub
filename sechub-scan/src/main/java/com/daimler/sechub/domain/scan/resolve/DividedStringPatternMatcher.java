// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * Special matcher to inspect string matching to a string pattern
 *
 */
public class DividedStringPatternMatcher {

    private String[] patternParts;

    private char divider;

    /**
     * Creates a new matcher for this pattern
     *
     * @param pattern a pattern which is separated by divider - e.g. when divider is
     *                '-' a valid pattern would be `1-2-3'
     * @param divider character used for separation
     */
    public DividedStringPatternMatcher(String pattern, char divider) {
        Objects.requireNonNull(pattern, "pattern may not be null");
        this.divider = divider;
        patternParts = splitByDivider(pattern);
    }

    public String[] getPatternParts() {
        return patternParts.clone();
    }

    public boolean isMatching(String given) {
        if (given == null) {
            return false;
        }
        String target = given.toLowerCase();
        String[] targetPart = splitByDivider(target);
        if (targetPart == null) {
            return false;
        }
        if (targetPart.length != patternParts.length) {
            return false;
        }
        boolean accepted = true;
        for (int i = 0; i < patternParts.length && accepted; i++) {
            accepted = patternParts[i].equals("*") || patternParts[i].equals(targetPart[i]);
        }
        return accepted;
    }

    private String[] splitByDivider(String pattern) {
        String[] parts = pattern.split(Pattern.quote("" + divider));

        String[] lowerCaseParts = new String[parts.length];
        int i = 0;
        for (String caseVariant : parts) {
            lowerCaseParts[i++] = caseVariant.toLowerCase();
        }

        return lowerCaseParts;
    }

}
