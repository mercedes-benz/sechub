// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.util;

import static org.junit.Assert.*;

import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

public class SortedMapToTextConverterTest {

    private SortedMapToTextConverter converterToTest;

    @Before
    public void before() throws Exception {
        converterToTest = new SortedMapToTextConverter();
    }

    @Test
    public void a_null_map_is_converted_to_emtpy_string() {
        /* execute */
        String result = converterToTest.convertToText(null);

        /* test */
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test

    public void a_map_with_key1_having_value1is_converted_is_converted_like_property_file() {
        /* prepare */
        TreeMap<String, String> map = new TreeMap<>();
        map.put("key1", "value1");

        /* execute */
        String result = converterToTest.convertToText(map);

        /* test */
        assertNotNull(result);
        assertEquals("key1=value1", result);
    }

    @Test
    public void a_map_with_key1_having_value1_and_key2_having_value2_is_converted_like_property_file() {
        /* prepare */
        TreeMap<String, String> map = new TreeMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        /* execute */
        String result = converterToTest.convertToText(map);

        /* test */
        assertNotNull(result);
        assertEquals("key1=value1\nkey2=value2", result);
    }

    
    @Test
    public void key_values_are_trimmed() {
        /* prepare */
        TreeMap<String, String> map = new TreeMap<>();
        map.put("  key1", "value1");
        map.put("key2", "     value2");

        /* execute */
        String result = converterToTest.convertToText(map);

        /* test */
        assertNotNull(result);
        assertEquals("key1=value1\nkey2=value2", result);
    }

    @Test
    public void a_map_with_key1_having_value1_and_key2_having_null_is_converted_like_property_file_key2_not_listed() {
        /* prepare */
        TreeMap<String, String> map = new TreeMap<>();
        map.put("key1", "value1");
        map.put("key2", null);

        /* execute */
        String result = converterToTest.convertToText(map);

        /* test */
        assertNotNull(result);
        assertEquals("key1=value1", result);
    }

}
