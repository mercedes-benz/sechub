// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.project;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.page.user.UserInfoService;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;
import com.mercedesbenz.sechub.webui.security.SecurityConfiguration;

@WebMvcTest(ProjectsController.class)
@Import(SecurityConfiguration.class)
class ProjectsControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    public ProjectsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void projects_page_is_not_accessible_anonymously() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/projects"))
                .andExpect(status().is3xxRedirection());
        /* @formatter:on */
    }

    @Test
    @WithMockUser
    void projects_page_is_accessible_with_authenticated_user() throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(get("/projects"))
                .andExpect(status().isOk());
        /* @formatter:on */
    }

    @TestConfiguration
    static class ProjectsControllerTestConfiguration {

        @Bean
        SecHubAccessService accessService() {
            return mock();
        }

        @Bean
        ProjectInfoService projectInfoService() {
            return mock();
        }

        @Bean
        UserInfoService userInfoService() {
            return mock();
        }
    }
}
