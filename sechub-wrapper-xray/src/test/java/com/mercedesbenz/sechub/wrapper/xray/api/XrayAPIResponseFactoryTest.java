package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIResponseFactoryTest {

    @Mock
    HttpURLConnection connection;
    ByteArrayInputStream inputStream;

    XrayAPIResponseFactory xrayAPIResponseFactory;

    @BeforeEach
    void beforeEach() {
        connection = mock(HttpURLConnection.class);
        xrayAPIResponseFactory = new XrayAPIResponseFactory();
    }

    @Test
    void getHttpResponseFromConnection_get_valid_http_response() throws IOException, XrayWrapperException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 200;
        inputStream = new ByteArrayInputStream("testData".getBytes("UTF-8"));
        doReturn(inputStream).when(connection).getInputStream();
        doReturn(statusCode).when(connection).getResponseCode();

        /* execute */
        response = xrayAPIResponseFactory.createHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("testData", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void getHttpResponseFromConnection_get_default_response() throws IOException, XrayWrapperException {
        /* prepare */
        XrayAPIResponse response;

        /* execute */
        response = xrayAPIResponseFactory.createHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("", response.getBody());
        assertEquals(0, response.getStatusCode());
    }

    @Test
    void getHttpResponseFromConnection_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> xrayAPIResponseFactory.createHttpResponseFromConnection(null, null));
    }

    @Test
    void getHttpResponseFromConnection_get_valid_error_http_response() throws IOException, XrayWrapperException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 404;
        inputStream = new ByteArrayInputStream("Error".getBytes("UTF-8"));
        doReturn(inputStream).when(connection).getErrorStream();
        doReturn(statusCode).when(connection).getResponseCode();

        /* execute */
        response = xrayAPIResponseFactory.createHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("Error", response.getBody());
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void getHttpResponseFromConnection_throws_xrayWrapperException() throws IOException {
        /* prepare */
        IOException e = new IOException("error");
        Mockito.when(connection.getResponseCode()).thenThrow(e);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIResponseFactory.createHttpResponseFromConnection(connection, null));
    }
}