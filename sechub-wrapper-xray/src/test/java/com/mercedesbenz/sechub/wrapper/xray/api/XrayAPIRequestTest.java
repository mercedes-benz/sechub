package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIRequestTest {

    @Test
    void xrayRequest_create_valid_request() throws XrayWrapperException {
        /* prepare */
        URL url = mock(URL.class);
        boolean auth = false;
        String data = "mydata";

        /* execute */
        XrayAPIRequest request = XrayAPIRequest.Builder.builder(url, XrayAPIRequest.RequestMethodEnum.GET).isAuthenticationNeeded(auth).buildJSONBody(data)
                .build();

        /* test */
        assertEquals(url, request.getUrl());
        assertEquals(auth, request.isAuthenticationNeeded());
        assertEquals(data, request.getData());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
    }

    @Test
    void xrayRequest_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequest.Builder.builder(null, null).build());
    }
}