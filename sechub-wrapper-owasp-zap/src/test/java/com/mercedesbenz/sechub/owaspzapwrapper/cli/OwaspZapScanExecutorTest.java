// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.helper.OwaspZapProductMessageHelper;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScan;
import com.mercedesbenz.sechub.owaspzapwrapper.util.TargetConnectionChecker;

class OwaspZapScanExecutorTest {

    private OwaspZapScanExecutor executorToTest;

    private OwaspZapClientApiFactory clientApiFactory;
    private OwaspZapScanResolver resolver;
    private TargetConnectionChecker connectionChecker;

    @BeforeEach
    void beforeEach() {
        executorToTest = new OwaspZapScanExecutor();

        clientApiFactory = mock(OwaspZapClientApiFactory.class);
        resolver = mock(OwaspZapScanResolver.class);
        connectionChecker = mock(TargetConnectionChecker.class);

        executorToTest.clientApiFactory = clientApiFactory;
        executorToTest.resolver = resolver;
        executorToTest.connectionChecker = connectionChecker;
    }

    @Test
    void the_result_from_resolver_returned_is_executed() throws Exception {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        ClientApi clientApi = mock(ClientApi.class);

        URL targetUrl = new URL("http://www.example.com");
        Set<URL> includeList = new HashSet<>();
        includeList.add(targetUrl);

        when(scanContext.getTargetUrl()).thenReturn(targetUrl);
        when(scanContext.getOwaspZapURLsIncludeList()).thenReturn(includeList);
        when(scanContext.getMaxNumberOfConnectionRetries()).thenReturn(1);
        when(scanContext.getRetryWaittimeInMilliseconds()).thenReturn(0);

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanContext), any())).thenReturn(scan);
        when(clientApiFactory.create(scanContext.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUrl, null)).thenReturn(true);

        /* execute */
        executorToTest.execute(scanContext);

        /* test */
        verify(connectionChecker).isTargetReachable(targetUrl, null);
        verify(clientApiFactory).create(scanContext.getServerConfig());
        verify(resolver).resolveScanImplementation(scanContext, clientApi);
        verify(scan).scan();

    }

    @Test
    void target_is_not_reachable_throws_mustexitruntimeexception() throws Exception {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        OwaspZapProductMessageHelper productMessageHelper = mock(OwaspZapProductMessageHelper.class);

        ClientApi clientApi = mock(ClientApi.class);

        URL targetUrl = new URL("http://www.my-url.com");

        Set<URL> includeList = new HashSet<>();
        includeList.add(targetUrl);
        when(scanContext.getOwaspZapURLsIncludeList()).thenReturn(includeList);
        when(scanContext.getMaxNumberOfConnectionRetries()).thenReturn(1);
        when(scanContext.getRetryWaittimeInMilliseconds()).thenReturn(0);
        when(scanContext.getOwaspZapProductMessageHelper()).thenReturn(productMessageHelper);
        doNothing().when(productMessageHelper).writeSingleProductMessage(any());

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanContext), any())).thenReturn(scan);
        when(clientApiFactory.create(scanContext.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUrl, null)).thenReturn(false);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> executorToTest.execute(scanContext));

        verify(connectionChecker).isTargetReachable(targetUrl, null);
        verify(scan, never()).scan();
        verify(clientApiFactory, never()).create(scanContext.getServerConfig());
        verify(resolver, never()).resolveScanImplementation(scanContext, clientApi);

    }
}
