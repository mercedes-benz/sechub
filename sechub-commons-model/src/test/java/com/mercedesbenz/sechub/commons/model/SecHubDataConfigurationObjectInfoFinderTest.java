// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecHubDataConfigurationObjectInfoFinderTest {

    private SecHubDataConfigurationObjectInfoFinder finderToTest;

    @BeforeEach
    void beforeEach() {
        finderToTest = new SecHubDataConfigurationObjectInfoFinder();
    }

    @Test
    void findDataObjectsByName_returns_empty_set_when_model_empty() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findDataObjectsByName_returns_empty_set_when_model_null() {
        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(null, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void findDataObjectsByName_returns_empty_set_when_set_of_names_null() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        
        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, null);
        
        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findDataObjectsByName_nothing_found_when_model_contains_data_with_other_name_only() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration sourceConfiguration = new SecHubSourceDataConfiguration();
        sourceConfiguration.setFileSystem(new SecHubFileSystemConfiguration());
        sourceConfiguration.setUniqueName("name2");

        data.getSources().add(sourceConfiguration);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void findDataObjectsByName_found_one_when_model_contains_data_with_correct_name_only() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration sourceConfiguration = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        sourceConfiguration.setFileSystem(fileSystem);
        sourceConfiguration.setUniqueName("name1");

        data.getSources().add(sourceConfiguration);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        SecHubDataConfigurationObjectInfo info = result.get(0);
        assertEquals(sourceConfiguration, info.getDataConfigurationObject());
    }

    @Test
    void findDataObjectsByName_found_one_when_model_contains_data_with_correct_name_and_another_one() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration sourceConfiguration1 = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        sourceConfiguration1.setFileSystem(fileSystem);
        sourceConfiguration1.setUniqueName("name1");

        SecHubSourceDataConfiguration sourceConfiguration2 = new SecHubSourceDataConfiguration();
        sourceConfiguration2.setFileSystem(new SecHubFileSystemConfiguration());
        sourceConfiguration2.setUniqueName("name2");

        data.getSources().add(sourceConfiguration1);
        data.getSources().add(sourceConfiguration2);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        SecHubDataConfigurationObjectInfo info = result.get(0);
        assertEquals(sourceConfiguration1, info.getDataConfigurationObject());
    }

    @Test
    void findDataObjectsByName_found_one_when_model_contains_data_with_correct_name_and_another_one_binary_variant() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubBinaryDataConfiguration binaryConfiguration1 = new SecHubBinaryDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        binaryConfiguration1.setFileSystem(fileSystem);
        binaryConfiguration1.setUniqueName("name1");

        SecHubBinaryDataConfiguration binaryConfiguration2 = new SecHubBinaryDataConfiguration();
        binaryConfiguration2.setFileSystem(new SecHubFileSystemConfiguration());
        binaryConfiguration2.setUniqueName("name2");

        data.getBinaries().add(binaryConfiguration1);
        data.getBinaries().add(binaryConfiguration2);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        SecHubDataConfigurationObjectInfo info = result.get(0);
        assertEquals(binaryConfiguration1, info.getDataConfigurationObject());
    }

    @Test
    void findDataObjectsByName_found_one_when_model_contains_data_with_correct_name_and_another_one_binary_and_source_mixed_variant1() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        SecHubSourceDataConfiguration sourceConfiguration1 = new SecHubSourceDataConfiguration();
        sourceConfiguration1.setFileSystem(fileSystem);
        sourceConfiguration1.setUniqueName("name1");

        SecHubBinaryDataConfiguration binaryConfiguration2 = new SecHubBinaryDataConfiguration();
        binaryConfiguration2.setFileSystem(new SecHubFileSystemConfiguration());
        binaryConfiguration2.setUniqueName("name2");

        data.getSources().add(sourceConfiguration1);
        data.getBinaries().add(binaryConfiguration2);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name1"));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        SecHubDataConfigurationObjectInfo info = result.get(0);
        assertEquals(sourceConfiguration1, info.getDataConfigurationObject());
    }

    @Test
    void findDataObjectsByName_found_one_when_model_contains_data_with_correct_name_and_another_one_binary_and_source_mixed_variant2() {
        /* prepare */
        SecHubConfigurationModel model = new SecHubConfigurationModel();

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        SecHubSourceDataConfiguration sourceConfiguration1 = new SecHubSourceDataConfiguration();
        sourceConfiguration1.setFileSystem(fileSystem);
        sourceConfiguration1.setUniqueName("name1");

        SecHubBinaryDataConfiguration binaryConfiguration2 = new SecHubBinaryDataConfiguration();
        binaryConfiguration2.setFileSystem(new SecHubFileSystemConfiguration());
        binaryConfiguration2.setUniqueName("name2");

        data.getSources().add(sourceConfiguration1);
        data.getBinaries().add(binaryConfiguration2);
        model.setData(data);

        /* execute */
        List<SecHubDataConfigurationObjectInfo> result = finderToTest.findDataObjectsByName(model, Collections.singleton("name2"));

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());
        SecHubDataConfigurationObjectInfo info = result.get(0);
        assertEquals(binaryConfiguration2, info.getDataConfigurationObject());
    }
}
