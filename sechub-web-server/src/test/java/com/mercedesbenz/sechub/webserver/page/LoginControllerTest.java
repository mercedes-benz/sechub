// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mercedesbenz.sechub.webserver.sechubaccess.SecHubAccessService;

/*
 * No HTTP Server will be started for this test
 * for more details see: https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#mock-objects-web-reactive
 */
@WebFluxTest(controllers = LoginController.class)
public class LoginControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecHubAccessService mockAccessService;

    @Test
    void login() throws Exception {
        webTestClient.get().uri("/login").exchange().expectStatus().isOk();
    }
}
