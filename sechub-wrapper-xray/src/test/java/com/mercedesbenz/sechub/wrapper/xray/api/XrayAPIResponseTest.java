// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class XrayAPIResponseTest {

    @Test
    void xrayAPIResponse_create_empty_body_response() throws XrayWrapperException {
        /* prepare */
        int status = 200;
        Map<String, List<String>> headers = new java.util.HashMap<>(Collections.emptyMap());

        /* execute */
        XrayAPIResponse response = XrayAPIResponse.Builder.builder().httpStatusCode(status).headers(headers).build();

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
        XrayAPIResponse response = XrayAPIResponse.Builder.builder().httpStatusCode(status).headers(headers).addResponseBody(body).build();

        /* test */
        assertEquals(status, response.getHttpStatusCode());
        assertEquals(body, response.getBody());
        assertEquals(values, response.getHeaders().get("header"));
    }

    @Test
    void xrayAPIResponse_null_headers_throws_xrayWrapperException() {
        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> XrayAPIResponse.Builder.builder().httpStatusCode(1).build());

        /* test */
        assertEquals("HTTP response headers cannot be null!", exception.getMessage());
    }

    @Test
    void xrayAPIResponse_invalid_responseCode__throws_xrayWrapperException() {
        /* prepare */
        Map<String, List<String>> headers = new HashMap<>();

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIResponse.Builder.builder().headers(headers).httpStatusCode(-1).build());

        /* test */
        assertEquals("HTTP status code is out of range. Must be between 0 and 600.", exception.getMessage());
    }
}