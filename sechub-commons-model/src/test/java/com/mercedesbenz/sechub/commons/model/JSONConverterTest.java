// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
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

}
