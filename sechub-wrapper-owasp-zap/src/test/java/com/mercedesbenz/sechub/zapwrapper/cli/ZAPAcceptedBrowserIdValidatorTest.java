// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.zapwrapper.config.ZAPAcceptedBrowserId;

class ZAPAcceptedBrowserIdValidatorTest {

    private ZAPAcceptedBrowserIdValidator validatorToTest = new ZAPAcceptedBrowserIdValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "1", "invalid", "UPPERCASE_INVALID" })
    void invalid_values_throw_an_parameter_exception(String browserId) {
        /* execute + test */
        assertThrows(ParameterException.class, () -> validatorToTest.validate(null, browserId));
    }

    @ParameterizedTest
    @EnumSource(ZAPAcceptedBrowserId.class)
    void all_valid_browser_ids_are_accepted(ZAPAcceptedBrowserId browserId) {
        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(null, browserId.getBrowserId()));
    }

    @ParameterizedTest
    @EnumSource(ZAPAcceptedBrowserId.class)
    void all_valid_browser_ids_are_accepted_uppercased(ZAPAcceptedBrowserId browserId) {
        /* prepare */
        String browserIdUppercased = browserId.getBrowserId().toUpperCase();

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(null, browserIdUppercased));
    }

}
