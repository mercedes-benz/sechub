// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.SecHub.WebUI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mercedesbenz.sechub.webui.LoginController;
import com.mercedesbenz.sechub.webui.SecHubServerAccessService;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = LoginController.class)
public class LoginControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SecHubServerAccessService mockAccessService;

    @Test
    void login() throws Exception {

        webTestClient.get().uri("/login").exchange().expectStatus().isOk();
    }
}
