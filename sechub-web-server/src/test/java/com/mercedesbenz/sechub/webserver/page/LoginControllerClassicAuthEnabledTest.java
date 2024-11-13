// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webserver.security.SecurityTestConfiguration;

@WebMvcTest(LoginController.class)
@Import(SecurityTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("classic-auth-enabled")
class LoginControllerClassicAuthEnabledTest {

    private final MockMvc mockMvc;

    @Autowired
    LoginControllerClassicAuthEnabledTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_page_is_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("isOAuth2Enabled"))
                .andExpect(model().attribute("isOAuth2Enabled", false))
                .andExpect(model().attributeExists("isClassicAuthEnabled"))
                .andExpect(model().attribute("isClassicAuthEnabled", true))
                .andExpect(model().attributeDoesNotExist("registrationId"))
                .andExpect(content().string(not(containsString("You will be redirected to your OAuth2 Provider for authentication."))))
                .andExpect(content().string(containsString("Login with your provided User ID & API Token.")))
                .andExpect(content().string(containsString("Don't have an account? <a href=\"/register\">Register</a>")));
        /* @formatter:on */
    }
}
