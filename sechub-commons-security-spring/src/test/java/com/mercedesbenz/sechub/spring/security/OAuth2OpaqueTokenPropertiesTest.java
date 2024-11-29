// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

@SpringBootTest
@ActiveProfiles("oauth2")
@TestPropertySource(locations = "classpath:application-opaque-token-test.yml", factory = YamlPropertyLoaderFactory.class)
class OAuth2OpaqueTokenPropertiesTest {

    private final OAuth2OpaqueTokenProperties properties;

    OAuth2OpaqueTokenPropertiesTest(@Autowired OAuth2OpaqueTokenProperties properties) {
        this.properties = properties;
    }

    @Test
    void construct_properties_with_opaque_token_enabled_succeeds() {
        assertThat(properties.getIntrospectionUri()).isEqualTo("https://example.org/introspection-uri");
        assertThat(properties.getClientId()).isEqualTo("example-client-id");
        assertThat(properties.getClientSecret()).isEqualTo("example-client-secret");
    }

    /* @formatter:off */
    @ParameterizedTest
    @ArgumentsSource(InvalidOAuth2OpaqueTokenPropertiesProvider.class)
    void construct_properties_with_null_arguments_fails(String introspectionUri,
                                                        String clientId,
                                                        String clientSecret,
                                                        String errMsg) {
        assertThatThrownBy(() -> new OAuth2OpaqueTokenProperties(introspectionUri, clientId, clientSecret))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(errMsg);
    }
    /* @formatter:on */

    @Configuration
    @Import({ OAuth2OpaqueTokenPropertiesConfiguration.class, OAuth2OpaqueTokenPropertiesConfiguration.class })
    static class TestConfig {
    }

    private static class InvalidOAuth2OpaqueTokenPropertiesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of(null, "example-client-id", "example-client-secret", "Property 'sechub.security.server.oauth2.opaque-token.introspection-uri' must not be null"),
                    Arguments.of("https://example.org/introspection-uri", null, "example-client-secret", "Property 'sechub.security.server.oauth2.opaque-token.client-id' must not be null"),
                    Arguments.of("https://example.org/introspection-uri", "example-client-id", null, "Property 'sechub.security.server.oauth2.opaque-token.client-secret' must not be null")
            );
            /* @formatter:on */
        }
    }
}