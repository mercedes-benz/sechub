// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@EnableConfigurationProperties
@RunWith(SpringRunner.class)
@SpringBootTest(value = { "sechub.userid=user", "sechub.apitoken=example" }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecHubWebUiApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

    @Test
    void index() throws Exception {
        mvc.perform(get("/").contentType("text/html")).andExpect(status().isFound());
    }

    @Test
    void login() throws Exception {
        mvc.perform(get("/login").contentType("text/html")).andExpect(status().isOk());
    }
}
