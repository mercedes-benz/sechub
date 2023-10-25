package com.mercedesbenz.sechub.xraywrapper.api;

import static com.mercedesbenz.sechub.xraywrapper.api.XrayAPIRequestExecutor.setUpHTTPConnection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class XrayAPIRequestExecutorTest {

    URL url;

    @BeforeEach
    void beforeEach() {
        url = mock(URL.class);
    }

    @Test
    void setUpHTTPConnection_GET_request() throws IOException {
        /* prepare */
        HttpURLConnection connection;
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        XrayAPIRequest request = XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.GET).build();

        /* execute */
        connection = setUpHTTPConnection(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }

    @Test
    void setUpHTTPConnection_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> setUpHTTPConnection(null));
    }

    @Test
    void setUpHTTPConnection_throws_xrayWrapperRuntimeException() {
        /* prepare */
        XrayAPIRequest request = XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.GET).build();

        /* execute + test */
        assertThrows(NullPointerException.class, () -> setUpHTTPConnection(request));
    }

    @Test
    void setUpHTTPConnection_POST_request() throws IOException {
        /* prepare */
        OutputStream outputStream = mock(OutputStream.class);
        HttpURLConnection connection;
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        Mockito.when(mockedCon.getOutputStream()).thenReturn(outputStream);
        XrayAPIRequest request = XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.POST).setAuthentication(false).setData("{}").build();

        /* execute */
        connection = setUpHTTPConnection(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }
}