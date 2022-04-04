// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

class OwaspZapClientApiFactoryTest {

    private OwaspZapClientApiFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new OwaspZapClientApiFactory();
    }

    @Test
    void server_config_is_null_throws_mustexcitruntimeexception() throws ClientApiException {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(null));
    }

    @Test
    void valid_configuration_returns_clientapi_object() throws ClientApiException {
        /* prepare */
        OwaspZapServerConfiguration serverConfig = new OwaspZapServerConfiguration("127.0.0.1", 8080, "secret-key");

        /* execute */
        ClientApi clientApi = factoryToTest.create(serverConfig);

        /* test */
        assertNotNull(clientApi);
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
        OwaspZapServerConfiguration serverConfig = new OwaspZapServerConfiguration(host, port, apiKey);

        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> factoryToTest.create(serverConfig));
    }

}
