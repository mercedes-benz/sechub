// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class ReportingDescriptorRelationshipTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (relationShip) -> relationShip.setTarget(new ReportingDescriptorReference())));
        /* @formatter:on */

    }

    private ReportingDescriptorRelationship createExample() {
        return new ReportingDescriptorRelationship();
    }

}
