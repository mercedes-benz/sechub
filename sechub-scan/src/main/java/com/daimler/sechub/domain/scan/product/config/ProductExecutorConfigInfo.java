// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;

public interface ProductExecutorConfigInfo {
    /**
     * @return product identifier
     */
    public ProductIdentifier getProductIdentifier();
    
    /**
     * @return UUID of product executor configuration or <code>null</code> if not available
     */
    public UUID getUUID();
}
