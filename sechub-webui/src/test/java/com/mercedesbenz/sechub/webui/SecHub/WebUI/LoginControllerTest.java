// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import com.mercedesbenz.sechub.webui.LoginController;

/*
 * No HTTP Server will be started for this test
 * for more details see: https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#mock-objects-web-reactive
 */
@WebFluxTest(controllers = LoginController.class)
public class LoginControllerTest {

//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private SecHubServerAccessService mockAccessService;
//
//    @Test
//    void login() throws Exception {
//        webTestClient.get().uri("/login").exchange().expectStatus().isOk();
//    }
}
