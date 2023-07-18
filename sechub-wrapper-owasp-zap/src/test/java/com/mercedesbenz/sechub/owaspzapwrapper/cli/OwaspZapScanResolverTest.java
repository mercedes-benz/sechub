// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.zaproxy.clientapi.core.ClientApi;

import com.mercedesbenz.sechub.owaspzapwrapper.config.OwaspZapScanContext;
import com.mercedesbenz.sechub.owaspzapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.OwaspZapScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.UnauthenticatedScan;
import com.mercedesbenz.sechub.owaspzapwrapper.scan.auth.HTTPBasicAuthScan;

class OwaspZapScanResolverTest {

    private OwaspZapScanResolver resolverToTest;

    @BeforeEach
    void beforeEach() {
        resolverToTest = new OwaspZapScanResolver();
    }

    @Test
    void unauthenticated_scan_is_resolved_correctly() {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        when(scanContext.getAuthenticationType()).thenReturn(AuthenticationType.UNAUTHENTICATED);
        ClientApi clientApi = mock(ClientApi.class);

        /* execute */
        OwaspZapScan scan = resolverToTest.resolveScanImplementation(scanContext, clientApi);

        /* test */
        assertTrue(scan instanceof UnauthenticatedScan);
    }

    @Test
    void http_basic_authentication_scan_is_resolved_correctly() {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        when(scanContext.getAuthenticationType()).thenReturn(AuthenticationType.HTTP_BASIC_AUTHENTICATION);
        ClientApi clientApi = mock(ClientApi.class);

        /* execute */
        OwaspZapScan scan = resolverToTest.resolveScanImplementation(scanContext, clientApi);

        /* test */
        assertTrue(scan instanceof HTTPBasicAuthScan);
    }

    @Test
    void authenticationtype_null_is_throwing_mustexitruntimeexception() {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        when(scanContext.getAuthenticationType()).thenReturn(null);
        ClientApi clientApi = mock(ClientApi.class);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> resolverToTest.resolveScanImplementation(scanContext, clientApi));
    }

    @ParameterizedTest
    @EnumSource(value = AuthenticationType.class, names = { "FORM_BASED_AUTHENTICATION", "SCRIPT_BASED_AUTHENTICATION", "JSON_BASED_AUTHENTICATION" })
    void not_yet_supported_authenticationtype_is_throwing_mustexitruntimeexception(AuthenticationType authType) {
        /* prepare */
        OwaspZapScanContext scanContext = mock(OwaspZapScanContext.class);
        when(scanContext.getAuthenticationType()).thenReturn(authType);
        ClientApi clientApi = mock(ClientApi.class);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> resolverToTest.resolveScanImplementation(scanContext, clientApi));
    }

}
