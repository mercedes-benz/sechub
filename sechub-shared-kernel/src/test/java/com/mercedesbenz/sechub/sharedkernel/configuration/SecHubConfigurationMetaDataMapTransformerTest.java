// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;

class SecHubConfigurationMetaDataMapTransformerTest {

    private SecHubConfigurationMetaDataMapTransformer transformerToTest;

    @BeforeEach
    void beforeEach() {
        transformerToTest = new SecHubConfigurationMetaDataMapTransformer();
    }

    @Test
    void map_null_leads_to_empty_labels() {
        /* execute */
        SecHubConfigurationMetaData result = transformerToTest.transform((Map<String, String>) null);

        /* test */
        Map<String, String> labels = result.getLabels();
        assertNotNull(labels);
        assertTrue(labels.isEmpty());
    }

    @Test
    void empty_map_leads_to_empty_labels() {
        /* execute */
        SecHubConfigurationMetaData result = transformerToTest.transform(new HashMap<>());

        /* test */
        Map<String, String> labels = result.getLabels();
        assertNotNull(labels);
        assertTrue(labels.isEmpty());
    }

    @Test
    void map_with_metadata_labels_leads_to_label_entries() {
        /* prepare */
        Map<String, String> map = new HashMap<>();
        map.put("metadata.labels.key1", "val1");
        map.put("metadata.labels.key2", null);
        map.put("metadata.labels.key3", "val3");

        /* execute */
        SecHubConfigurationMetaData result = transformerToTest.transform(map);

        /* test */
        Map<String, String> labels = result.getLabels();
        assertNotNull(labels);
        assertEquals("val1", labels.get("key1"));
        assertEquals(null, labels.get("key2"));
        assertEquals("val3", labels.get("key3"));
    }

    @Test
    void meta_data_null_leads_to_empty_map() {
        /* prepare */
        SecHubConfigurationMetaData metaData = null;

        /* execute */
        Map<String, String> result = transformerToTest.transform(metaData);

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    void meta_data_empty_labels_empty_leads_to_empty_map() {
        /* prepare */
        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();

        /* check precondition */
        assertTrue(metaData.getLabels().isEmpty());

        /* execute */
        Map<String, String> result = transformerToTest.transform(metaData);

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    void meta_data_with_labels_resuls_in_map_with_expected_label_entries() {
        /* prepare */
        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        metaData.getLabels().put("label1", "val1");
        metaData.getLabels().put("label2", "val2");

        /* execute */
        Map<String, String> result = transformerToTest.transform(metaData);

        /* test */
        assertNotNull(result);
        assertTrue(result.containsKey("metadata.labels.label1"));
        assertTrue(result.containsKey("metadata.labels.label2"));
        assertEquals("val1", result.get("metadata.labels.label1"));
        assertEquals("val2", result.get("metadata.labels.label2"));
        assertEquals(2, result.size());

    }

}
