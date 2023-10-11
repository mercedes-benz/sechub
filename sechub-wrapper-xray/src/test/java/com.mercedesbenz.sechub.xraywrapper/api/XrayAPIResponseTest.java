package com.mercedesbenz.sechub.xraywrapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class XrayAPIResponseTest {

    // TODO

    @Test
    public void test_xrayAPIResponseEmpty() {
        /* prepare */
        XrayAPIResponse response;

        /* execute */
        response = new XrayAPIResponse();

        /* test */
        assertEquals(0, response.getStatus_code());
        assertEquals("", response.getBody());
    }

    @Test
    public void test_xrayAPIResponse() {
        /* prepare */
        XrayAPIResponse response;
        int status = 200;
        String body = "body";
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());
        List<String> values = Arrays.asList("elem", "elem2");
        headers.put("header", values);

        /* execute */
        response = new XrayAPIResponse();
        response.setBody(body);
        response.setStatus_code(status);
        response.setHeaders(headers);

        /* test */
        assertEquals(200, response.getStatus_code());
        assertEquals("body", response.getBody());
        assertEquals(values, response.getHeaders().get("header"));
    }
}