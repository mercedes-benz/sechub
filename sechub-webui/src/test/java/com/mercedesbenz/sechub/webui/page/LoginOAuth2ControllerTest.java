// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.security.SecurityConfiguration;

@WebMvcTest(LoginOAuth2Controller.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class LoginOAuth2ControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    LoginOAuth2ControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_oauth2_page_is_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login/oauth2"))
                .andExpect(status().isOk());
        /* @formatter:on */
    }
}
