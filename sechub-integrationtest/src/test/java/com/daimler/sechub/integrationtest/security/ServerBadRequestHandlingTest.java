// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.security;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.integrationtest.SecurityTestHelper;
import com.daimler.sechub.integrationtest.SecurityTestHelper.TestTargetType;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.sharedkernel.logging.IntegrationTestSecurityLogEntry;

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
        List<IntegrationTestSecurityLogEntry> list = TestAPI.getSecurityLogs();
        assertEquals(1, list.size());

        IntegrationTestSecurityLogEntry logEntry = list.iterator().next();
        assertEquals("Rejected request, remote address={}, uri={}, reason={}", logEntry.message);
        List<Object> objects = logEntry.objects;
        assertEquals(3, objects.size());

        // test log parameters
        Iterator<Object> iterator = objects.iterator();
        Object firstParam = iterator.next();
        assertEquals("127.0.0.1", firstParam);

        Object secondParam = iterator.next();
        assertEquals(secondParam, "/api/anonymous/check/alive");

        Object thirdParam = iterator.next();
        assertTrue(thirdParam.toString().contains("rejected because"));
        assertTrue(thirdParam.toString().contains("UPDATE"));

    }

    @Test
    void sending_wrong_http_method_bad_request_to_check_alive_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String checkAliveURL = context.getUrlBuilder().buildCheckIsAliveUrl();

        /* execute */
        securityTestHelper.sendCurlRequest(checkAliveURL, "bad_request");

        /* test */
        List<IntegrationTestSecurityLogEntry> list = TestAPI.getSecurityLogs();
        assertEquals(1, list.size());

        IntegrationTestSecurityLogEntry logEntry = list.iterator().next();
        assertEquals("Rejected request, remote address={}, uri={}, reason={}", logEntry.message);
        List<Object> objects = logEntry.objects;
        assertEquals(3, objects.size());

        // test log parameters
        Iterator<Object> iterator = objects.iterator();
        Object firstParam = iterator.next();
        assertEquals("127.0.0.1", firstParam);

        Object secondParam = iterator.next();
        assertEquals("/api/anonymous/check/alive", secondParam);

        Object thirdParam = iterator.next();
        assertTrue(thirdParam.toString().contains("rejected because"));
        assertTrue(thirdParam.toString().contains("bad_request"));

    }

    @Test
    void sending_wrong_http_method_bad_request_to_non_existing_url_will_be_logged_by_security_log_service() throws Exception {
        /* prepare */
        TestAPI.clearSecurityLogs();
        String nonExistingURL = context.getUrlBuilder().buildUrl("/i-am-not-existing");

        /* execute */
        securityTestHelper.sendCurlRequest(nonExistingURL, "bad_request");

        /* test */
        List<IntegrationTestSecurityLogEntry> list = TestAPI.getSecurityLogs();
        assertEquals(1, list.size());

        IntegrationTestSecurityLogEntry logEntry = list.iterator().next();
        assertEquals("Rejected request, remote address={}, uri={}, reason={}", logEntry.message);
        List<Object> objects = logEntry.objects;
        assertEquals(3, objects.size());

        // test log parameters
        Iterator<Object> iterator = objects.iterator();
        Object firstParam = iterator.next();
        assertEquals("127.0.0.1", firstParam);

        Object secondParam = iterator.next();
        assertEquals("/i-am-not-existing", secondParam);

        Object thirdParam = iterator.next();
        assertTrue(thirdParam.toString().contains("rejected because"));
        assertTrue(thirdParam.toString().contains("bad_request"));

    }

}
