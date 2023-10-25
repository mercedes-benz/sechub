package com.mercedesbenz.sechub.xraywrapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

class XrayAPIRequestTest {

    XrayAPIRequest xrayAPIRequest;

    @Test
    void xrayRequest_create_valid_request() {
        /* prepare */
        URL url = mock(URL.class);
        boolean auth = false;
        String data = "mydata";

        /* execute */
        xrayAPIRequest = XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.GET).setAuthentication(auth).setData(data).build();

        /* test */
        assertEquals(url, xrayAPIRequest.getUrl());
        assertEquals(auth, xrayAPIRequest.needAuthentication());
        assertEquals(data, xrayAPIRequest.getData());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, xrayAPIRequest.getRequestMethodEnum());
    }

    @Test
    void xrayRequest_throws_xrayWrapperRuntimeException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequest.Builder.create(null, null).build());
    }
}