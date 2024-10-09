// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webui.security.SecurityTestConfiguration;

@WebMvcTest({ LoginClassicController.class, LoginOAuth2Controller.class })
@Import(SecurityTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("classic-auth-enabled")
class LoginClassicControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    LoginClassicControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_classic_auth_page_is_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login/classic"))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @Test
    void login_o_auth_2_page_is_not_accessible_when_o_auth_2_is_disabled() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login/oauth2"))
                .andExpect(status().isNotFound());
        /* @formatter:on */
    }
}
