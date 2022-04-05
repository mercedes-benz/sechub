// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

public interface ProductIdentifiable {

    /**
     * Identifies the product which will be executed
     *
     * @return product identifier
     */
    public ProductIdentifier getIdentifier();
}
