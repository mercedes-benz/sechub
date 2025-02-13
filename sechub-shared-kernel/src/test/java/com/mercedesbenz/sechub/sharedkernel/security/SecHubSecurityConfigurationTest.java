// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.spring.security.AbstractSecurityConfiguration;

/**
 * This test class makes sure that the defined API security rules from
 * {@link AbstractSecurityConfiguration} are working properly.
 *
 * <p>
 * Using {@link WithMockUser} to set up a mocked
 * {@link org.springframework.security.core.context.SecurityContext}, we can
 * test how the endpoints behave when accessed by different roles.
 * </p>
 *
 * <p>
 * <b>Note:</b> Here we don't test the integration of OAuth2 or Basic Auth. For
 * that, see
 * {@link com.mercedesbenz.sechub.spring.security.OAuth2JwtIntegrationTest} or
 * {@link com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntegrationTest}.
 * This test class is only concerned with verifying if the security rules are
 * correctly applied on an abstract level.
 * </p>
 *
 * @see WithMockUser
 * @see OAuth2JwtIntegrationTest
 * @see AbstractSecurityConfiguration
 *
 * @author hamidonos
 */
@SuppressWarnings("JavadocReference")
@WebMvcTest
class SecHubSecurityConfigurationTest {

    private static final String SUPERADMIN = "SUPERADMIN";
    private static final String USER = "USER";
    private static final String OWNER = "OWNER";

    private final MockMvc mockMvc;

    @Autowired
    SecHubSecurityConfigurationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    /* Super Admin */

    @Test
    @WithMockUser(roles = SUPERADMIN)
    void api_admin_is_accessible_with_superadmin_role() throws Exception {
        getAndExpect("/api/admin", HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = SUPERADMIN)
    void api_user_is_accessible_with_superadmin_role() throws Exception {
        getAndExpect("/api/user", HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = SUPERADMIN)
    void api_project_is_accessible_with_superadmin_role() throws Exception {
        getAndExpect("/api/project", HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = SUPERADMIN)
    void api_owner_is_accessible_with_superadmin_role() throws Exception {
        getAndExpect("/api/owner", HttpStatus.OK);
    }

    /* User */

    @Test
    @WithMockUser(roles = USER)
    void api_admin_is_not_accessible_with_user_role() throws Exception {
        getAndExpect("/api/admin", HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(roles = USER)
    void api_user_is_accessible_with_user_role() throws Exception {
        getAndExpect("/api/user", HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = USER)
    void api_project_is_accessible_with_user_role() throws Exception {
        getAndExpect("/api/project", HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = USER)
    void api_owner_is_not_accessible_with_user_role() throws Exception {
        getAndExpect("/api/owner", HttpStatus.FORBIDDEN);
    }

    /* Owner */

    @Test
    @WithMockUser(roles = OWNER)
    void api_admin_is_not_accessible_with_owner_role() throws Exception {
        getAndExpect("/api/admin", HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(roles = OWNER)
    void api_user_is_not_accessible_with_owner_role() throws Exception {
        getAndExpect("/api/user", HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(roles = OWNER)
    void api_project_is_not_accessible_with_owner_role() throws Exception {
        getAndExpect("/api/project", HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(roles = OWNER)
    void api_owner_is_accessible_with_owner_role() throws Exception {
        getAndExpect("/api/owner", HttpStatus.OK);
    }

    /* Anonymous */

    @Test
    void api_admin_is_not_accessible_anonymously() throws Exception {
        getAndExpect("/api/admin", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void api_user_is_not_accessible_anonymously() throws Exception {
        getAndExpect("/api/user", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void api_project_is_not_accessible_anonymously() throws Exception {
        getAndExpect("/api/project", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void api_owner_is_not_accessible_anonymously() throws Exception {
        getAndExpect("/api/owner", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void api_anonymous_is_accessible_anonymously() throws Exception {
        getAndExpect("/api/anonymous", HttpStatus.OK);
    }

    @Test
    void error_page_is_accessible_anonymously() throws Exception {
        getAndExpect("/error", HttpStatus.OK);
    }

    @Test
    void actuator_is_accessible_anonymously() throws Exception {
        getAndExpect("/actuator", HttpStatus.OK);
    }

    private void getAndExpect(String path, HttpStatus httpStatus) throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(path))
                .andExpect(status().is(httpStatus.value()));
        /* @formatter:on */
    }

    @Configuration
    @Import(SecHubSecurityConfiguration.class)
    static class TestConfig {

        @Bean
        RestTemplate restTemplate() {
            return mock();
        }

        @Bean
        ApplicationShutdownHandler applicationShutdownHandler() {
            return mock();
        }

        @Bean
        TestSecurityController testSecurityController() {
            return new TestSecurityController();
        }
    }
}
