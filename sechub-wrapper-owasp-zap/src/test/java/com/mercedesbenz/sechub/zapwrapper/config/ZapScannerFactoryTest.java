// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner;

class ZapScannerFactoryTest {

    private ZapScannerFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new ZapScannerFactory();
    }

    @Test
    void server_config_is_null_throws_mustexcitruntimeexception() throws ClientApiException {
        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void valid_configuration_returns_clientapi_object() throws ClientApiException {
        /* prepare */
        ZapServerConfiguration serverConfig = new ZapServerConfiguration("127.0.0.1", 8080, "secret-key");

        ZapScanContext scanContext = mock(ZapScanContext.class);
        when(scanContext.getServerConfig()).thenReturn(serverConfig);

        /* execute */
        ZapScanner zapScanner = factoryToTest.create(scanContext);

        /* test */
        assertNotNull(zapScanner);
    }

    /* @formatter:off */
	@ParameterizedTest
	@CsvSource({
		"127.0.0.1,8888,",
		"localhost,0,secret",
		",7777,change-me",
	})
	/* @formatter:on */
    void configuration_where_one_field_is_null_or_invalid_throws_mustexitruntimeexception(String host, int port, String apiKey) throws ClientApiException {
        /* prepare */
        ZapServerConfiguration serverConfig = new ZapServerConfiguration(host, port, apiKey);

        ZapScanContext scanContext = mock(ZapScanContext.class);
        when(scanContext.getServerConfig()).thenReturn(serverConfig);

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(scanContext));
    }

}
