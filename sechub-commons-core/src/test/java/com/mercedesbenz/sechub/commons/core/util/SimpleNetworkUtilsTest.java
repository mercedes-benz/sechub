// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class SimpleNetworkUtilsTest {

    @Test
    void isURIEmpty__null() {
        /* execute + test */
        assertTrue(SimpleNetworkUtils.isURINullOrEmpty(null));
    }

    @Test
    void isURIEmpty__empty_string() {
        /* execute + test */
        assertTrue(SimpleNetworkUtils.isURINullOrEmpty(URI.create("")));
    }

    @Test
    void isURIEmpty__string_with_space() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> {
            SimpleNetworkUtils.isURINullOrEmpty(URI.create(" "));
        });
    }

    @Test
    void isURIEmpty__uri() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isURINullOrEmpty(URI.create("https://example.org")));
    }

    @Test
    void isURIEmpty__string() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isURINullOrEmpty(URI.create("localhost")));
    }

    @Test
    void isURIEmpty__special_chars() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isURINullOrEmpty(URI.create("ραταρδιaẞßzüa")));
    }

    @Test
    void isHttpProtocol__http() {
        /* execute + test */
        assertTrue(SimpleNetworkUtils.isHttpProtocol(URI.create("http://localhost:3293/admin/login.php")));
    }

    @Test
    void isHttpProtocol__https() {
        /* execute + test */
        assertTrue(SimpleNetworkUtils.isHttpProtocol(URI.create("https://127.0.0.1:3293/admin/login.php")));
    }

    @Test
    void isHttpProtocol__null() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isHttpProtocol(null));
    }

    @Test
    void isHttpProtocol__empty_string() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isHttpProtocol(URI.create("")));
    }

    @Test
    void isHttpProtocol__no_schema() {
        /* execute + test */
        assertFalse(SimpleNetworkUtils.isHttpProtocol(URI.create("localhost")));
    }
}
