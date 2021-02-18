// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.util;

import static org.junit.Assert.*;

import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

public class TextToSortedMapConverterTest {

    private TextToSortedMapConverter converterToTest;

    @Before
    public void before() throws Exception {
        converterToTest = new TextToSortedMapConverter();
    }

    @Test
    public void a_null_map_is_converted_to_emtpy_string() {
        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText(null);

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void key1_value1_property_file_converted_map() {
        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("key1=value1");

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test
    public void newline_newLine_key1_value1_property_file_converted_map() {
        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("\n\nkey1=value1");

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test
    public void property_file_content_with_two_keys_is_converted() {
        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("key1=value1\nkey2=value2");

        /* test */
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    
    @Test
    public void property_file_content_with_two_keys_and_multiple_newlines_between_is_converted() {

        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("\n\nkey1=value1\n\nkey2=value2\n\n");

        /* test */
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }
    
    @Test
    public void key1_no_value__value2_no_key_key3_value3__only_key3_value3_recognized() {

        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("\n\nkey1=\n\n=value2\n\nkey3=value3");

        /* test */
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value3", result.get("key3"));
    }
    
    @Test
    public void text_line_a_line_with_equals_only_and_key3_value3__only_key3_value3_recognized() {

        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("\n\nkey1\n\n=\n\nkey3=value3");

        /* test */
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value3", result.get("key3"));
    }
    
    @Test
    public void key_and_value_are_trimmed() {

        /* execute */
        SortedMap<String, String> result = converterToTest.convertFromText("   key1   =    value1     ");

        /* test */
        assertNotNull(result);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
    }


}
