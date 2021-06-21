// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ToolComponentTest {

    @Test
    void test_setter() {
        /* execute */
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setGuid("42")));
        testBothAreNOTEqual(createExample(), change(createExample(), (toolComponent) -> toolComponent.setName("name")));
        /* @formatter:on */

    }

    private ToolComponent createExample() {
        return new ToolComponent();
    }

}
