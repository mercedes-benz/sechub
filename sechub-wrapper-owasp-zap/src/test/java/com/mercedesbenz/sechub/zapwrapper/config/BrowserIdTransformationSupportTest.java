// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.Assert.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class BrowserIdTransformationSupportTest {

    @ParameterizedTest
    @ArgumentsSource(NoHeadlessTransformationResultArgumentsProvider.class)
    void transform_when_no_headless_wanted(String from, String to) {
        /* prepare */
        BrowserIdTransformationSupport supportToTest = new BrowserIdTransformationSupport();

        /* execute */
        String result = supportToTest.transformBrowserIdWhenNoHeadless(true, from);

        /* test */
        assertEquals(to, result);
    }

    @ParameterizedTest
    @ArgumentsSource(StillHeadlessTransformationResultArgumentsProvider.class)
    void transform_when_headless_wanted__keep_settings_as_is(String from, String to) {
        /* prepare */
        BrowserIdTransformationSupport supportToTest = new BrowserIdTransformationSupport();

        /* execute */
        String result = supportToTest.transformBrowserIdWhenNoHeadless(false, from);

        /* test */
        assertEquals(to, result);
    }

    private static class NoHeadlessTransformationResultArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of("firefox-headless", "firefox"),
              Arguments.of("chrome-headless", "chrome"),
              Arguments.of("chrome", "chrome"),
              Arguments.of("firefox", "firefox"),
              Arguments.of("safari", "safari"),
              Arguments.of("htmlunit", "htmlunit"),
              Arguments.of("other", "other"),
              Arguments.of(null, null),
		      Arguments.of("", ""));
        }
        /* @formatter:on*/
    }

    private static class StillHeadlessTransformationResultArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of("firefox-headless","firefox-headless"),
              Arguments.of("chrome-headless", "chrome-headless"),
              Arguments.of("chrome", "chrome"),
              Arguments.of("firefox", "firefox"),
              Arguments.of("safari", "safari"),
              Arguments.of("htmlunit", "htmlunit"),
              Arguments.of("other", "other"),
              Arguments.of(null, null),
              Arguments.of("", ""));
        }
        /* @formatter:on*/
    }
}
