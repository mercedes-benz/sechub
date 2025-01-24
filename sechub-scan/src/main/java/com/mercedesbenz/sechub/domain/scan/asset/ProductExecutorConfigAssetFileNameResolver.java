package com.mercedesbenz.sechub.domain.scan.asset;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

public interface ProductExecutorConfigAssetFileNameResolver {

    /**
     * Tries to resolve asset file name for given executor configuration
     *
     * @param config the product executor configuration to inspect
     * @return asset file name or <code>null</code> if not resolvable
     */
    public String resolveAssetFilename(ProductExecutorConfig config);
}
