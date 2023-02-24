// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class ProductIdentifierTest {

    @ParameterizedTest()
    @EnumSource(ProductIdentifier.class)
    @NullSource
    void fromString__enum_value(ProductIdentifier productIdentifier) {
        /* prepare */
        ProductIdentifier expected = productIdentifier == null ? ProductIdentifier.UNKNOWN : productIdentifier;
        String productIdName = productIdentifier == null ? null : productIdentifier.name();

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }

    @ParameterizedTest()
    @EnumSource(ProductIdentifier.class)
    void fromString__lower_case(ProductIdentifier productIdentifier) {
        /* prepare */
        ProductIdentifier expected = productIdentifier;
        String productIdName = productIdentifier.name().toLowerCase();

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }

    @Test
    void fromString__fantasy_type() {
        /* prepare */
        ProductIdentifier expected = ProductIdentifier.UNKNOWN;
        String productIdName = "fantasyProduct";

        /* execute */
        ProductIdentifier result = ProductIdentifier.fromString(productIdName);

        /* test */
        assertEquals(expected, result);
    }
}
