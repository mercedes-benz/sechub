// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

class UrlUtilTest {

    private UrlUtil urlUtilToTest;

    @BeforeEach
    void beforeEach() {
        urlUtilToTest = new UrlUtil();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void null_or_empty_url_leads_to_empty_result(String url) {
        /* execute */
        String result = urlUtilToTest.replaceWebScanWildCardsWithRegexInString(url);

        /* test */
        assertEquals("", result);
    }

    @Test
    void replacing_sechub_url_wildcard_results_in_url_with_regex() {
        /* prepare */
        String url = "https://localhost/admin/<*>/profile/<*>";
        String expectedUrl = "https://localhost/admin/.*/profile/.*";

        /* execute */
        String result = urlUtilToTest.replaceWebScanWildCardsWithRegexInString(url);

        /* test */
        assertEquals(expectedUrl, result);
    }

    @Test
    void url_without_wildcard_is_not_changed_by_util_method() {
        /* prepare */
        String url = "https://localhost/admin/profile";
        String expectedUrl = "https://localhost/admin/profile";

        /* execute */
        String result = urlUtilToTest.replaceWebScanWildCardsWithRegexInString(url);

        /* test */
        assertEquals(expectedUrl, result);
    }

}
