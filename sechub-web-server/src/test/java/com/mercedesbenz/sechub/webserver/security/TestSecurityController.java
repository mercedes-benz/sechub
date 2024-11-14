// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller spins up a mock API for testing the
 * {@link SecurityConfiguration} of the SecHub Web UI server application.
 *
 * @author hamidonos
 */
@RestController
class TestSecurityController {

    private static final String OK = HttpStatus.OK.getReasonPhrase();

    @GetMapping("/actuator")
    String actuator() {
        return OK;
    }

    @GetMapping("/login")
    String login() {
        return OK;
    }

    @GetMapping("/home")
    String home() {
        return OK;
    }

    @GetMapping("/css")
    String css() {
        return OK;
    }

    @GetMapping("/js")
    String js() {
        return OK;
    }

    @GetMapping("/images")
    String images() {
        return OK;
    }

    @GetMapping("/oauth2")
    String oauth2() {
        return OK;
    }

    @GetMapping("/sechub-logo.svg")
    String sechubLogoSvg() {
        return OK;
    }

    @GetMapping("/error")
    String errorPage() {
        return OK;
    }
}
