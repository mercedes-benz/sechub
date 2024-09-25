// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mercedesbenz.sechub.test.TestPortProvider;

import wiremock.org.apache.hc.core5.http.HttpStatus;

/**
 * Junit 4 test because of missing official WireMock Junit5 extension - so we
 * use WireMock Rule and Junit4.
 *
 * @author Albert Tregnaghi
 *
 */
public class DefaultSechubClientWireMockTest {

    private static final String EXAMPLE_TOKEN = "example-token";

    private static final String EXAMPLE_USER = "example-user";
    private static final String APPLICATION_JSON = "application/json";

    private static final int HTTPS_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPSPort();

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT).httpsPort(HTTPS_PORT));

    @Test
    public void fetch_sechub_status_with_basic_auth() throws Exception {

        /* prepare */
        String statusBody = """
                [
                    {
                        "key": "status.scheduler.enabled",
                        "value": "true"
                    },
                    {
                        "key": "status.scheduler.jobs.all",
                        "value": "2"
                    },
                    {
                        "key": "status.scheduler.jobs.initializing",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.ready_to_start",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.started",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.cancel_requested",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.canceled",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.ended",
                        "value": "0"
                    }
                ]
                            """;
        stubFor(get(urlEqualTo("/api/admin/status")).withBasicAuth(EXAMPLE_USER, EXAMPLE_TOKEN)
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", APPLICATION_JSON).withBody(statusBody)));

        SecHubClient client = createTestClientWithExampleCredentials();

        /* execute */
        SecHubStatus status = client.fetchSecHubStatus();

        /* test */
        verify(getRequestedFor(urlEqualTo("/api/admin/status")));
        assertNotNull(status);
        assertTrue(status.getScheduler().isEnabled());
        assertEquals(2, status.getScheduler().getJobs().getAll());

    }

    @Test
    public void credential_changes_on_client_after_creation_are_possible() throws Exception {

        /* prepare */
        String statusBody = """
                [
                    {
                        "key": "status.scheduler.enabled",
                        "value": "false"
                    },
                    {
                        "key": "status.scheduler.jobs.all",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.initializing",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.ready_to_start",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.started",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.cancel_requested",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.canceled",
                        "value": "0"
                    },
                    {
                        "key": "status.scheduler.jobs.ended",
                        "value": "0"
                    }
                ]
                             """;
        stubFor(get(urlEqualTo("/api/admin/status")).withBasicAuth("other-user", "other-token")
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withHeader("Content-Type", APPLICATION_JSON).withBody(statusBody)));

        SecHubClient client = createTestClientWithExampleCredentials();

        /* execute */
        client.setApiToken("other-token");
        client.setUserId("other-user");

        /* test */
        SecHubStatus status = client.fetchSecHubStatus();
        verify(getRequestedFor(urlEqualTo("/api/admin/status")));

        assertFalse(status.getScheduler().isEnabled());
        assertEquals(0, status.getScheduler().getJobs().getAll());
    }

    private SecHubClient createTestClientWithExampleCredentials() {
        /* @formatter:off */
        return OldDefaultSecHubClient.builder().
                server(URI.create(wireMockRule.baseUrl())).
                user(EXAMPLE_USER).
                apiToken(EXAMPLE_TOKEN).
                trustAll(true).
               build();
        /* @formatter:on */
    }
}
