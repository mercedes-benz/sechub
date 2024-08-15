// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.support;

import static org.junit.Assert.assertTrue;

import java.net.http.HttpClient;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SecretValidatorHttpClientFactoryTest {

    private SecretValidatorHttpClientFactory factoryToTest = new SecretValidatorHttpClientFactory();

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void create_proxy_http_client_returns_expected_http_client(boolean trustAllCertificates) {
        /* execute */
        HttpClient proxyHttpClient = factoryToTest.createProxyHttpClient(trustAllCertificates);

        /* test */
        assertTrue(proxyHttpClient.proxy().isPresent());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void create_direct_http_client_returns_expected_http_client(boolean trustAllCertificates) {
        /* execute */
        HttpClient proxyHttpClient = factoryToTest.createDirectHttpClient(trustAllCertificates);

        /* test */
        assertTrue(proxyHttpClient.proxy().isEmpty());
    }

}
