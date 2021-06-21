// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ToolTest {

    @Test
    void test_setter() {
        /* prepare */
        Tool tool = createExample();

        /* execute */
        testSetterAndGetter(tool);
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (tool) -> tool.setDriver((new Driver()))));
        /* @formatter:on */

    }

    private Tool createExample() {
        return new Tool();
    }

}
