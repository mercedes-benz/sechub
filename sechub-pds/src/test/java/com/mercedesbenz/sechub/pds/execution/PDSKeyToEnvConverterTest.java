// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class PDSKeyToEnvConverterTest {

    private PDSKeyToEnvConverter converterToTest;

    @BeforeEach
    public void beforeEach() throws Exception {
        converterToTest = new PDSKeyToEnvConverter();

    }

    @ParameterizedTest
    @ArgumentsSource(ConverterArgumentsProvider.class)
    void convertKeyToEnv_converts_origin_to_expected_result(String origin, String expectedResult) {
        assertThat(converterToTest.convertKeyToEnv(origin)).isEqualTo(expectedResult);
    }

    private static class ConverterArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                      /* origin, expectedResult */
              Arguments.of("abc", "ABC"),
              Arguments.of("abc.def.ghi", "ABC_DEF_GHI"),
              Arguments.of("pds.config.filefilter.EXCLUDES", "PDS_CONFIG_FILEFILTER_EXCLUDES"),
              Arguments.of("pds.Config.Product.timeout.minutes", "PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES"),
              Arguments.of("all-hyphens.are.re-moved", "ALLHYPHENS_ARE_REMOVED"),
              Arguments.of(null, null),
		      Arguments.of("", ""));
        }
        /* @formatter:on*/
    }

}
