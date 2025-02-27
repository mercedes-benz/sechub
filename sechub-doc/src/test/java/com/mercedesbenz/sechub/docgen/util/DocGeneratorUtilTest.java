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

    @ParameterizedTest
    @ArgumentsSource(CorrectPropertyNamesToEnvNamesProvider.class)
    void convertSystemPropertyToEnvironmentVariable_converts(String from, String to) {
        /* execute */
        String result = DocGeneratorUtil.convertSystemPropertyToEnvironmentVariable(from);

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
              Arguments.of("",""),
              Arguments.of(null,null),
              Arguments.of("OAuth2","oauth2"),
              Arguments.of("oAuth2","oauth2"),
              Arguments.of("redirectUri","redirect-uri"),
              Arguments.of("clientId","client-id"),
              Arguments.of("clientSecret","client-secret"),
		      Arguments.of("jwkSetUri","jwk-set-uri"));
        }
        /* @formatter:on*/
    }

    private static class CorrectPropertyNamesToEnvNamesProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of("test", "TEST"),
                    Arguments.of("Test", "TEST"),
                    Arguments.of("TesT", "TEST"),
                    Arguments.of("TEST", "TEST"),
                    Arguments.of("", ""),
                    Arguments.of(null,null),
                    Arguments.of("uri", "URI"),
                    Arguments.of("oauth2", "OAUTH2"),
                    Arguments.of("redirect-uri","REDIRECTURI"),
                    Arguments.of("sechub.security.server.oauth2.opaque-token.max-cache-duration", "SECHUB_SECURITY_SERVER_OAUTH2_OPAQUETOKEN_MAXCACHEDURATION"),
                    Arguments.of("jwk-set-uri", "JWKSETURI"));
        }
        /* @formatter:on*/
    }

}
