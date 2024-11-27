// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller spins up a mock API for testing the
 * {@link SecHubSecurityConfiguration} of the SecHub application.
 *
 * <p>
 * <b>Note:</b> The <i>sechub-shared-kernel</i> module is a library that does
 * not contain the actual implementation of the endpoints, hence a mock
 * controller is used here.
 * </p>
 *
 * @author hamidonos
 */
@RestController
class TestSecurityController {

    private static final String OK = HttpStatus.OK.getReasonPhrase();

    @GetMapping("/api/admin")
    String apiAdmin() {
        return OK;
    }

    @GetMapping("/api/user")
    String apiUser() {
        return OK;
    }

    @GetMapping("/api/project")
    String apiProject() {
        return OK;
    }

    @GetMapping("/api/project/{projectId}/false-positives")
    Set<String> apiProjectFalsePositives(@PathVariable("projectId") String _ignored) {
        return Set.of("false-positive-1", "false-positive-2", "false-positive-3");
    }

    @GetMapping("/api/owner")
    String apiOwner() {
        return OK;
    }

    @GetMapping("/api/anonymous")
    String apiAnonymous() {
        return OK;
    }

    @GetMapping("/error")
    String errorPage() {
        return OK;
    }

    @GetMapping("/actuator")
    String actuator() {
        return OK;
    }
}
