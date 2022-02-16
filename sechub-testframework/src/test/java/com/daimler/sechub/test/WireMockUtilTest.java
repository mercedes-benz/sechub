// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;

import org.junit.Test;

public class WireMockUtilTest {

    @Test
    public void null_map_results_in_empty_string() {
        assertEquals("", WireMockUtil.toFormUrlEncoded(null));
    }

    @Test
    public void empty_map_results_in_empty_string() {
        assertEquals("", WireMockUtil.toFormUrlEncoded(new LinkedHashMap<>()));
    }

    @Test
    public void simple_key1_value1() {
        assertEquals("key1=value1", WireMockUtil.toFormUrlEncoded("key1", "value1"));
    }

    @Test
    public void simple_key1_null() {
        assertEquals("key1=", WireMockUtil.toFormUrlEncoded("key1", null));
    }

    @Test
    public void map_contains_key1_value1_key2_value2_results_in_url_string_with_this_ordering() {
        /* prepare */
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        /* execute + test */
        assertEquals("key1=value1&key2=value2", WireMockUtil.toFormUrlEncoded(map));
    }

    @Test
    public void map_contains_key2_value2_key1_value1_results_in_url_string_with_this_ordering() {
        /* prepare */
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("key2", "value2");
        map.put("key1", "value1");

        /* execute + test */
        assertEquals("key2=value2&key1=value1", WireMockUtil.toFormUrlEncoded(map));
    }

    @Test
    public void map_contains_key2_null_key1_value1_results_in_url_string_with_this_ordering() {
        /* prepare */
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("key2", null);
        map.put("key1", "value1");

        /* execute + test */
        assertEquals("key2=&key1=value1", WireMockUtil.toFormUrlEncoded(map));
    }

}
