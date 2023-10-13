package com.mercedesbenz.sechub.xraywrapper.api;

import static com.mercedesbenz.sechub.xraywrapper.api.XrayAPIRequestExecutor.setUpGetConnection;
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

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

class XrayAPIRequestExecutorTest {

    URL url;

    @BeforeEach
    public void beforeEach() {
        url = mock(URL.class);
    }

    @Test
    public void test_setUpGetConnection_GET() throws IOException {
        /* prepare */
        HttpURLConnection con;
        String stringUrls = url.toString();
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        XrayAPIRequest request = new XrayAPIRequest(stringUrls, XrayAPIRequest.RequestMethodEnum.GET, false, "");
        request.setUrl(url);

        /* execute */
        con = setUpGetConnection(request);

        /* test */
        assertEquals(0, con.getConnectTimeout());
    }

    @Test
    public void test_setUpGetConnection_null() {
        /* execute */
        assertThrows(NullPointerException.class, () -> setUpGetConnection(null));
    }

    @Test
    public void test_setUpGetConnection_XrayWrapperRuntimeException() {
        /* prepare */
        XrayAPIRequest request = new XrayAPIRequest();

        /* execute */
        assertThrows(XrayWrapperRuntimeException.class, () -> setUpGetConnection(request));
    }

    @Test
    public void test_setUpGetConnection_POST() throws IOException {
        /* prepare */
        OutputStream os = mock(OutputStream.class);
        HttpURLConnection con;
        String stringUrls = url.toString();
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        Mockito.when(mockedCon.getOutputStream()).thenReturn(os);
        XrayAPIRequest request = new XrayAPIRequest(stringUrls, XrayAPIRequest.RequestMethodEnum.POST, false, "");
        request.setUrl(url);
        request.setData("{}");

        /* execute */
        con = setUpGetConnection(request);

        /* test */
        assertEquals(0, con.getConnectTimeout());
    }
}