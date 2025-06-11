// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

public class ProductExecutorContext {

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorContext.class);

    List<ProductResult> formerResults = new ArrayList<>();
    List<ProductResult> results = new ArrayList<>();
    ProductExecutorCallback callback;

    private ProductExecutorConfig executorConfig;

    ProductExecutorContext(ProductExecutorConfig executorConfig, List<ProductResult> formerResults) {
        notNull(executorConfig, "executorConfig may not be null");
        notNull(formerResults, "formerResults may not be null");

        this.executorConfig = executorConfig;
        this.formerResults = formerResults;
    }

    /**
     * The returned configuration contains configuration setup which will be used by
     * executors - REMARK: some old executors will not use the configuration but
     * insist on environment variables (e.g. CHECKMARX V1, NESSUS V1, NETSPARKER V1)
     *
     * @return executor configuration, never <code>null</code>
     */
    public ProductExecutorConfig getExecutorConfig() {
        return executorConfig;
    }

    public ProductExecutorCallback getCallback() {
        return callback;
    }

    private ProductResult getFormerProductResultOrNull() {
        if (formerResults.size() > 0) {
            return formerResults.iterator().next();
        }
        return null;
    }

    public void useFirstFormerResult() {
        callback.setCurrentProductResult(getFormerProductResultOrNull());
    }

    public void useFirstFormerResultHavingMetaData(String key, URI uri) {
        useFirstFormerResultHavingMetaData(key, "" + uri);
    }

    public void useFirstFormerResultHavingMetaData(String key, String value) {
        LOG.debug("use first former result with key:{},value:{}", key, value);

        AdapterMetaDataConverter metaDataConverter = callback.getMetaDataConverter();

        for (ProductResult result : formerResults) {
            if (result == null) {
                continue;
            }
            String metaDataString = result.getMetaData();
            AdapterMetaData metaDataOrNull = metaDataConverter.convertToMetaDataOrNull(metaDataString);

            if (metaDataOrNull != null && metaDataOrNull.hasValue(key, value)) {
                callback.setCurrentProductResult(result);
                return;
            }
        }
        /* not found - ensure null */
        callback.setCurrentProductResult(null);
    }

    public ProductResult getCurrentProductResult() {
        return callback.getProductResult();
    }

    public AdapterMetaData getCurrentMetaDataOrNull() {
        return callback.getMetaDataOrNull();
    }

    public void persist(ProductResult productResult) {
        callback.save(productResult);
    }

}
