// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

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
@ActiveProfiles("test")
class SecHubWebUiApplicationSpringBootTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private SecHubServerAccessService mockAccessService;
//
//    @MockBean
//    private CredentialService mockCredentialService;
//
//    @Test
//    void contextLoads(ApplicationContext context) {
//        assertNotNull(context);
//    }
//
//    @Test
//    void index() throws Exception {
//        webTestClient.get().uri("/").exchange().expectStatus().isFound();
//    }
}
