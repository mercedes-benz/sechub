// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;

/**
 * A default product executor configuration info
 * 
 * @author Albert Tregnaghi
 *
 */
public class DefaultProductExecutorConfigInfo implements ProductExecutorConfigInfo {

    private ProductIdentifier productIdentifier;
    private UUID productExecutorConfigUUID;

    public DefaultProductExecutorConfigInfo(ProductIdentifier productIdentifier, UUID productExecutorConfigUUID) {
        if (productIdentifier == null) {
            throw new IllegalArgumentException("productIdentifier may not be null!");
        }
        if (productExecutorConfigUUID == null) {
            throw new IllegalArgumentException("productExecutorConfigUUID may not be null!");
        }
        this.productIdentifier = productIdentifier;
        this.productExecutorConfigUUID = productExecutorConfigUUID;
    }

    @Override
    public UUID getUUID() {
        return productExecutorConfigUUID;
    }

    @Override
    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

}
