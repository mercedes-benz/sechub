// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class CodeFlowTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (codeFlow) -> codeFlow.setMessage(new Message("other"))));
        /* @formatter:on */

    }

    private CodeFlow createExample() {
        return new CodeFlow();
    }

}
