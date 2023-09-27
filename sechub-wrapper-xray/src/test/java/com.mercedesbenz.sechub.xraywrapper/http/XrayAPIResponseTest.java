package com.mercedesbenz.sechub.xraywrapper.http;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XrayAPIResponseTest {

    @Test
    public void testXrayAPIResponseEmpty() {
        /* prepare */
        XrayAPIResponse response;

        /* execute */
        response = new XrayAPIResponse();

        /* test */
        assertEquals(0, response.getStatus_code());
        assertEquals("", response.getBody());
    }

    @Test
    public void testXrayAPIResponse() {
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