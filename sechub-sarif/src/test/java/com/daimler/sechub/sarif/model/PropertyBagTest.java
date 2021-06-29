// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

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
        testBothAreEqualAndHaveSameHashCode(createExample(), change(createExample(), (bag) -> bag.clear()));

        testBothAreNOTEqual(createExample(), change(createExample(), (bag) -> bag.put("key","value")));
        /* @formatter:on */

    }

    @Test
    void test_adding_tags_1() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.addTag("tag1");
        propertyBag.addTag("tag2");
        propertyBag.addTag("tag3");

        /* test */
        List<String> fetchedTags = propertyBag.fetchTags();
        assertEquals(1, propertyBag.size());
        assertTrue(fetchedTags.contains("tag1"));
        assertTrue(fetchedTags.contains("tag2"));
        assertTrue(fetchedTags.contains("tag3"));

    }

    @Test
    void test_adding_tags_2() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.put(null, "value1");
        propertyBag.put("key1", null);
        propertyBag.put("key2", "value2");

        List<String> list = new ArrayList<>();
        propertyBag.put("tags", list);
        list.add("value1");
        list.add("value2");
        propertyBag.addTag("tag1");

        /* test */
        assertEquals(propertyBag.size(), 4);
        List<String> fetchedTags = propertyBag.fetchTags();
        assertTrue(fetchedTags.contains("value1"));
        assertTrue(fetchedTags.contains("value2"));
        assertTrue(fetchedTags.contains("tag1"));

    }

    @Test
    void test_adding_values() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.put(null, "value1");
        propertyBag.put("key1", null);
        propertyBag.put("key2", "value2");

        List<String> list = new ArrayList<>();
        propertyBag.put("tags", list);
        list.add("value1");
        list.add("value2");

        /* test */
        assertEquals(propertyBag.size(), 4);
        List<String> fetchedTags = propertyBag.fetchTags();
        assertTrue(fetchedTags.contains("value1"));
        assertTrue(fetchedTags.contains("value2"));
    }

    private PropertyBag createExample() {
        return new PropertyBag();
    }
}
