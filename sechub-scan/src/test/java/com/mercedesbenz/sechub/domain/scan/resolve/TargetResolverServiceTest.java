// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

public class TargetResolverServiceTest {

    @ParameterizedTest
    @CsvSource({ "abc,def,xyz,null,INTERNET,INTRANET,abc|xyz,1,0,1,INTRANET", "abc,def,xyz,null,INTERNET,INTRANET,xyz|abc,0,0,1,INTRANET",
            "abc,def,xyz,null,INTERNET,INTRANET,abc,1,0,0,INTERNET", "abc,def,xyz,null,INTERNET,INTRANET,def,0,1,0,INTERNET",
            "abc,def,xyz,null,INTRANET,INTRANET,def,0,1,0,INTRANET", "abc,def,xyz,null,UNKNOWN,INTRANET,def,0,1,0,UNKNOWN",
            "abc,def,xyz,null,UNKNOWN,INTRANET,abc|def,1,1,0,UNKNOWN", "abc,def,xyz,null,UNKNOWN,INTRANET,def|xyz,0,1,0,UNKNOWN",
            "abc,def,xyz,null,UNKNOWN,INTRANET,xyz|def|abc,0,0,1,INTRANET", "abc,def,xyz,null,INTERNET,INTRANET,unknown,0,0,0,INTERNET",
            "abc,def,xyz,null,INTERNET,INTRANET,,0,0,0,INTERNET", })
    void three_uri_strategies_two_defined_two_are_tried_fallback_is_internet(
    /* @formatter:off */
            String first,
            String second,
            String third,

            String firstType,
            String secondType,
            String thirdType,

            String definedStrategy,

            int times1,
            int times2,
            int times3,

            String expectedType) {
        /* @formatter:on */

        NetworkTargetType type1 = convertType(firstType);
        NetworkTargetType type2 = convertType(secondType);
        NetworkTargetType type3 = convertType(thirdType);

        NetworkTargetType expectedResultType = convertType(expectedType);

        URITargetResolveStrategy strategy1 = mock(URITargetResolveStrategy.class, first + "=" + firstType + ":" + times1);
        URITargetResolveStrategy strategy2 = mock(URITargetResolveStrategy.class, second + "=" + secondType + ":" + times2);
        URITargetResolveStrategy strategy3 = mock(URITargetResolveStrategy.class, third + "=" + thirdType + ":" + times3);

        when(strategy1.initialize(first)).thenReturn(true);
        when(strategy2.initialize(second)).thenReturn(true);
        when(strategy3.initialize(third)).thenReturn(true);

        /* prepare */
        serviceToTest.uriTargetResolveStrategies.add(strategy1);
        serviceToTest.uriTargetResolveStrategies.add(strategy2);
        serviceToTest.uriTargetResolveStrategies.add(strategy3);

        serviceToTest.definedUriStrategy = definedStrategy;

        URI uri = URI.create("https://productfailure.demo.example.org");
        if (type1 != null) {
            when(strategy1.resolveTargetFor(uri)).thenReturn(new NetworkTarget(uri, type1));
        }
        if (type2 != null) {
            when(strategy2.resolveTargetFor(uri)).thenReturn(new NetworkTarget(uri, type2));
        }
        if (type3 != null) {
            when(strategy3.resolveTargetFor(uri)).thenReturn(new NetworkTarget(uri, type3));
        }

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, expectedResultType), found);

        verify(strategy1, times(times1)).resolveTargetFor(uri);
        verify(strategy2, times(times2)).resolveTargetFor(uri);
        verify(strategy3, times(times3)).resolveTargetFor(uri);
    }

    private NetworkTargetType convertType(String typeAsString) {
        if (typeAsString == null || typeAsString.equals("null")) {
            return null;
        }
        return NetworkTargetType.valueOf(typeAsString);
    }

    @Test
    void no_strategies_defined_uri_resolved_as_INTRANET() {
        /* prepare */
        serviceToTest.definedUriStrategy = null;
        serviceToTest.definedInetAddressStrategy = null;
        URI uri = URI.create("https://productfailure.demo.example.org");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTERNET), found);
    }

    @Test
    void no_strategies_set_uri_resolved_as_INTRANET() {
        /* prepare */

        // simulate no strategies found, so jut new instance:
        serviceToTest = new TargetResolverService();
        // prepare defined strategies, so wrong configured...*/
        prepareDefinedStrategies();

        URI uri = URI.create("https://productfailure.demo.example.org");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(uri);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.INTERNET), found);

    }

    @Test
    void no_strategies_defined_ip_resolved_as_INTRANET() throws Exception {
        /* prepare */
        serviceToTest.definedUriStrategy = null;
        serviceToTest.definedInetAddressStrategy = null;
        /* prepare */
        InetAddress address = Inet4Address.getByName("172.217.22.99");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTERNET), found);
    }

    @Test
    void no_strategies_set_ip_resolved_as_INTRANET() throws Exception {
        /* prepare */

        // simulate no strategies found, so jut new instance:
        serviceToTest = new TargetResolverService();
        // prepare defined strategies, so wrong configured...*/
        prepareDefinedStrategies();

        /* prepare */
        InetAddress address = Inet4Address.getByName("172.217.22.99");

        /* execute */
        NetworkTarget found = serviceToTest.resolveTarget(address);

        /* test */
        assertEquals(new NetworkTarget(address, NetworkTargetType.INTERNET), found);

    }

    @Test
    void null_URI_is_resolved_as_unknown_without_strategy_call() throws Exception {
        /* null always handled by fallback strategy as unknown */
        assertEquals(new NetworkTarget((URI) null, NetworkTargetType.UNKNOWN), serviceToTest.resolveTarget((URI) null));

        verify(uriTestStrategy1, never()).resolveTargetFor(any());
    }

    @Test
    void null_IP_address_is_resolved_as_unknown_without_strategy_call() throws Exception {
        assertEquals(new NetworkTarget((InetAddress) null, NetworkTargetType.UNKNOWN), serviceToTest.resolveTarget((InetAddress) null));
        verify(ipTestStrategy1, never()).resolveTargetFor(any());
    }

    @Test
    void when_illegal_uri_target_detector_denies_uri_it_returns_target_but_illegal() throws Exception {
        /* prepare */
        URI uri = URI.create("illegal.example.com");
        when(illegalURITargetDetector.isIllegal(uri)).thenReturn(true);

        /* test */
        assertEquals(new NetworkTarget(uri, NetworkTargetType.ILLEGAL), serviceToTest.resolveTarget(uri));
        verify(illegalURITargetDetector).isIllegal(uri);
    }

    @Test
    void when_illegal_ip_target_detector_denies_ip_it_returns_target_but_illegal() throws Exception {

        /* prepare */
        InetAddress inetAddress = mock(InetAddress.class);
        when(illegalInetAdressTargetDetector.isIllegal(inetAddress)).thenReturn(true);

        /* execute + test */
        assertEquals(new NetworkTarget(inetAddress, NetworkTargetType.ILLEGAL), serviceToTest.resolveTarget(inetAddress));

    }

    @Test
    void inetaddress_strategy_result_is_used_as_result() throws Exception {
        /* execute + test */

        assertEquals(inetAddressTarget1, serviceToTest.resolveTarget(mock(InetAddress.class)));
    }

    @Test
    void uri_strategy_result_is_used_as_result() throws Exception {
        /* execute + test */
        assertEquals(uriTarget1, serviceToTest.resolveTarget(URI.create("https://example.com")));
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private TargetResolverService serviceToTest;

    private URITargetResolveStrategy uriTestStrategy1;
    private URITargetResolveStrategy uriTestStrategy2;
    private NetworkTarget uriTarget1;

    private IllegalInetAddressTargetDetector illegalInetAdressTargetDetector;
    private IllegalURItargetDetector illegalURITargetDetector;

    private InetAdressTargetResolveStrategy ipTestStrategy1;
    private InetAdressTargetResolveStrategy ipTestStrategy2;
    private NetworkTarget inetAddressTarget1;

    @BeforeEach
    void before() throws Exception {
        List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies = new ArrayList<>();
        List<URITargetResolveStrategy> uriAddressTargetResolveStrategies = new ArrayList<>();

        /* detectors for illegal targets */
        illegalInetAdressTargetDetector = mock(IllegalInetAddressTargetDetector.class);
        illegalURITargetDetector = mock(IllegalURItargetDetector.class);

        prepareURIStrategies(uriAddressTargetResolveStrategies);
        prepareIPStrategies(inetAddressTargetResolveStrategies);

        serviceToTest = new TargetResolverService();
        serviceToTest.illegalInetAddressTargetDetector = illegalInetAdressTargetDetector;
        serviceToTest.illegalURItargetDetector = illegalURITargetDetector;
        serviceToTest.uriTargetResolveStrategies = uriAddressTargetResolveStrategies;
        serviceToTest.inetAddressTargetResolveStrategies = inetAddressTargetResolveStrategies;

        prepareDefinedStrategies();
    }

    private void prepareDefinedStrategies() {
        serviceToTest.definedUriStrategy = "uri-test-strategy-1"; // we use always "1"
        serviceToTest.definedInetAddressStrategy = "ip-test-strategy-1";// we use always "1"
    }

    private void prepareIPStrategies(List<InetAdressTargetResolveStrategy> inetAddressTargetResolveStrategies) {

        /* inet inetAddress test strategies */
        ipTestStrategy1 = mock(InetAdressTargetResolveStrategy.class);
        ipTestStrategy2 = mock(InetAdressTargetResolveStrategy.class);

        inetAddressTarget1 = mock(NetworkTarget.class);

        when(ipTestStrategy1.initialize("ip-test-strategy-1")).thenReturn(true);
        when(ipTestStrategy1.resolveTargetFor(any())).thenReturn(inetAddressTarget1);

        when(ipTestStrategy2.initialize("ip-test-strategy-2")).thenReturn(true);
        when(ipTestStrategy2.resolveTargetFor(any())).thenReturn(null);

        inetAddressTargetResolveStrategies.add(ipTestStrategy1);
        inetAddressTargetResolveStrategies.add(ipTestStrategy2);

    }

    private void prepareURIStrategies(List<URITargetResolveStrategy> uriAddressTargetResolveStrategies) {
        /* uri test strategies */
        uriTestStrategy1 = mock(URITargetResolveStrategy.class);
        uriTestStrategy2 = mock(URITargetResolveStrategy.class);

        uriTarget1 = mock(NetworkTarget.class);

        when(uriTestStrategy1.initialize("uri-test-strategy-1")).thenReturn(true);
        when(uriTestStrategy1.resolveTargetFor(any())).thenReturn(uriTarget1);

        when(uriTestStrategy2.initialize("uri-test-strategy-2")).thenReturn(true);
        when(uriTestStrategy2.resolveTargetFor(any())).thenReturn(null);

        uriAddressTargetResolveStrategies.add(uriTestStrategy1);
        uriAddressTargetResolveStrategies.add(uriTestStrategy2);
    }

}
