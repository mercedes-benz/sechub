// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ReportingDescriptorReferenceTest {
    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (ref) -> ref.setId("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (ref) -> ref.setGuid("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (ref) -> ref.setToolComponent(new ToolComponentReference())));
        /* @formatter:on */

    }

    private ReportingDescriptorReference createExample() {
        return new ReportingDescriptorReference();
    }

}
