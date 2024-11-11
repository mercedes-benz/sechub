// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration.ADMIN;
import static com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration.OWNER;
import static com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration.USER;
import static com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration.getJwtAuthHeader;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mercedesbenz.sechub.testframework.spring.OAuth2SecurityTestConfiguration;
import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

/**
 * This test class verifies the integration of Spring Security OAuth2
 * components.
 *
 * <p>
 * Unlike {@link SecHubApiSecurityConfigurationTest}, which primarily tests if a
 * endpoint is secured on a abstract level, this class exercises the full OAuth2
 * flow with real OAuth2 mechanisms. We do that by relying the
 * {@link AbstractSecHubAPISecurityConfiguration}.
 * </p>
 *
 * <p>
 * In a typical setup, the
 * {@link org.springframework.security.oauth2.jwt.JwtDecoder} decodes JWT tokens
 * by integrating with a identity provider. With this configuration, however, we
 * mock the identity provider to avoid external dependencies. Additionally, we
 * mock the user's roles, which are otherwise fetched from the database.
 * </p>
 *
 * <p>
 * <b>Note:</b> This test class is not intended for verifying whether security
 * is enabled on specific endpoints. For that, use
 * {@link SecHubApiSecurityConfigurationTest}.
 * </p>
 *
 * @see AbstractSecHubAPISecurityConfiguration
 * @see com.mercedesbenz.sechub.domain.authorization.AuthUserDetailsService
 * @see OAuth2AuthenticationProvider
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see SecHubApiSecurityConfigurationTest
 *
 * @author hamidonos
 */
@SuppressWarnings("JavadocReference")
@WebMvcTest
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("oauth2")
class OAuth2IntegrationTest {

    /**
     * For this test we call the API endpoint
     * /api/project/mock-project/false-positives. It is just a mock endpoint to test
     * the OAuth2 integration. It could also be any other endpoint.
     */
    private static final String PROJECT_FALSE_POSITIVES_PATH = "/api/project/mock-project/false-positives";

    private final MockMvc mockMvc;

    @Autowired
    OAuth2IntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void api_call_projects_false_positives_anonymously_is_unauthorized() throws Exception {
        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(PROJECT_FALSE_POSITIVES_PATH))
                .andExpect(status().isUnauthorized());
        /* @formatter:on */
    }

    @Test
    void api_call_projects_false_positives_as_admin_user_is_ok() throws Exception {
        /* prepare */
        String authHeader = getJwtAuthHeader(ADMIN);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(PROJECT_FALSE_POSITIVES_PATH).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @Test
    void api_call_projects_false_positives_as_owner_is_forbidden() throws Exception {
        /* prepare */
        String authHeader = getJwtAuthHeader(OWNER);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(PROJECT_FALSE_POSITIVES_PATH).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isForbidden());
        /* @formatter:on */
    }

    @Test
    void api_call_projects_false_positives_as_user_is_ok() throws Exception {
        /* prepare */
        String authHeader = getJwtAuthHeader(USER);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(PROJECT_FALSE_POSITIVES_PATH).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @Configuration
    @Import(OAuth2SecurityTestConfiguration.class)
    @EnableConfigurationProperties(OAuth2Properties.class)
    static class TestConfig extends AbstractSecHubAPISecurityConfiguration {

        @Bean
        TestSecurityController testSecurityController() {
            return new TestSecurityController();
        }
    }
}
