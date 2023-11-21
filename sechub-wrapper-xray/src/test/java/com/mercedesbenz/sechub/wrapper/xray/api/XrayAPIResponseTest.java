package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIResponseTest {

    @Test
    void xrayAPIResponse_create_empty_body_response() throws XrayWrapperException {
        /* prepare */
        int status = 200;
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());

        /* execute */
        XrayAPIResponse response = XrayAPIResponse.Builder.builder().statusCode(status).headers(headers).build();

        /* test */
        assertEquals("", response.getBody());
    }

    @Test
    void xrayAPIResponse_create_valid_response_with_elements() throws XrayWrapperException {
        /* prepare */
        int status = 200;
        String body = "body";
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());
        List<String> values = Arrays.asList("elem", "elem2");
        headers.put("header", values);

        /* execute */
        XrayAPIResponse response = XrayAPIResponse.Builder.builder().statusCode(status).headers(headers).addResponseBody(body).build();

        /* test */
        assertEquals(200, response.getStatusCode());
        assertEquals("body", response.getBody());
        assertEquals(values, response.getHeaders().get("header"));
    }

    @Test
    void xrayAPIResponse_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIResponse.Builder.builder().statusCode(1).build());
    }
}