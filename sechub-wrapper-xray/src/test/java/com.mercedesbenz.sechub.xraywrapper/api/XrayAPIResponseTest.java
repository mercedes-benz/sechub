package com.mercedesbenz.sechub.xraywrapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class XrayAPIResponseTest {

    @Test
    void test_xrayAPIResponse_empty() {
        /* prepare */
        XrayAPIResponse response;
        int status = 200;
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());

        /* execute */
        response = XrayAPIResponse.Builder.create(status, headers).build();

        /* test */
        assertEquals("", response.getBody());
    }

    @Test
    void test_xrayAPIResponse() {
        /* prepare */
        XrayAPIResponse response;
        int status = 200;
        String body = "body";
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());
        List<String> values = Arrays.asList("elem", "elem2");
        headers.put("header", values);

        /* execute */
        response = XrayAPIResponse.Builder.create(status, headers).setBody(body).build();

        /* test */
        assertEquals(200, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals(values, response.getHeaders().get("header"));
    }
}