// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

public class JSONConverterTest {

    private JSONConverter converterToTest;

    @BeforeEach
    void before() {
        converterToTest = new JSONConverter();
    }

    @Test
    void JSONMapper_is_created_by_mapper_factory() {
        try (MockedStatic<JsonMapperFactory> mockedFactory = mockStatic(JsonMapperFactory.class)) {
            /* prepare */
            mockedFactory.when(JsonMapperFactory::createMapper).thenReturn(null);

            /* execute */
            new JSONConverter();

            /* test */
            mockedFactory.verify(JsonMapperFactory::createMapper);
        }
    }

    @Test
    void fromJson_single_quotes_accepted() {
        /* prepare */
        String json = "{'collection':['alpha']}";

        /* execute */
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);

        /* test */
        assertTrue(result.collection.contains("alpha"));

    }

    @Test
    void fromJson_array_with_single_element_accepted_for_collection() {
        /* prepare */
        String json = "{\"collection\":[\"alpha\"]}";

        /* execute */
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);

        /* test */
        assertTrue(result.collection.contains("alpha"));

    }

    @Test // this jackson feature is necessary for some classes - e.g. SimpleMailMessage
    void fromJson_single_value_accepted_for_collection() {
        /* prepare */
        String json = "{\"collection\":\"alpha\"}";

        /* execute */
        CollectionTestClass result = converterToTest.fromJSON(CollectionTestClass.class, json);

        /* test */
        assertTrue(result.collection.contains("alpha"));

    }

    @Test
    void toJson_string_array_with_one_element_is_an_array() {
        /* prepare */
        ArrayTestClass origin = new ArrayTestClass();
        origin.stringArray = new String[] { "alpha" };

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

    @ParameterizedTest
    @EnumSource(TrafficLight.class)
    void fromJson_enum_as_lowercased_can_be_read(TrafficLight trafficLight) {
        /* prepare */
        TrafficLightEnumTestClass origin = new TrafficLightEnumTestClass();
        origin.trafficLight = trafficLight;

        String json = converterToTest.toJSON(origin);
        String jsonWithLoweredGreen = json.replaceAll(trafficLight.name(), trafficLight.name().toLowerCase());

        assertNotNull(json);
        assertTrue(jsonWithLoweredGreen.contains(trafficLight.name().toLowerCase()));
        assertFalse(jsonWithLoweredGreen.contains(trafficLight.name()));

        /* execute */
        TrafficLightEnumTestClass result = converterToTest.fromJSON(TrafficLightEnumTestClass.class, jsonWithLoweredGreen);

        /* test */
        assertEquals(trafficLight, result.trafficLight);

    }

    @Test
    void toJson_for_a_object_containing_a_local_date_must_work() {
        /* prepare */
        LocalDateTestClass testObject = new LocalDateTestClass();
        testObject.date = LocalDate.now();

        /* execute */
        String json = converterToTest.toJSON(testObject);

        /* test */
        assertNotNull(json);

    }

    @Test
    void toJson_and_from_json_local_date_must_be_equal() {
        /* prepare */
        LocalDateTestClass origin = new LocalDateTestClass();
        origin.date = LocalDate.now();

        /* execute */
        String json = converterToTest.toJSON(origin);

        /* test */
        assertNotNull(json);

        /* execute */
        LocalDateTestClass result = converterToTest.fromJSON(LocalDateTestClass.class, json);
        assertEquals(origin.date, result.date);

    }

    @Test
    void fromJson_with_array_localdatetime_works() {
        /* prepare */
        String json = "{\"dateTime\":[2023,3,7,10,38,49,470191000]}";

        /* execute */
        LocalDateTimeTestClass result = converterToTest.fromJSON(LocalDateTimeTestClass.class, json);

        assertNotNull(result);

        LocalDateTime dateTime = result.getDateTime();
        assertNotNull(dateTime);

        assertEquals(2023, dateTime.getYear());
        assertEquals(3, dateTime.getMonthValue());
        assertEquals(7, dateTime.getDayOfMonth());

        assertEquals(10, dateTime.getHour());
        assertEquals(38, dateTime.getMinute());
        assertEquals(49, dateTime.getSecond());
        assertEquals(470191000, dateTime.getNano());

    }

    @Test
    void fromJson_with_iso8601_localdatetime_works() {
        /* prepare */
        String json = "{\"dateTime\":\"2023-03-13T10:44:13\"}";

        /* execute */
        LocalDateTimeTestClass result = converterToTest.fromJSON(LocalDateTimeTestClass.class, json);

        assertNotNull(result);

        LocalDateTime dateTime = result.getDateTime();
        assertNotNull(dateTime);

        assertEquals(2023, dateTime.getYear());
        assertEquals(3, dateTime.getMonthValue());
        assertEquals(13, dateTime.getDayOfMonth());

        assertEquals(10, dateTime.getHour());
        assertEquals(44, dateTime.getMinute());
        assertEquals(13, dateTime.getSecond());
        assertEquals(0, dateTime.getNano());
    }

    @Test
    void toJson_and_from_json_local_datetime_must_be_equal() throws InterruptedException {
        /* prepare */
        LocalDateTimeTestClass origin = new LocalDateTimeTestClass();
        origin.dateTime = LocalDateTime.of(2023, 3, 14, 20, 27, 10, 123);

        /* execute */
        String json = converterToTest.toJSON(origin);
        System.out.println(json);
        /* test */
        assertNotNull(json);

        /* execute */
        LocalDateTimeTestClass result = converterToTest.fromJSON(LocalDateTimeTestClass.class, json);
        assertEquals(origin.dateTime, result.dateTime);

    }

    @Test
    void toJson_local_datetime_in_expected_format() {
        /* prepare */
        LocalDateTimeTestClass origin = new LocalDateTimeTestClass();
        origin.dateTime = LocalDateTime.parse("2023_03_07X17_08_17_36804000Z", DateTimeFormatter.ofPattern("yyyy_MM_dd'X'HH_mm_ss_n'Z'"));

        /* execute */
        String json = converterToTest.toJSON(origin);

        /* test */
        String expectedTimeDate = "2023-03-07T17:08:17.36804000Z";
        if (!json.contains(expectedTimeDate)) {
            fail("Expected time date:" + expectedTimeDate + "\n not found in:\n" + json);
        }

    }

    @Test
    void toJson_local_date_in_expected_format() {
        /* prepare */
        LocalDateTestClass origin = new LocalDateTestClass();
        origin.date = LocalDate.of(2023, 03, 07);

        /* execute */
        String json = converterToTest.toJSON(origin);

        /* test */
        assertTrue(json.contains("\"2023-03-07\""));

    }

    @Test
    void toJSON_list_with_two_test_object_returns_expected_json_string() throws Exception {
        assertEquals("[{\"info\":\"test1\"},{\"info\":\"test2\"}]",
                converterToTest.toJSON(Arrays.asList(new TestJSONConverterObject("test1"), new TestJSONConverterObject("test2"))));
    }

    @Test
    void fromJSON_list_correct_json_with_array_containing_two_test_objects_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "[{\"info\":\"test1\"},{\"info\":\"test2\"}]";

        /* execute */
        List<TestJSONConverterObject> result = converterToTest.fromJSONtoListOf(TestJSONConverterObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals(2, result.size());

        Iterator<TestJSONConverterObject> it = result.iterator();
        TestJSONConverterObject obj1 = it.next();
        TestJSONConverterObject obj2 = it.next();

        assertEquals("test1", obj1.getInfo());
        assertEquals("test2", obj2.getInfo());
    }

    @Test
    void toJSON_test_object_returns_expected_json_string() throws Exception {
        assertEquals("{\"info\":\"test1\"}", converterToTest.toJSON(new TestJSONConverterObject("test1")));
    }

    @Test
    void fromJSON_correct_json_with_double_quotes_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "{\"info\":\"test1\"}";
        /* execute */
        TestJSONConverterObject result = converterToTest.fromJSON(TestJSONConverterObject.class, json);
        /* test */
        assertEquals("test1", result.getInfo());
    }

    @Test
    void fromJSON_correct_json_with_single_quotes_results_in_expected_object() throws Exception {
        /* prepare */
        String json = "{'info':'info1'}";
        /* execute */
        TestJSONConverterObject result = converterToTest.fromJSON(TestJSONConverterObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals("info1", result.getInfo());
    }

    @Test
    void fromJSON_when_string_null_throws_JSONConverterException() throws Exception {
        assertThrows(JSONConverterException.class, () -> converterToTest.fromJSON(TestJSONConverterObject.class, null));
    }

    @Test
    void fromJSON_comments_are_allowed() throws Exception {
        /* prepare */
        String json = "//just a comment\\\n{\n//comments are a nice thing. \n/*not standard but used in *wildness* so we provide it*/\n'info':'info1'}";
        /* execute */
        TestJSONConverterObject result = converterToTest.fromJSON(TestJSONConverterObject.class, json);

        /* test */
        assertNotNull(result);
        assertEquals("info1", result.getInfo());
    }

    @Test
    void valid_enum_values_are_returned_as_enum() {
        /* prepare */
        String json = """
                {
                    "info":"info1",
                    "enumValue":"TEST1"
                }
                """;

        /* execute */
        TestJSONConverterObject result = converterToTest.fromJSON(TestJSONConverterObject.class, json);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getEnumValue()).isEqualTo(TestJSONConverterEnum.TEST1);
    }

    @Test
    void invalid_enum_values_are_returned_as_null() {
        /* prepare */
        String json = """
                {
                    "info":"info1",
                    "enumValue":"invalid_enum_value"
                }
                """;

        /* execute */
        TestJSONConverterObject result = converterToTest.fromJSON(TestJSONConverterObject.class, json);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getEnumValue()).isNull();
        assertThat(result.getInfo()).isEqualTo("info1");
    }

    static class LocalDateTestClass {
        private LocalDate date;

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    static class LocalDateTimeTestClass {
        private LocalDateTime dateTime;

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
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

    static class ArrayTestClass {
        private String[] stringArray;

        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }

        public String[] getStringArray() {
            return stringArray;
        }
    }

    static class CollectionTestClass {
        private Collection<String> collection = new ArrayList<>();

        public Collection<String> getCollection() {
            return collection;
        }

        public void setCollection(Collection<String> collection) {
            this.collection = collection;
        }
    }

}
