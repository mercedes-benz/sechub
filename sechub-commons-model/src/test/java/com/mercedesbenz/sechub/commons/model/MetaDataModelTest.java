// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetaDataModelTest {

    private MetaDataModel modelToTest;

    @BeforeEach
    void beforeEach() {
        modelToTest = new MetaDataModel();
    }

    @Test
    void getvalue_for_unknown_key_returns_null() {
        /* test */
        assertEquals(null, modelToTest.getValueAsStringOrNull("xyz"));
        assertEquals(null, modelToTest.getValueAsStringOrNull(null));
        assertEquals(0, modelToTest.getKeys().size());
    }

    @Test
    void getvalue_for_known_key_returns_value() {
        /* prepare */
        modelToTest.setValue("xyz", "123");

        /* test */
        assertEquals("123", modelToTest.getValueAsStringOrNull("xyz"));
        assertEquals(1, modelToTest.getKeys().size());
        assertTrue(modelToTest.getKeys().contains("xyz"));

    }

    @Test
    void setvalue_with_null() {
        /* prepare */
        modelToTest.setValue("xyz", "123");
        modelToTest.setValue("xyz", null);

        /* test */
        assertEquals(null, modelToTest.getValueAsStringOrNull("xyz"));

    }

    @Test
    void hasvalue_with_xyz_123() {
        /* prepare */
        modelToTest.setValue("xyz", "123");

        /* test */
        assertTrue(modelToTest.hasValue("xyz", "123"));
        assertFalse(modelToTest.hasValue("xyz", "1234"));

    }

    @Test
    void hasvalue_with_null() {
        /* prepare */
        modelToTest.setValue("xyz", null);

        /* test */
        assertTrue(modelToTest.hasValue("xyz", null));
        assertFalse(modelToTest.hasValue("xyz", "1234"));

    }

    @Test
    void setvalue_with_key_null_is_same_as_string_null() {
        /* test */
        assertEquals(null, modelToTest.getValueAsStringOrNull(null));
        assertEquals(0, modelToTest.getKeys().size());

        /* execute */
        modelToTest.setValue(null, "123");

        /* test */
        assertEquals("123", modelToTest.getValueAsStringOrNull(null));
        assertEquals("123", modelToTest.getValueAsStringOrNull("null"));
        assertEquals(1, modelToTest.getKeys().size());
        assertTrue(modelToTest.getKeys().contains("null"));

        /* execute */
        modelToTest.setValue("null", "456");

        /* test */
        assertEquals("456", modelToTest.getValueAsStringOrNull(null));
        assertEquals("456", modelToTest.getValueAsStringOrNull("null"));
        assertEquals(1, modelToTest.getKeys().size());
        assertTrue(modelToTest.getKeys().contains("null"));

    }

    @Test
    void metadata_as_json_by_jsonconverter_is_as_expected() {
        /* prepare */
        modelToTest.setValue("key1", 1);
        modelToTest.setValue("key2", "i-am-a-string");
        modelToTest.setValue("key3", true);

        /* execute */
        String json = JSONConverter.get().toJSON(modelToTest);

        /* test */
        String expectedJson = "{\"metaData\":{\"key1\":\"1\",\"key2\":\"i-am-a-string\",\"key3\":\"true\"}}";
        assertEquals(expectedJson, json);
    }

    @Test
    void metadata_as_json_can_be_read_via_json_converter() {
        /* prepare */
        String json = "{\"metaData\":{\"key1\":\"1\",\"key2\":\"i-am-a-string\",\"key3\":\"true\"}}";

        /* execute */
        MetaDataModel result = JSONConverter.get().fromJSON(MetaDataModel.class, json);

        /* test */
        assertEquals(Long.valueOf(1), result.getValueAsLongOrNull("key1"));
        assertEquals("i-am-a-string", result.getValueAsStringOrNull("key2"));
        assertEquals(true, result.getValueAsBoolean("key3"));

    }

    @Test
    void metadata_as_json_with_unknown_property_can_be_read_via_json_converter() {
        /* prepare */
        String json = "{\"unknown-property\" : true, \"metaData\":{\"key1\":\"1\",\"key2\":\"i-am-a-string\",\"key3\":\"true\"}}";

        /* execute */
        MetaDataModel result = JSONConverter.get().fromJSON(MetaDataModel.class, json);

        /* test */
        assertEquals(Long.valueOf(1), result.getValueAsLongOrNull("key1"));
        assertEquals("i-am-a-string", result.getValueAsStringOrNull("key2"));
        assertEquals(true, result.getValueAsBoolean("key3"));

    }

}
