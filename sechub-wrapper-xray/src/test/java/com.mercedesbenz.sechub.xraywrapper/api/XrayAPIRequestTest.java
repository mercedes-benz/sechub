package com.mercedesbenz.sechub.xraywrapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

class XrayAPIRequestTest {

    XrayAPIRequest xrayAPIRequest;

    @Test
    public void test_xrayRequest() {
        /* prepare */
        String url = "myurl";
        boolean auth = false;
        String data = "mydata";

        /* execute */
        xrayAPIRequest = new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.GET, auth, data);

        /* test */
        assertEquals(url, xrayAPIRequest.getStringUrl());
        assertEquals(auth, xrayAPIRequest.needAuthentication());
        assertEquals(data, xrayAPIRequest.getData());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, xrayAPIRequest.getRequestMethodEnum());
    }

    @Test
    public void test_getUrl_null() {
        /* prepare */
        xrayAPIRequest = new XrayAPIRequest(null, null, false, null);

        /* execute + test */
        assertThrows(MalformedURLException.class, () -> xrayAPIRequest.getUrl());
    }
}