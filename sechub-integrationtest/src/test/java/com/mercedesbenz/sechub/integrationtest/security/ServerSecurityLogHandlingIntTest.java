// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.security;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.mercedesbenz.sechub.integrationtest.SecurityTestHelper;
import com.mercedesbenz.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.test.TestPortProvider;

/* We need a running PDS instance - execute this test only when integration test server is running:*/
@EnabledIfSystemProperty(named = "sechub.integrationtest.running", matches = "true")
class ServerSecurityLogHandlingIntTest {

    private static SecurityTestHelper securityTestHelper;
    private IntegrationTestContext context;
    private int serverPort;

    @BeforeEach
    void beforeEach() {
        context = IntegrationTestContext.get();
        serverPort = TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestServerPort();
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
                hasOneOfGivenClientIps("127.0.0.1","0:0:0:0:0:0:0:1").
                hasRequestURI("/api/anonymous/check/alive").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "UPDATE").
                hasHTTPHeader("host","localhost:"+serverPort).
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
                hasOneOfGivenClientIps("127.0.0.1","0:0:0:0:0:0:0:1").
                hasRequestURI("/api/anonymous/check/alive").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "bad_request").
                hasHTTPHeader("host","localhost:"+serverPort).
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
                hasOneOfGivenClientIps("127.0.0.1","0:0:0:0:0:0:0:1").
                hasRequestURI("/i-am-not-existing").
                hasMessageContaining("Rejected request, reason").
                hasMessageParameterContainingStrings(0, "bad_request").
                hasHTTPHeader("host","localhost:"+serverPort).
                hasHTTPHeader("content-type", "application/json");

        /* @formatter:on */
    }

    @Test
    void sending_http_method_GET_to_non_existing_url_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String nonExistingURL = context.getUrlBuilder().buildUrl("/i-am-not-existing");

        /* execute */
        securityTestHelper.sendCurlRequest(nonExistingURL, "GET");

        /* test */
        /* @formatter:off */
        assertSecurityLog().
            hasEntries(1).
            entry(0).
                hasOneOfGivenClientIps("127.0.0.1","0:0:0:0:0:0:0:1").
                hasRequestURI("/i-am-not-existing").
                hasMessageContaining("401").
                hasHTTPHeader("host","localhost:"+serverPort).
                hasHTTPHeader("content-type", "application/json");

        /* @formatter:on */
    }

}
