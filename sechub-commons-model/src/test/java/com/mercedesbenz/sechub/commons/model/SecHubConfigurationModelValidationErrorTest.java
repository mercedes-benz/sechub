// SPDX-License-Identifier: MIT

package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SecHubConfigurationModelValidationErrorTest {

    @ParameterizedTest
    @EnumSource(SecHubConfigurationModelValidationError.class)
    void message_does_end_with_dot_or_exclamation_mark(SecHubConfigurationModelValidationError error) {
        /* execute */
        String defaultMessage = error.getDefaultMessage();

        /* test */
        String[] expectedEndings = new String[] { ".", "!" };
        boolean endsWithExpected = false;
        for (String expectedEnding : expectedEndings) {
            endsWithExpected = defaultMessage.endsWith(expectedEnding);
            if (endsWithExpected) {
                break;
            }

        }

        if (!endsWithExpected) {
            fail("Message does not end with expected endings:" + expectedEndings + ":\n" + defaultMessage);
        }
    }

}
