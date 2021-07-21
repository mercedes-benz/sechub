// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class ThreadFlowTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (threadFlow) -> threadFlow.setLocations(Collections.singletonList(new ThreadFlowLocation()))));
        /* @formatter:on */

    }

    private ThreadFlow createExample() {
        /* prepare */
        ThreadFlow run = new ThreadFlow();
        return run;
    }

}
