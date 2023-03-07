// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JSONConverterTest {

    private JSONConverter converterToTest;

    @BeforeEach
    void before() {
        converterToTest = new JSONConverter();
    }
    
    @Test
    void fromJson_single_quotes_accepted() {
        /* prepare*/
        String json = "{'collection':['alpha']}";
        
        /* execute*/
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);
        
        /* test */
        assertTrue(result.collection.contains("alpha"));

    }
    @Test
    void fromJson_array_with_single_element_accepted_for_collection() {
        /* prepare*/
        String json = "{\"collection\":[\"alpha\"]}";
        
        /* execute*/
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);
        
        /* test */
        assertTrue(result.collection.contains("alpha"));
        
    }
    
    @Test // this jackson feature is necessary for some classes - e.g. SimpleMailMessage
    void fromJson_single_value_accepted_for_collection() {
        /* prepare*/
        String json = "{\"collection\":\"alpha\"}";
        
        /* execute*/
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);
        
        /* test */
        assertTrue(result.collection.contains("alpha"));
        
    }
    
    @Test
    void toJson_string_array_with_one_element_is_an_array() {
        /* prepare */
        ArrayTestClass origin = new ArrayTestClass();
        origin.stringArray= new String[] {"alpha"};
        
        /* execute */
        String json = converterToTest.toJSON(origin);
        
        /* test */
        assertTrue(json.contains("["));
    }
    
    @Test
    void toJson_string_collection_with_one_element_is_an_array() {
        /* prepare */
        CollectionTestClass origin = new CollectionTestClass();
        origin.collection.add("alpha");
        
        /* execute */
        String json = converterToTest.toJSON(origin);
        
        /* test */
        assertTrue(json.contains("["));
    }
    
    @Test
    void toJson_enum_is_uppercased() {
        /* prepare */
        TrafficLightEnumTestClass origin = new TrafficLightEnumTestClass();
        origin.trafficLight = TrafficLight.GREEN;

        /* execute */
        String json = converterToTest.toJSON(origin);

        /* test */
        assertNotNull(json);
        assertTrue(json.contains("GREEN"));
        assertFalse(json.contains("green"));

    }
    
    @Test
    void fromJson_enum_as_lowercased_can_be_read() {
        /* prepare */
        TrafficLightEnumTestClass origin = new TrafficLightEnumTestClass();
        origin.trafficLight = TrafficLight.GREEN;
        
        String json = converterToTest.toJSON(origin);
        String jsonWithLoweredGreen =json.replaceAll("GREEN", "green");
        
        assertNotNull(json);
        assertTrue(jsonWithLoweredGreen.contains("green"));
        assertFalse(jsonWithLoweredGreen.contains("GREEN"));
        
        /* execute */
        TrafficLightEnumTestClass result = converterToTest.fromJSON(TrafficLightEnumTestClass.class, jsonWithLoweredGreen);
        
        /* test */
        assertEquals(TrafficLight.GREEN, result.trafficLight);
        
    }

    @Test
    void toJson_for_a_object_containing_a_local_date_must_work() {
        /* prepare */
        LocalDateTestClass testObject = new LocalDateTestClass();
        testObject.date1 = LocalDate.now();

        /* execute */
        String json = converterToTest.toJSON(testObject);

        /* test */
        assertNotNull(json);

    }

    @Test
    void toJson_and_from_json_local_date_must_be_equal() {
        /* prepare */
        LocalDateTestClass origin = new LocalDateTestClass();
        origin.date1 = LocalDate.now();

        /* execute */
        String json = converterToTest.toJSON(origin);

        /* test */
        assertNotNull(json);

        /* execute */
        LocalDateTestClass result = converterToTest.fromJSON(LocalDateTestClass.class, json);
        assertEquals(origin.date1, result.date1);

    }

    @Test
    void toJSON_list_with_two_test_object_returns_expected_json_string() throws Exception {
        assertEquals("[{\"info\":\"test1\"},{\"info\":\"test2\"}]",
                converterToTest.toJSON(Arrays.asList(new JSONConverterTestObject("test1"), new JSONConverterTestObject("test2"))));
    }

    @Test
    void fromJSON_list_correct_json_with_array_containing_two_test_objects_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "[{\"info\":\"test1\"},{\"info\":\"test2\"}]";

        /* execute */
        List<JSONConverterTestObject> result = converterToTest.fromJSONtoListOf(JSONConverterTestObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals(2, result.size());

        Iterator<JSONConverterTestObject> it = result.iterator();
        JSONConverterTestObject obj1 = it.next();
        JSONConverterTestObject obj2 = it.next();

        assertEquals("test1", obj1.getInfo());
        assertEquals("test2", obj2.getInfo());
    }

    @Test
    void toJSON_test_object_returns_expected_json_string() throws Exception {
        assertEquals("{\"info\":\"test1\"}", converterToTest.toJSON(new JSONConverterTestObject("test1")));
    }

    @Test
    void fromJSON_correct_json_with_double_quotes_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "{\"info\":\"test1\"}";
        /* execute */
        JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);
        /* test */
        assertEquals("test1", result.getInfo());
    }

    @Test
    void fromJSON_correct_json_with_single_quotes_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "{'info':'info1'}";
        /* execute */
        JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals("info1", result.getInfo());
    }

    @Test
    void fromJSON_when_string_null_throws_JSONConverterException() throws Exception {
        assertThrows(JSONConverterException.class, () -> converterToTest.fromJSON(JSONConverterTestObject.class, null));
    }

    @Test
    void fromJSON_comments_are_allowed() throws Exception {
        /* prepare */
        String json = "//just a comment\\\n{\n//comments are a nice thing. \n/*not standard but used in *wildness* so we provide it*/\n'info':'info1'}";
        /* execute */
        JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals("info1", result.getInfo());
    }

    static class LocalDateTestClass {
        private LocalDate date1;

        public void setDate1(LocalDate date1) {
            this.date1 = date1;
        }

        public LocalDate getDate1() {
            return date1;
        }
    }

    static class TrafficLightEnumTestClass {
        private TrafficLight trafficLight;

        public void setTrafficLight(TrafficLight trafficLight) {
            this.trafficLight = trafficLight;
        }

        public TrafficLight getTrafficLight() {
            return trafficLight;
        }
    }
    
    static class ArrayTestClass{
        private String[] stringArray;
        
        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }
        
        public String[] getStringArray() {
            return stringArray;
        }
    }
    
    static class CollectionTestClass{
        private Collection<String> collection= new ArrayList<>();
        public Collection<String> getCollection() {
            return collection;
        }
        
        public void setCollection(Collection<String> collection) {
            this.collection = collection;
        }
    }

}
