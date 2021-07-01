// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class PropertyBagTest {

    @Test
    void setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreEqualAndHaveSameHashCode(createExample(), change(createExample(), (bag) -> bag.clear()));

        testBothAreNOTEqual(createExample(), change(createExample(), (bag) -> bag.put("key","value")));
        /* @formatter:on */

    }

    @Test
    void add_tags_directly() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        /* execute */
        propertyBag.addTag("tag1");
        propertyBag.addTag("tag2");
        propertyBag.addTag("tag3");

        /* test */
        Set<String> fetchedTags = propertyBag.fetchTags();
        assertEquals(1, propertyBag.size());
        assertTrue(fetchedTags.contains("tag1"));
        assertTrue(fetchedTags.contains("tag2"));
        assertTrue(fetchedTags.contains("tag3"));

    }

    @Test
    void adding_tags_by_add_tag_method_when_already_containing_other_tags() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        // define some already existing tags
        Set<String> tagSet = new TreeSet<>();
        tagSet.add("value1");
        tagSet.add("value2");
        propertyBag.put("tags", tagSet);

        /* execute */
        propertyBag.addTag("tag1");

        /* test */
        assertEquals(1, propertyBag.size());
        Set<String> fetchedTags = propertyBag.fetchTags();
        assertTrue(fetchedTags.contains("value1"));
        assertTrue(fetchedTags.contains("value2"));
        assertTrue(fetchedTags.contains("tag1"));
        assertEquals(3, fetchedTags.size(), "Fetched tags size differ!");

    }

    @Test
    void adding_tags_by_add_tag_method_when_already_containing_same_tag() {
        /* prepare */
        PropertyBag propertyBag = new PropertyBag();

        // define some already existing tags
        propertyBag.put("tags", Collections.singleton("value1"));

        /* execute */
        propertyBag.addTag("tag1");
        boolean alreadyContainedTag = propertyBag.addTag("value1");

        /* test */
        assertFalse(alreadyContainedTag, "Value1 was NOT contained formerly? Wrong!");

    }

    private PropertyBag createExample() {
        return new PropertyBag();
    }
}
