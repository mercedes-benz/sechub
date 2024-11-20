// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;

/**
 * This test class verifies the integration of Spring Security OAuth2 components
 * in JWT mode.
 *
 * <p>
 * Unlike {@link SecurityConfigurationTest}, which primarily tests if a endpoint
 * is secured on an abstract level, this class exercises the full OAuth2 flow
 * with real OAuth2 mechanisms. We do that by relying on the
 * {@link AbstractSecurityConfiguration}.
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
 * {@link SecurityConfigurationTest}.
 * </p>
 *
 * @see AbstractSecurityConfiguration
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see OAuth2JwtAuthenticationProvider
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see SecurityConfigurationTest
 *
 * @author hamidonos
 */
@WebMvcTest
@TestPropertySource(locations = "classpath:application-jwt-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("oauth2")
class OAuth2JwtIntegrationTest {

    /**
     * For this test we call the API endpoint /api/user. It is just a mock endpoint
     * to test the OAuth2 integration. It could also be any other endpoint.
     */
    private static final String API_USER = "/api/user";

    private final MockMvc mockMvc;

    @Autowired
    OAuth2JwtIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void api_user_is_not_accessible_anonymously() throws Exception {
        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(API_USER))
                .andExpect(status().isUnauthorized());
        /* @formatter:on */
    }

    @Test
    void api_user_is_accessible_as_superadmin() throws Exception {
        /* prepare */
        String authHeader = TestOAuth2JwtSecurityConfiguration.getJwtAuthHeader(TestRoles.SUPERADMIN);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(API_USER).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @Test
    void api_user_is_not_accessible_as_owner() throws Exception {
        /* prepare */
        String authHeader = TestOAuth2JwtSecurityConfiguration.getJwtAuthHeader(TestRoles.OWNER);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(API_USER).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isForbidden());
        /* @formatter:on */
    }

    @Test
    void api_user_is_accessible_as_user() throws Exception {
        /* prepare */
        String authHeader = TestOAuth2JwtSecurityConfiguration.getJwtAuthHeader(TestRoles.USER);

        /* execute & test */
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(API_USER).header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @Configuration
    @Import({ TestSecurityConfiguration.class, TestOAuth2JwtSecurityConfiguration.class, OAuth2JwtPropertiesConfiguration.class })
    static class TestConfig {

        @Bean
        TestSecurityController testSecurityController() {
            return new TestSecurityController();
        }
    }
}
