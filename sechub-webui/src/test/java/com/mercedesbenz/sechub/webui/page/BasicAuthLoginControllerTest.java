// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.webui.SecurityTestConfiguration;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

/*
 * No HTTP Server will be started for this test
 * for more details see: https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#mock-objects-web-reactive
 */
@WebMvcTest(BasicAuthLoginController.class)
@Import(SecurityTestConfiguration.class)
public class BasicAuthLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecHubAccessService mockAccessService;

    @Test
    void login() throws Exception {
        mockMvc.perform(get("/auth/bauth/login")).andExpect(status().isOk());
    }
}
