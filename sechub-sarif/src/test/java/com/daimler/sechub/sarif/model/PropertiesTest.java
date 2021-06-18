package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

class PropertiesTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());
        testBothAreEqualAndHaveSameHashCode(createExample(), change(createExample(), (properties) -> properties.setTags(new LinkedHashSet<>())));

        testBothAreNOTEqual(createExample(), change(createExample(), (properties) -> properties.setTags(Collections.singleton("something"))));
        /* @formatter:on */

    }

    private Properties createExample() {
        return new Properties();
    }

}
