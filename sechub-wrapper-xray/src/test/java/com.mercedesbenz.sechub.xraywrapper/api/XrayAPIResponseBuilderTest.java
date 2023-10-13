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
    HttpURLConnection con;
    ByteArrayInputStream is;

    @BeforeEach
    public void beforeEach() {
        con = mock(HttpURLConnection.class);
    }

    @Test
    public void test_getHttpResponseFromConnection() throws IOException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 200;
        is = new ByteArrayInputStream("testData".getBytes("UTF-8"));
        doReturn(is).when(con).getInputStream();
        doReturn(statusCode).when(con).getResponseCode();

        /* execute */
        response = getHttpResponseFromConnection(con, "filename");

        /* test */
        assertEquals("testData", response.getBody());
        assertEquals(200, response.getStatus_code());
    }

    @Test
    public void test_getHttpResponseFromConnection_empty() throws IOException {
        /* prepare */
        XrayAPIResponse response;

        /* execute */
        response = getHttpResponseFromConnection(con, "filename");

        /* test */
        assertEquals("", response.getBody());
        assertEquals(0, response.getStatus_code());
    }

    @Test
    public void test_getHttpResponseFromConnection_null() throws IOException {
        /* prepare */
        XrayAPIResponse response;

        /* execute + test */
        assertThrows(NullPointerException.class, () -> getHttpResponseFromConnection(null, null));
    }

    @Test
    public void test_getHttpResponseFromConnection_error() throws IOException {
        /* prepare */
        XrayAPIResponse response;
        int statusCode = 404;
        is = new ByteArrayInputStream("Error".getBytes("UTF-8"));
        doReturn(is).when(con).getErrorStream();
        doReturn(statusCode).when(con).getResponseCode();

        /* execute */
        response = getHttpResponseFromConnection(con, "filename");

        /* test */
        assertEquals("Error", response.getBody());
        assertEquals(404, response.getStatus_code());
    }

    @Test
    public void test_getHttpResponseFromConnection_XrayWrapperRuntimeException() throws IOException {
        /* prepare */
        HttpURLConnection con = mock(HttpURLConnection.class);
        IOException e = new IOException("error");
        Mockito.when(con.getResponseCode()).thenThrow(e);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> getHttpResponseFromConnection(con, null));
    }
}