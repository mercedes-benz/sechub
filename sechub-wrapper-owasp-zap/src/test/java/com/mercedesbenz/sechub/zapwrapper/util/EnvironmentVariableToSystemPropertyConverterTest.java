package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class EnvironmentVariableToSystemPropertyConverterTest {

    @ParameterizedTest
    @ArgumentsSource(EnvironmentToSystemPropertyValidDataArgumentsProvider.class)
    void test(String from, String to) {
        /* prepare */
        EnvironmentVariableToSystemPropertyConverter converterToTest = new EnvironmentVariableToSystemPropertyConverter();

        /* execute */
        String result = converterToTest.convertEnvironmentVariableToSystemPropertyKey(from);

        /* test */
        assertEquals(to, result);
    }

    private static class EnvironmentToSystemPropertyValidDataArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of(null, null),
              Arguments.of("",""),
              Arguments.of("TEST_1", "test.1"),
              Arguments.of("test_Alpha.1", "test.alpha.1"),
              Arguments.of("PDS_SCAN_CONFIGURATION", "pds.scan.configuration"),
              /* ... more ...*/
		      Arguments.of("test", "test"));
        }
        /* @formatter:on*/
    }

}
