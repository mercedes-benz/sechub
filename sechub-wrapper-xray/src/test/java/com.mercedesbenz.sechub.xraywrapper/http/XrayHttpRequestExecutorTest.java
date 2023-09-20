package com.mercedesbenz.sechub.xraywrapper.http;

import static com.mercedesbenz.sechub.xraywrapper.http.XrayHttpRequestExecutor.setUpGetConnection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;

class XrayHttpRequestExecutorTest {

    URL url;

    @BeforeEach
    public void beforeEach() {
        url = mock(URL.class);
    }

    @Test
    public void testSetUpGetConnection() throws IOException {
        // prepare
        HttpURLConnection con;
        String stringUrls = url.toString();
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(mockedCon);
        XrayAPIRequest request = new XrayAPIRequest(stringUrls, XrayAPIRequest.RequestMethodEnum.GET, false, "");
        request.setUrl(url);

        // execute
        con = setUpGetConnection(request);

        // assert
        assertEquals(0, con.getConnectTimeout());
    }

}