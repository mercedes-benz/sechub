// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ThreadFlowLocationTest {
    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setLocation(new Location())));
        /* @formatter:on */

    }

    private ThreadFlowLocation createExample() {
        ThreadFlowLocation threadFlowLocation = new ThreadFlowLocation();
        return threadFlowLocation;
    }

}
