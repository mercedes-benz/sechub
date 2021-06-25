// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import org.springframework.stereotype.Component;

@Component
public class AuthorizeValueObfuscator {

    private static final int SHOWN_REAL_CHARS_OF_PASSWORD = 4;
    private static final String BASIC_WITH_SPACE_LOWERCASED = "basic ";

    /**
     * Obfuscates given authentication value (password, api token etc.).
     * If a password starts with "Basic " this will always be visible.
     * After this 
     * 
     * @param value
     * @return
     */
    public String obfuscate(String value) {

        int showRealChars = calculateCharsToShow(value);
        
        boolean atleastHalfOfThePwdWouldBeObfuscated = value.length() > showRealChars + SHOWN_REAL_CHARS_OF_PASSWORD;
        
        if (atleastHalfOfThePwdWouldBeObfuscated) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.substring(0, showRealChars));
            for (int i = showRealChars; i < value.length(); i++) {
                sb.append('*');
            }
            return sb.toString();
        } else {
            return "*****length was:" + value.length();
        }

    }

    private int calculateCharsToShow(String value) {
        int showRealChars = SHOWN_REAL_CHARS_OF_PASSWORD;
        if (value.toLowerCase().startsWith(BASIC_WITH_SPACE_LOWERCASED)) {
            showRealChars += BASIC_WITH_SPACE_LOWERCASED.length();
        }
        return showRealChars;
    }
}
