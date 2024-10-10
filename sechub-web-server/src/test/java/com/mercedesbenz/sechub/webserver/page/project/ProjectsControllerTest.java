// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page.project;

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
@WebFluxTest(controllers = ProjectsController.class)
public class ProjectsControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecHubAccessService mockAccessService;

    @MockBean
    private ProjectInfoService projectInfoService;

    @Test
    void index() throws Exception {
        webTestClient.get().uri("/").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void projects() throws Exception {
        webTestClient.get().uri("/projects").exchange().expectStatus().isUnauthorized();
    }
}
