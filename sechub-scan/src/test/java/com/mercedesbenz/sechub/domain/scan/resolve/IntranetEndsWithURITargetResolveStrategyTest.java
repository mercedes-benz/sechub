// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

public class IntranetEndsWithURITargetResolveStrategyTest {
    private static URI URI_EMPTY = URI.create("");
    private static URI URI_HTTPS_EXAMPLE_ORG = URI.create("https://www.example.org");
    private static URI URI_HTTP_SOMEWHERE_INTX_EXAMPLE_COM = URI.create("http://somewhere.intx.example.com");
    private static URI URI_FTP_SOMEWHERE_INTX_EXAMPLE_COM = URI.create("ftp://somewhere.intx.example.com");
    private static URI URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG = URI.create("https://somewhere.intranet.example.org");
    private static URI URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234 = URI.create("https://somewhere.intranet.example.org:1234");
    private static URI URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234_WITH_PATH = URI
            .create("https://somewhere.intranet.example.org:1234/myapplication/web/NAV/client.aspx");

    private IntranetEndsWithURITargetResolveStrategy strategyToTest;

    @Before
    public void before() {
        strategyToTest = new IntranetEndsWithURITargetResolveStrategy();
    }

    @Test
    public void healthcheck() {
        assertNotNull(URI_EMPTY);
    }

    @Test
    public void initialize_returned_result_as_expected() {
        assertTrue(strategyToTest.initialize("intranet-hostname-ends-with:xxx"));
        assertTrue(strategyToTest.initialize("intranet-hostname-ends-with: xxx"));
        assertTrue(strategyToTest.initialize("intranet-hostname-ends-with: xxx,yyy"));

        assertFalse(strategyToTest.initialize(""));
        assertFalse(strategyToTest.initialize(null));
        assertFalse(strategyToTest.initialize("xxx,yyy"));
        assertFalse(strategyToTest.initialize("ends-with xxx,yyy"));
        assertFalse(strategyToTest.initialize(" intranet-hostname-ends-with:xxx"));

        assertFalse(strategyToTest.initialize("intranet-hostname-ends-with:"));

    }

    @Test
    public void intranet_example_org_only() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-ends-with:intranet.example.org");

        /* test */
        assertEquals(new NetworkTarget(URI_EMPTY, NetworkTargetType.UNKNOWN), strategyToTest.resolveTargetFor(URI_EMPTY));
        assertEquals(new NetworkTarget(URI_HTTPS_EXAMPLE_ORG, NetworkTargetType.INTERNET), strategyToTest.resolveTargetFor(URI_HTTPS_EXAMPLE_ORG));
        assertEquals(new NetworkTarget(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG));
        assertEquals(new NetworkTarget(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234));
        assertEquals(new NetworkTarget(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234_WITH_PATH, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234_WITH_PATH));

        assertEquals(new NetworkTarget(URI_HTTP_SOMEWHERE_INTX_EXAMPLE_COM, NetworkTargetType.INTERNET),
                strategyToTest.resolveTargetFor(URI_HTTP_SOMEWHERE_INTX_EXAMPLE_COM));
        assertEquals(new NetworkTarget(URI_FTP_SOMEWHERE_INTX_EXAMPLE_COM, NetworkTargetType.INTERNET), strategyToTest.resolveTargetFor(URI_FTP_SOMEWHERE_INTX_EXAMPLE_COM));
    }

    @Test
    public void intranet_example_org__and_intx_example_com() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-ends-with:intranet.example.org, intx.example.com");

        /* test */
        assertEquals(new NetworkTarget(URI_EMPTY, NetworkTargetType.UNKNOWN), strategyToTest.resolveTargetFor(URI_EMPTY));
        assertEquals(new NetworkTarget(URI_HTTPS_EXAMPLE_ORG, NetworkTargetType.INTERNET), strategyToTest.resolveTargetFor(URI_HTTPS_EXAMPLE_ORG));
        assertEquals(new NetworkTarget(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG));
        assertEquals(new NetworkTarget(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_SOMEWHERE_INTRANET_EXAMPLE_ORG_PORT_1234));

        assertEquals(new NetworkTarget(URI_HTTP_SOMEWHERE_INTX_EXAMPLE_COM, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTP_SOMEWHERE_INTX_EXAMPLE_COM));
        assertEquals(new NetworkTarget(URI_FTP_SOMEWHERE_INTX_EXAMPLE_COM, NetworkTargetType.INTRANET), strategyToTest.resolveTargetFor(URI_FTP_SOMEWHERE_INTX_EXAMPLE_COM));

    }

}
