package com.daimler.sechub.sarif.model;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class PropertyBagTest {

    @Test
    public void test_setter() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.setAdditionalProperties(new HashMap<String, String>());

        /* test */
        assertTrue(propertyBag.getAdditionalProperties().isEmpty());
    }

    @Test
    public void test_adding_values() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.addAdditionalProperty(null, "value1");
        propertyBag.addAdditionalProperty("key1", null);
        propertyBag.addAdditionalProperty("key2", "value2");

        /* test */
        assertEquals(propertyBag.getAdditionalProperties().size(), 3);
    }

}
