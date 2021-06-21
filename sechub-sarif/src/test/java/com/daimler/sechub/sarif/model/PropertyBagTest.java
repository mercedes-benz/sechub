// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class PropertyBagTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreEqualAndHaveSameHashCode(createExample(), change(createExample(), (bag) -> bag.setAdditionalProperties(new HashMap<>())));

        testBothAreNOTEqual(createExample(), change(createExample(), (bag) -> bag.setAdditionalProperties(null)));
        /* @formatter:on */

    }

    @Test
    void test_adding_values() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.addAdditionalProperty(null, "value1");
        propertyBag.addAdditionalProperty("key1", null);
        propertyBag.addAdditionalProperty("key2", "value2");

        /* test */
        assertEquals(propertyBag.getAdditionalProperties().size(), 3);
    }

    private PropertyBag createExample() {
        return new PropertyBag();
    }
}
