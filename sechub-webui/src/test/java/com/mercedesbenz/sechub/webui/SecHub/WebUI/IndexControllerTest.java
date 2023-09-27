// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.mercedesbenz.sechub.webui.IndexController;
import com.mercedesbenz.sechub.webui.configuration.SecHubAccessService;

/*
 * No HTTP Server will be started for this test
 * for more details see: https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#mock-objects-web-reactive
 */
@WebFluxTest(controllers = IndexController.class)
public class IndexControllerTest {
    @MockBean
    private SecHubAccessService mockAccessService;

//    @Test
//    void index() throws Exception {
//        webTestClient.get().uri("/").exchange().expectStatus().isUnauthorized();
//    }
}
