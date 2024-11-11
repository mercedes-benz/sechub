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
import com.mercedesbenz.sechub.webserver.security.OAuth2Properties;
import com.mercedesbenz.sechub.webserver.security.SecurityTestConfiguration;

@WebMvcTest(LoginController.class)
@Import(SecurityTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("oauth2-enabled")
class LoginControllerOAuth2EnabledTest {

    private final MockMvc mockMvc;
    private final OAuth2Properties oAuth2Properties;

    @Autowired
    LoginControllerOAuth2EnabledTest(MockMvc mockMvc, OAuth2Properties oAuth2Properties) {
        this.mockMvc = mockMvc;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Test
    void login_page_is_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("isOAuth2Enabled"))
                .andExpect(model().attribute("isOAuth2Enabled", true))
                .andExpect(model().attributeExists("isClassicAuthEnabled"))
                .andExpect(model().attribute("isClassicAuthEnabled", false))
                .andExpect(model().attributeExists("registrationId"))
                .andExpect(model().attribute("registrationId", oAuth2Properties.getProvider()))
                .andExpect(content().string(containsString("You will be redirected to your OAuth2 Provider for authentication.")))
                .andExpect(content().string(not(containsString("Login with your provided User ID & API Token."))))
                .andExpect(content().string(not(containsString("Don't have an account? <a href=\"/register\">Register</a>"))));
        /* @formatter:on */
    }
}
