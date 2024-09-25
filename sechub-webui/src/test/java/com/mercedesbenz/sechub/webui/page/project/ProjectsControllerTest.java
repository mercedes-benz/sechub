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
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.SecurityTestConfiguration;
import com.mercedesbenz.sechub.webui.page.user.UserInfoService;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@WebMvcTest(ProjectsController.class)
@Import(SecurityTestConfiguration.class)
public class ProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void index() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isForbidden());
    }

    @Test
    void projects() throws Exception {
        mockMvc.perform(get("/projects")).andExpect(status().isForbidden());
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
