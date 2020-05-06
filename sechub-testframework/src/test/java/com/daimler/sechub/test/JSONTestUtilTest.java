package com.daimler.sechub.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class JSONTestUtilTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void null_returns_empty_json() {
        assertEquals("{}", JSONTestUtil.toJSONContainingNullValues((Map)null));
    }

    @Test
    public void empty_map_returns_empty_json() {
        assertEquals("{}", JSONTestUtil.toJSONContainingNullValues(new HashMap<>()));
    }

    @Test
    public void key1_null_key2_value2_contains_both_entries_in_json() {
        /* prepare */
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("key1", null);
        map.put("key2", "value2");
        
        /* @formatter:off */
        /* execute + test */
        assertEquals("{\n" + 
                "\"key1\" : null,\n" + 
                "\"key2\" : \"value2\"\n" + 
                "}", JSONTestUtil.toJSONContainingNullValues(map));
        /* @formatter:on */
    }
    @Test
    public void key1_1L_key2_value2_contains_both_entries_in_json() {
        /* prepare */
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("key1", 1L);
        map.put("key2", "value2");
        
        /* @formatter:off */
        /* execute + test */
        assertEquals("{\n" + 
                "\"key1\" : 1,\n" + 
                "\"key2\" : \"value2\"\n" + 
                "}", JSONTestUtil.toJSONContainingNullValues(map));
        /* @formatter:on */
    }
    
    @Test
    public void key1_map_with_key3_key2_value2_contains_all_entries_in_json() {
        /* prepare */
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        
        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        
        map.put("key1", map2);
        map.put("key2", "value2");
        map2.put("key3", "value3");
        
        /* @formatter:off */
        /* execute + test */
        assertEquals("{\n" + 
                "\"key1\" : {\n" + 
                "\"key3\" : \"value3\"\n" + 
                "},\n" + 
                "\"key2\" : \"value2\"\n" + 
                "}", JSONTestUtil.toJSONContainingNullValues(map));
        /* @formatter:on */
    }
    
    @Test
    public void example_with_3_keys_one_null_at_end() {
        /* prepare */
        LinkedHashMap<String,Object> jsonBody = new LinkedHashMap<>();
        jsonBody.put("isPublic", "false");
        jsonBody.put("name", "PROJECT_ID");
        jsonBody.put("owningTeam", null);
        
        /* @formatter:off */
        /* execute + test */
        assertEquals("{\n" + 
                "\"isPublic\" : \"false\",\n" + 
                "\"name\" : \"PROJECT_ID\",\n" + 
                "\"owningTeam\" : null\n" + 
                "}", JSONTestUtil.toJSONContainingNullValues(jsonBody));
        /* @formatter:on */
    }
    
    

}
