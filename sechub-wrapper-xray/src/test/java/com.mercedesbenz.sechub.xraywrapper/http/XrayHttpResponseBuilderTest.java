package com.mercedesbenz.sechub.xraywrapper.http;

import static com.mercedesbenz.sechub.xraywrapper.http.XrayHttpResponseBuilder.getHttpResponseFromConnection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;

class XrayHttpResponseBuilderTest {

    @Mock
    HttpURLConnection con;
    ByteArrayInputStream is;

    @BeforeEach
    public void beforeEach() {
        con = mock(HttpURLConnection.class);
    }

    @Test
    public void testGetHttpResponseFromConnection() throws IOException {
        // prepare
        XrayAPIResponse response;
        int statusCode = 200;
        is = new ByteArrayInputStream("testData".getBytes("UTF-8"));
        doReturn(is).when(con).getInputStream();
        doReturn(statusCode).when(con).getResponseCode();

        // execute
        response = getHttpResponseFromConnection(con, "filename");

        // assert
        assertEquals("testData", response.getBody());
        assertEquals(200, response.getStatus_code());
    }

    @Test
    public void testEmptyGetHttpResponseFromConnection() throws IOException {
        // prepare
        XrayAPIResponse response;

        // execute
        response = getHttpResponseFromConnection(con, "filename");

        // assert
        assertEquals("", response.getBody());
        assertEquals(0, response.getStatus_code());
    }

    @Test
    public void testErrorGetHttpResponseFromConnection() throws IOException {
        // prepare
        XrayAPIResponse response;
        int statusCode = 404;
        is = new ByteArrayInputStream("Error".getBytes("UTF-8"));
        doReturn(is).when(con).getErrorStream();
        doReturn(statusCode).when(con).getResponseCode();

        // execute
        response = getHttpResponseFromConnection(con, "filename");

        // assert
        assertEquals("Error", response.getBody());
        assertEquals(404, response.getStatus_code());
    }
}