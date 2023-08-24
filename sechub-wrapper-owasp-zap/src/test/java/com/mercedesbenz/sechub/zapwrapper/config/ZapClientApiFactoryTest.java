// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.scan.ClientApiFacade;

class ZapClientApiFactoryTest {

    private ZapClientApiFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new ZapClientApiFactory();
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

        /* execute */
        ClientApiFacade clientApiFacade = factoryToTest.create(serverConfig);

        /* test */
        assertNotNull(clientApiFacade);
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

        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> factoryToTest.create(serverConfig));
    }

}
