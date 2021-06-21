// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ToolComponentReferenceTest {

    @Test
    void test_setter() {
        /* prepare */
        ToolComponentReference tool = createExample();

        /* execute */
        testSetterAndGetter(tool);
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (ref) -> ref.setGuid("42")));
        testBothAreNOTEqual(createExample(), change(createExample(), (ref) -> ref.setName("name42")));
        /* @formatter:on */

    }

    private ToolComponentReference createExample() {
        return new ToolComponentReference();
    }

}
