// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.UUID;

public class ProductResultTestAccess {

    public static void setUUID(ProductResult productResult, UUID uuid) {
        productResult.uUID = uuid;
    }
}
