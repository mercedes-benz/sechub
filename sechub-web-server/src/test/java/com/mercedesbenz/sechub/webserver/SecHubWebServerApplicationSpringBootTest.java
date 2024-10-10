// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mercedesbenz.sechub.webserver.sechubaccess.SecHubAccessService;

/*
 * This test launches a real HTTP Server (Netty) during the test.
 *
 * The RANDOM_PORT setting starts the test application on an available port on the system.
 *
 * The operating system is responsible for allocating the port and it is guaranteed to be available (source: https://stackoverflow.com/a/48923117).
 * As a result, there should never be any conflict with the ports, even if tests are running in parallel.
 *
 * The random port is injected into the WebTestClient by default.
 * In addition, one can get the port using the <code>@LocalServerPort<code> annotation.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("webserver_test")
class SecHubWebServerApplicationSpringBootTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecHubAccessService mockAccessService;

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

    @Test
    void index() throws Exception {
        webTestClient.get().uri("/").exchange().expectStatus().isFound();
    }
}
