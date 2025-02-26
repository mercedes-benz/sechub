// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class DocGeneratorUtilTest {

    @ParameterizedTest
    @ArgumentsSource(CorrectPropertyNamesProvider.class)
    void convertParameterNameToSpringSystemPropertyNamePart_converts(String from, String to) {
        /* execute */
        String result = DocGeneratorUtil.convertCamelCaseToKebabCase(from);

        /* test */
        assertThat(result).isEqualTo(to);
    }

    private static class CorrectPropertyNamesProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of("test","test"),
              Arguments.of("URI","uri"),
              Arguments.of("OAuth2","oauth2"),
              Arguments.of("oAuth2","oauth2"),
              Arguments.of("redirectUri","redirect-uri"),
              Arguments.of("clientId","client-id"),
              Arguments.of("clientSecret","client-secret"),
		      Arguments.of("jwkSetUri","jwk-set-uri"));
        }
        /* @formatter:on*/
    }

}
