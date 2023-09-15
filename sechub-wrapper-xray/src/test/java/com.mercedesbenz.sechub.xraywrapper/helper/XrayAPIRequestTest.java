package com.mercedesbenz.sechub.xraywrapper.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

class XrayAPIRequestTest {

    // todo: testing http requests with mockito

    @Test
    public void testSendRequest() {
        /*
         * // prepare XrayAPIRequest request = new XrayAPIRequest();
         * request.setBaseUrl(artifactoryUrl + "/xray/api/v1/system/version");
         * request.setRequestMethodEnum(XrayAPIRequest.RequestMethodEnum.GET);
         * XrayAPIResponse response;
         *
         * // execute try { response = request.sendRequest(); } catch (IOException e) {
         * throw new RuntimeException(e); }
         *
         * // assert assertNotNull(response);
         *
         */
    }

    @Test
    public void testSendRequestMalformedURL() {
        // prepare
        XrayAPIRequest request = new XrayAPIRequest();

        // execute and assert
        MalformedURLException thrown = assertThrows(MalformedURLException.class, request::sendRequest, "Malformed URL");
    }

}