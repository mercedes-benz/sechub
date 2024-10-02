// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webui.security.SecurityTestConfiguration;

@WebMvcTest(LoginClassicController.class)
@Import(SecurityTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
class LoginClassicControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    LoginClassicControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_classic_page_is_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/login/classic"))
                .andExpect(status().isOk());
        /* @formatter:on */
    }
}
