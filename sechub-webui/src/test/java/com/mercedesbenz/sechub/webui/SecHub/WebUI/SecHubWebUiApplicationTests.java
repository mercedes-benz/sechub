// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mercedesbenz.sechub.webui.SecHubServerAccessService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = { "server.port=8042", "management.server.port=9042", "sechub.userid=user",
        "sechub.apitoken=example" })
@AutoConfigureWebTestClient
class SecHubWebUiApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecHubServerAccessService mockAccessService;

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

    @Test
    void index() throws Exception {
        webTestClient.get().uri("/").exchange().expectStatus().isFound();
    }
}
