// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapClientApiFactory;
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
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

        URI targetUri = new URI("http://www.example.com");
        when(scanContext.getTargetUri()).thenReturn(targetUri);

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanContext), any())).thenReturn(scan);
        when(clientApiFactory.create(scanContext.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUri, null)).thenReturn(true);

        /* execute */
        executorToTest.execute(scanContext);

        /* test */
        verify(connectionChecker).isTargetReachable(targetUri, null);
        verify(clientApiFactory).create(scanContext.getServerConfig());
        verify(resolver).resolveScanImplementation(scanContext, clientApi);
        verify(scan).scan();

    }

    @Test
    void target_is_not_reachable_throws_mustexitruntimeexception() throws Exception {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        ClientApi clientApi = mock(ClientApi.class);

        URI targetUri = new URI("http://www.my-url.com");
        when(scanContext.getTargetUri()).thenReturn(targetUri);

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanContext), any())).thenReturn(scan);
        when(clientApiFactory.create(scanContext.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUri, null)).thenReturn(false);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> executorToTest.execute(scanContext));

        verify(connectionChecker).isTargetReachable(targetUri, null);

        verify(scan, never()).scan();
        verify(clientApiFactory, never()).create(scanContext.getServerConfig());
        verify(resolver, never()).resolveScanImplementation(scanContext, clientApi);

    }
}
