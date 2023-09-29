package com.mercedesbenz.sechub.xraywrapper.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(url, xrayAPIRequest.getBaseUrl());
        assertEquals(auth, xrayAPIRequest.needAuthentication());
        assertEquals(data, xrayAPIRequest.getData());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, xrayAPIRequest.getRequestMethodEnum());
    }

}