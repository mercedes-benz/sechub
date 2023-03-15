// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

public class IntranetStartsWithURITargetResolveStrategyTest {
    private static URI URI_EMPTY = URI.create("");
    private static URI URI_HTTPS_INTRANET_EXAMPLE_ORG = URI.create("https://intranet.example.org");
    private static URI URI_HTTPS_10_5_1_200 = URI.create("https://10.5.1.200");
    private static URI URI_HTTPS_43_1_2_100 = URI.create("https://43.1.2.100");

    private IntranetStartsWithURITargetResolveStrategy strategyToTest;

    @BeforeEach
    void before() {
        strategyToTest = new IntranetStartsWithURITargetResolveStrategy();
    }

    @Test
    void healthcheck() {
        assertNotNull(URI_EMPTY);
    }

    @Test
    void initialize_returned_result_as_expected() {
        assertTrue(strategyToTest.initialize("intranet-hostname-starts-with:xxx"));
        assertTrue(strategyToTest.initialize("intranet-hostname-starts-with: xxx"));
        assertTrue(strategyToTest.initialize("intranet-hostname-starts-with: xxx,yyy"));

        assertFalse(strategyToTest.initialize(""));
        assertFalse(strategyToTest.initialize(null));
        assertFalse(strategyToTest.initialize("xxx,yyy"));
        assertFalse(strategyToTest.initialize("starts-with xxx,yyy"));
        assertFalse(strategyToTest.initialize(" intranet-hostname-starts-with:xxx"));

        assertFalse(strategyToTest.initialize("intranet-hostname-starts-with:"));

    }

    @Test
    void intranet_example_org_only() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-starts-with:intranet");

        /* test */
        assertEquals(new NetworkTarget(URI_EMPTY, NetworkTargetType.UNKNOWN), strategyToTest.resolveTargetFor(URI_EMPTY));
        assertEquals(new NetworkTarget(URI_HTTPS_INTRANET_EXAMPLE_ORG, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_INTRANET_EXAMPLE_ORG));
    }

    @Test
    void intranet_example_org__and_intx_example_com() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-starts-with:intranet.example.org, intranet.example.com");

        /* test */
        assertEquals(new NetworkTarget(URI_EMPTY, NetworkTargetType.UNKNOWN), strategyToTest.resolveTargetFor(URI_EMPTY));
        assertEquals(new NetworkTarget(URI_HTTPS_INTRANET_EXAMPLE_ORG, NetworkTargetType.INTRANET),
                strategyToTest.resolveTargetFor(URI_HTTPS_INTRANET_EXAMPLE_ORG));

    }

    @Test
    void intranet_example_with_IP_both_found() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-starts-with:10.5.,43.1.2");

        /* test */
        assertEquals(new NetworkTarget(URI_HTTPS_10_5_1_200, NetworkTargetType.INTRANET), strategyToTest.resolveTargetFor(URI_HTTPS_10_5_1_200));
        assertEquals(new NetworkTarget(URI_HTTPS_43_1_2_100, NetworkTargetType.INTRANET), strategyToTest.resolveTargetFor(URI_HTTPS_43_1_2_100));

    }

    @Test
    void intranet_example_with_IP_one_found() throws Exception {

        /* execute */
        strategyToTest.initialize("intranet-hostname-starts-with:10.5");

        /* test */
        assertEquals(new NetworkTarget(URI_HTTPS_10_5_1_200, NetworkTargetType.INTRANET), strategyToTest.resolveTargetFor(URI_HTTPS_10_5_1_200));
        // returns null if the strategy cannot resolve the target
        assertNull(strategyToTest.resolveTargetFor(URI_HTTPS_43_1_2_100));

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "192.168", "example", "10.5.2" })
    void intranet_example_not_starting_with(String initPart) throws Exception {

        /* execute */
        if (initPart != null) {
            strategyToTest.initialize("intranet-hostname-starts-with:" + initPart);
        }

        /* test */
        // returns null if the strategy cannot resolve the target
        assertNull(strategyToTest.resolveTargetFor(URI_HTTPS_10_5_1_200));
        assertNull(strategyToTest.resolveTargetFor(URI_HTTPS_43_1_2_100));

    }

}
