// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.mercedesbenz.sechub.testframework.spring.WithMockJwtUser;
import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webserver.security.TestWebServerSecurityConfiguration;
import com.mercedesbenz.sechub.webserver.server.ServerProperties;
import com.mercedesbenz.sechub.webserver.server.ServerPropertiesConfiguration;

@WebMvcTest(HomeController.class)
@Import({ TestWebServerSecurityConfiguration.class, ServerPropertiesConfiguration.class })
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@ActiveProfiles("oauth2")
class HomeControllerTest {

    private final MockMvc mockMvc;
    private final RequestPostProcessor requestPostProcessor;
    private final String homePageUrl;

    @Autowired
    HomeControllerTest(MockMvc mockMvc, RequestPostProcessor requestPostProcessor, ServerProperties serverProperties) {
        this.mockMvc = mockMvc;
        this.requestPostProcessor = requestPostProcessor;
        this.homePageUrl = "http://localhost:%d/home".formatted(serverProperties.getPort());
    }

    @Test
    void home_page_is_not_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get(homePageUrl))
                .andExpect(status().is3xxRedirection());
        /* @formatter:on */
    }

    @Test
    @WithMockJwtUser
    void home_page_is_accessible_with_authenticated_user() throws Exception {
        /* execute & test */

        /* @formatter:off */
        mockMvc
                .perform(get(homePageUrl).with(requestPostProcessor))
                .andExpect(status().isOk())
                .andReturn();
        /* @formatter:on */
    }
}
