package com.mercedesbenz.sechub.wrapper.xray.api;

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

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIHTTPUrlConnectionFactoryTest {

    URL url;

    XrayAPIHTTPUrlConnectionFactory factory;

    @BeforeEach
    void beforeEach() {
        url = mock(URL.class);
        factory = new XrayAPIHTTPUrlConnectionFactory();
    }

    @Test
    void setUpHTTPConnection_GET_request() throws IOException, XrayWrapperException {
        /* prepare */
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        XrayAPIRequest request = XrayAPIRequest.Builder.builder(url, XrayAPIRequest.RequestMethodEnum.GET).build();

        /* execute */
        HttpURLConnection connection = factory.create(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }

    @Test
    void setUpHTTPConnection_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> factory.create(null));
    }

    @Test
    void setUpHTTPConnection_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request = XrayAPIRequest.Builder.builder(url, XrayAPIRequest.RequestMethodEnum.GET).build();

        /* execute + test */
        assertThrows(NullPointerException.class, () -> factory.create(request));
    }

    @Test
    void setUpHTTPConnection_POST_request() throws IOException, XrayWrapperException {
        /* prepare */
        OutputStream outputStream = mock(OutputStream.class);
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        Mockito.when(mockedCon.getOutputStream()).thenReturn(outputStream);
        XrayAPIRequest request = XrayAPIRequest.Builder.builder(url, XrayAPIRequest.RequestMethodEnum.POST).isAuthenticationNeeded(false).buildJSONBody("{}")
                .build();

        /* execute */
        HttpURLConnection connection = factory.create(request);

        /* test */
        assertEquals(0, connection.getConnectTimeout());
    }
}