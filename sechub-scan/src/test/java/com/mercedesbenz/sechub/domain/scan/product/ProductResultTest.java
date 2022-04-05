// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import java.util.UUID;

import org.junit.Test;

public class ProductResultTest {

    @Test
    public void test_equals_and_hashcode_correct_implemented() {
        /* prepare */
        ProductResult objectA = new ProductResult();
        objectA.uUID = UUID.randomUUID();

        ProductResult objectBequalToA = new ProductResult();
        objectBequalToA.uUID = objectA.uUID;

        ProductResult objectCnotEqualToAOrB = new ProductResult();
        objectCnotEqualToAOrB.uUID = UUID.randomUUID();

        /* test */
        testEqualsAndHashCodeCorrectImplemented(objectA, objectBequalToA, objectCnotEqualToAOrB);
    }

}
