package com.mercedesbenz.sechub.xraywrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class XrayAPIRequestTest {

    @Test
    public void testXrayRequest() {
        // prepare
        XrayAPIRequest xrayAPIRequest;
        String url = "myurl";
        boolean auth = false;
        String data = "mydata";

        // execute
        xrayAPIRequest = new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.GET, auth, data);

        // assert
        assertEquals(url, xrayAPIRequest.getBaseUrl());
        assertEquals(auth, xrayAPIRequest.needAuthentication());
        assertEquals(data, xrayAPIRequest.getData());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, xrayAPIRequest.getRequestMethodEnum());
    }

}