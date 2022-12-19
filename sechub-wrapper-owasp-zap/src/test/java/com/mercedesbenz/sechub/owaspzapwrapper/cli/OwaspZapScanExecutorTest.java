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
import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanConfiguration;
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
        OwaspZapScanConfiguration scanConfig = mock(OwaspZapScanConfiguration.class);
        ClientApi clientApi = mock(ClientApi.class);

        URI targetUri = new URI("http://www.example.com");
        when(scanConfig.getTargetUri()).thenReturn(targetUri);

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanConfig), any())).thenReturn(scan);
        when(clientApiFactory.create(scanConfig.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUri, null)).thenReturn(true);

        /* execute */
        executorToTest.execute(scanConfig);

        /* test */
        verify(connectionChecker).isTargetReachable(targetUri, null);
        verify(clientApiFactory).create(scanConfig.getServerConfig());
        verify(resolver).resolveScanImplementation(scanConfig, clientApi);
        verify(scan).scan();

    }

    @Test
    void target_is_not_reachable_throws_mustexitruntimeexception() throws Exception {
        /* prepare */
        OwaspZapScanConfiguration scanConfig = mock(OwaspZapScanConfiguration.class);
        ClientApi clientApi = mock(ClientApi.class);

        URI targetUri = new URI("http://www.my-url.com");
        when(scanConfig.getTargetUri()).thenReturn(targetUri);

        OwaspZapScan scan = mock(OwaspZapScan.class);
        when(resolver.resolveScanImplementation(eq(scanConfig), any())).thenReturn(scan);
        when(clientApiFactory.create(scanConfig.getServerConfig())).thenReturn(clientApi);
        when(connectionChecker.isTargetReachable(targetUri, null)).thenReturn(false);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> executorToTest.execute(scanConfig));

        verify(connectionChecker).isTargetReachable(targetUri, null);

        verify(scan, never()).scan();
        verify(clientApiFactory, never()).create(scanConfig.getServerConfig());
        verify(resolver, never()).resolveScanImplementation(scanConfig, clientApi);

    }
}
