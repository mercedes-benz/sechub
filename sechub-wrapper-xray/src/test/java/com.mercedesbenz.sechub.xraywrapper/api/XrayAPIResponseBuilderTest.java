package com.mercedesbenz.sechub.xraywrapper.api;

import static com.mercedesbenz.sechub.xraywrapper.api.XrayAPIResponseBuilder.getHttpResponseFromConnection;
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

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

class XrayAPIResponseBuilderTest {

    @Mock
    HttpURLConnection connection;
    ByteArrayInputStream inputStream;

    @BeforeEach
    void beforeEach() {
        connection = mock(HttpURLConnection.class);
    }

    @Test
    void test_getHttpResponseFromConnection() throws IOException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 200;
        inputStream = new ByteArrayInputStream("testData".getBytes("UTF-8"));
        doReturn(inputStream).when(connection).getInputStream();
        doReturn(statusCode).when(connection).getResponseCode();

        /* execute */
        response = getHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("testData", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void test_getHttpResponseFromConnection_empty() throws IOException {
        /* prepare */
        XrayAPIResponse response;

        /* execute */
        response = getHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("", response.getBody());
        assertEquals(0, response.getStatusCode());
    }

    @Test
    void test_getHttpResponseFromConnection_null() throws IOException {
        /* prepare */
        XrayAPIResponse response;

        /* execute + test */
        assertThrows(NullPointerException.class, () -> getHttpResponseFromConnection(null, null));
    }

    @Test
    void test_getHttpResponseFromConnection_error() throws IOException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 404;
        inputStream = new ByteArrayInputStream("Error".getBytes("UTF-8"));
        doReturn(inputStream).when(connection).getErrorStream();
        doReturn(statusCode).when(connection).getResponseCode();

        /* execute */
        response = getHttpResponseFromConnection(connection, "filename");

        /* test */
        assertEquals("Error", response.getBody());
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void test_getHttpResponseFromConnection_XrayWrapperRuntimeException() throws IOException {
        /* prepare */
        IOException e = new IOException("error");
        Mockito.when(connection.getResponseCode()).thenThrow(e);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> getHttpResponseFromConnection(connection, null));
    }
}