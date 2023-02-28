// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import java.util.UUID;

import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

/**
 * A special product executor configuration info - will always return null for
 * product configuration uuid because there is just no configuration available
 *
 * @author Albert Tregnaghi
 *
 */
public class WithoutProductExecutorConfigInfo implements ProductExecutorConfigInfo {

    private ProductIdentifier productIdentifier;

    public WithoutProductExecutorConfigInfo(ProductIdentifier productIdentifier) {
        if (productIdentifier == null) {
            throw new IllegalArgumentException("productIdentifier may not be null!");
        }
        this.productIdentifier = productIdentifier;
    }

    @Override
    public UUID getUUID() {
        // we always return null - there is no executor configuration available
        return null;
    }

    @Override
    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

}
