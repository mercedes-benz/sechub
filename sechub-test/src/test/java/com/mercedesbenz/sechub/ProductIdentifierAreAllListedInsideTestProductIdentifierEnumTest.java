// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestProductExecutorIdentifier;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

class ProductIdentifierAreAllListedInsideTestProductIdentifierEnumTest {

    @Test
    void real_product_identifiers_are_available_in_test_product_identifiers() {

        for (ProductIdentifier identifier : ProductIdentifier.values()) {
            boolean found = false;
            for (TestProductExecutorIdentifier testIdentifier : TestProductExecutorIdentifier.values()) {
                if (identifier.name().equals(testIdentifier.name())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("The product identifier:" + identifier + " was not found in test product executor identifiers:" + TestProductExecutorIdentifier.values());
            }
        }

    }

}
