package com.mercedesbenz.sechub.domain.scan.asset;

import static java.util.Objects.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

@Service
public class ProductExecutorConfigAssetFileNameService {

    private List<ProductExecutorConfigAssetFileNameResolver> assetFileNameResolvers;

    public ProductExecutorConfigAssetFileNameService(List<ProductExecutorConfigAssetFileNameResolver> assetFileNameResolvers) {
        this.assetFileNameResolvers = requireNonNull(assetFileNameResolvers, "resolver list may not be null!");
        if (assetFileNameResolvers.size() == 0) {
            throw new IllegalStateException("An empty list of file name resolvers was injected. At least one must be available!");
        }
    }

    /**
     * Resolves the name for the asset file.
     *
     * @param config the configuration to inspect
     * @return name or <code>null</code> if none of the file name resolvers can
     *         provide a name.
     */
    public String resolveAssetFileName(ProductExecutorConfig config) {
        for (ProductExecutorConfigAssetFileNameResolver resolver : assetFileNameResolvers) {
            String fileName = resolver.resolveAssetFilename(config);
            if (fileName != null) {
                return fileName;
            }
        }
        return null;
    }

}
