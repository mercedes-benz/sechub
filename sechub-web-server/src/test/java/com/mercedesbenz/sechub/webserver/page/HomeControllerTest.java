// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webserver.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webserver.security.SecurityTestConfiguration;

@WebMvcTest(HomeController.class)
@Import(SecurityTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("classic-auth-enabled")
class HomeControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    public HomeControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void home_page_is_not_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/home"))
                .andExpect(status().is3xxRedirection());
        /* @formatter:on */
    }

    @Test
    @WithMockUser
    void home_page_is_accessible_with_authenticated_user() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/home"))
                .andExpect(status().isOk());
        /* @formatter:on */
    }
}
