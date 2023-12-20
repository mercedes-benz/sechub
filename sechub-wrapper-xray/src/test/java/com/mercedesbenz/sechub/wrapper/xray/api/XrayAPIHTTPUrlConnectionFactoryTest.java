// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIHTTPUrlConnectionFactoryTest {

    URL url;

    XrayAPIHTTPUrlConnectionFactory urlConnectionfactoryToTest;

    @BeforeEach
    void beforeEach() {
        url = mock(URL.class);
        urlConnectionfactoryToTest = new XrayAPIHTTPUrlConnectionFactory();
    }

    @Test
    void factoryHTTPConnection_GET_request() throws IOException, XrayWrapperException {
        /* prepare */
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        XrayAPIRequest request = XrayAPIRequest.Builder.builder().url(url).build();

        /* execute */
        HttpURLConnection connection = urlConnectionfactoryToTest.create(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }

    @Test
    void factoryHTTPConnection_create_invalid_GET_throws_xrayWrapperException() throws XrayWrapperException, MalformedURLException {
        /* prepare */
        URL myUrl = new URL("https://invalid-url-example.com");
        XrayAPIRequest request = XrayAPIRequest.Builder.builder().url(myUrl).build();

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> urlConnectionfactoryToTest.create(request));

        assertEquals("Could not open connection to Artifactory", exception.getMessage());
    }

    @Test
    void factoryHTTPConnection_create_invalid_POST_throws_xrayWrapperException() throws XrayWrapperException, MalformedURLException {
        /* prepare */
        URL myUrl = new URL("https://invalid-url-example.com");
        XrayAPIRequest request = XrayAPIRequest.Builder.builder().url(myUrl).requestMethod(XrayAPIRequest.RequestMethodEnum.POST).build();

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> urlConnectionfactoryToTest.create(request));

        assertEquals("Could not get Output Stream for api connection", exception.getMessage());
    }

    @Test
    void factoryHTTPConnection_POST_request() throws IOException, XrayWrapperException {
        /* prepare */
        OutputStream outputStream = mock(OutputStream.class);
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        Mockito.when(mockedCon.getOutputStream()).thenReturn(outputStream);
        XrayAPIRequest request = XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.POST).jsonBody("{}").build();

        /* execute */
        HttpURLConnection connection = urlConnectionfactoryToTest.create(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }
}