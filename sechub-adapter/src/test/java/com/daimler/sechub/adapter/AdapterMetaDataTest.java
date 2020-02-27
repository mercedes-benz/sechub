package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AdapterMetaDataTest {

    AdapterMetaData metaDataToTest;

    @Before
    public void before() {
        metaDataToTest = new AdapterMetaData();
    }

    @Test
    public void adapter_version_initial_0() {
        assertEquals(0, metaDataToTest.getAdapterVersion());
    }

    @Test
    public void adapter_version_set_get_22() {
        metaDataToTest.setAdapterVersion(22);
        assertEquals(22, metaDataToTest.getAdapterVersion());
    }

    @Test
    public void adapter_getvalue_for_unknown_key_returns_null() {
        assertEquals(null, metaDataToTest.getValue("xyz"));
        assertEquals(null, metaDataToTest.getValue(null));
        assertEquals(0, metaDataToTest.getKeys().size());
    }

    @Test
    public void adapter_getvalue_for_known_key_returns_value() {
        metaDataToTest.setValue("xyz", "123");
        assertEquals("123", metaDataToTest.getValue("xyz"));
        assertEquals(1, metaDataToTest.getKeys().size());
        assertTrue(metaDataToTest.getKeys().contains("xyz"));

    }
    
    @Test
    public void adapter_setvalue_with_null() {
        metaDataToTest.setValue("xyz", "123");
        metaDataToTest.setValue("xyz", null);
        assertEquals(null, metaDataToTest.getValue("xyz"));

    }
    @Test
    public void adapter_hasvalue_with_xyz_123() {
        metaDataToTest.setValue("xyz", "123");
        assertTrue(metaDataToTest.hasValue("xyz","123"));
        assertFalse(metaDataToTest.hasValue("xyz","1234"));
        
    }
    @Test
    public void adapter_hasvalue_with_null() {
        metaDataToTest.setValue("xyz", null);
        assertTrue(metaDataToTest.hasValue("xyz",null));
        assertFalse(metaDataToTest.hasValue("xyz","1234"));
        
    }

    @Test
    public void adapter_setvalue_with_key_null_is_same_as_string_null() {
        assertEquals(null, metaDataToTest.getValue(null));
        assertEquals(0, metaDataToTest.getKeys().size());

        metaDataToTest.setValue(null, "123");
        assertEquals("123", metaDataToTest.getValue(null));
        assertEquals("123", metaDataToTest.getValue("null"));
        assertEquals(1, metaDataToTest.getKeys().size());
        assertTrue(metaDataToTest.getKeys().contains("null"));

        metaDataToTest.setValue("null", "456");
        assertEquals("456", metaDataToTest.getValue(null));
        assertEquals("456", metaDataToTest.getValue("null"));
        assertEquals(1, metaDataToTest.getKeys().size());
        assertTrue(metaDataToTest.getKeys().contains("null"));

    }

}
