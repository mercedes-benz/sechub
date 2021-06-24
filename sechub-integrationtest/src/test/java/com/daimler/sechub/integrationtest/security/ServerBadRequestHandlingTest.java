// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.security;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.integrationtest.SecurityTestHelper;
import com.daimler.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

class ServerBadRequestHandlingTest {

    private static SecurityTestHelper securityTestHelper;
    private IntegrationTestContext context;

    @BeforeEach
    void beforeEach() {
        context = IntegrationTestContext.get();
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        String checkAlive = IntegrationTestContext.get().getUrlBuilder().buildCheckIsAliveUrl();
        securityTestHelper = new SecurityTestHelper(TestTargetType.SECHUB_SERVER, new URL(checkAlive));
    }

    @Test
    void sending_wrong_http_method_UPDATE_request_to_check_alive_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String checkAliveURL = context.getUrlBuilder().buildCheckIsAliveUrl();

        /* execute */
        securityTestHelper.sendCurlRequest(checkAliveURL, "UPDATE");

        /* test */
        /* @formatter:off */
        assertSecurityLog().
            hasEntries(1).
            entry(0).
                hasClientIp("127.0.0.1").
                hasRequestURI("/api/anonymous/check/alive").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "UPDATE").
                hasHTTPHeader("host","localhost:8443").
                hasHTTPHeader("content-type", "application/json");
        
        /* @formatter:on */
        
    }

    @Test
    void sending_wrong_http_method_bad_request_to_check_alive_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String checkAliveURL = context.getUrlBuilder().buildCheckIsAliveUrl();

        /* execute */
        securityTestHelper.sendCurlRequest(checkAliveURL, "bad_request");

        /* test */
        /* @formatter:off */
        assertSecurityLog().
            hasEntries(1).
            entry(0).
                hasClientIp("127.0.0.1").
                hasRequestURI("/api/anonymous/check/alive").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "bad_request").
                hasHTTPHeader("host","localhost:8443").
                hasHTTPHeader("content-type", "application/json");
        
        /* @formatter:on */
    }

    @Test
    void sending_wrong_http_method_bad_request_to_non_existing_url_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String nonExistingURL = context.getUrlBuilder().buildUrl("/i-am-not-existing");

        /* execute */
        securityTestHelper.sendCurlRequest(nonExistingURL, "bad_request");

        /* test */
        /* @formatter:off */
        assertSecurityLog().
            hasEntries(1).
            entry(0).
                hasClientIp("127.0.0.1").
                hasRequestURI("/i-am-not-existing").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "bad_request").
                hasHTTPHeader("host","localhost:8443").
                hasHTTPHeader("content-type", "application/json");
        
        /* @formatter:on */
    }
    

}
