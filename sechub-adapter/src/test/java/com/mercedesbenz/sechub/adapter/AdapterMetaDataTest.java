// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.MetaDataModel;

public class AdapterMetaDataTest {

    private AdapterMetaData metaDataToTest;

    @BeforeEach
    void before() {
        metaDataToTest = new AdapterMetaData();
    }

    @Test
    void adapter_version_initial_0() {
        assertEquals(0, metaDataToTest.getAdapterVersion());
    }

    @Test
    void adapter_version_set_get_22() {
        /* prepare */
        metaDataToTest.adapterVersion = 22;

        /* execute + test */
        assertEquals(22, metaDataToTest.getAdapterVersion());
    }

    /**
     * With this test we ensure we have the full functionality of
     * {@link MetaDataModel} (via inheritance)
     */
    @Test
    void adapter_meta_data_extends_metadata_model() {
        assertTrue(metaDataToTest instanceof MetaDataModel);
    }

    @Test
    void metadata_as_json_by_jsonconverter_is_as_expected() {
        /* prepare */
        metaDataToTest.adapterVersion = 4711;
        metaDataToTest.setValue("key1", 1);
        metaDataToTest.setValue("key2", "i-am-a-string");
        metaDataToTest.setValue("key3", true);

        /* execute */
        String json = JSONConverter.get().toJSON(metaDataToTest);

        /* test */
        String expectedJson = "{\"adapterVersion\":4711,\"metaData\":{\"key1\":\"1\",\"key2\":\"i-am-a-string\",\"key3\":\"true\"}}";
        assertEquals(expectedJson, json);
    }

}
