// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static java.util.Objects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * Not a service but simple POJO - because stateful
 * 
 * @author Albert Tregnaghi
 *
 */
public class ProductExecutorCallbackImpl implements ProductExecutorCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorCallback.class);

    ProductResultTransactionService transactionService;

    ProductResult currentProductResult;

    private String projectId;

    private ProductExecutorContext productExecutorContext;

    private SecHubExecutionContext context;

    private AdapterMetaDataConverter metaDataConverter;

    public ProductExecutorCallbackImpl(SecHubExecutionContext context, ProductExecutorContext productExecutorContext,
            ProductResultTransactionService transactionService) {
        requireNonNull(context, "context must be not null");
        requireNonNull(productExecutorContext, "productExecutorContext must be not null");

        SecHubConfiguration configuration = context.getConfiguration();
        requireNonNull(configuration, "configuration must be not null");

        String projectId = configuration.getProjectId();
        requireNonNull(projectId, "Project id must be set");

        this.metaDataConverter = new AdapterMetaDataConverter();
        this.transactionService = transactionService;
        this.projectId = projectId;
        this.context = context;
        this.productExecutorContext = productExecutorContext;
    }

    public AdapterMetaData getMetaDataOrNull() {
        ProductResult formerResult = getProductResult();
        if (formerResult == null) {
            return null;
        }
        String metaDataString = formerResult.getMetaData();
        if (metaDataString == null || metaDataString.isEmpty()) {
            return null;
        }
        return getMetaDataConverter().convertToMetaDataOrNull(metaDataString);
    }

    @Override
    public void persist(AdapterMetaData metaData) {
        ProductResult result = getProductResult();
        result.setMetaData(getMetaDataConverter().convertToJSONOrNull(metaData));
        result = save(currentProductResult);
        setCurrentProductResult(result);
    }

    public AdapterMetaDataConverter getMetaDataConverter() {
        return metaDataConverter;
    }

    @Override
    public ProductResult save(ProductResult result) {
        if (result == null) {
            LOG.error("Product executor {} returned null as a result {}", productExecutorContext.getExecutorConfig(), context.getTraceLogId());
            return null;
        }
        ProductResult newPProductResult = transactionService.persistResult(context.getTraceLogId(), result);
        setCurrentProductResult(newPProductResult);
        return newPProductResult;
    }

    @Override
    public ProductResult getProductResult() {
        if (currentProductResult == null) {
            currentProductResult = new ProductResult(context.getSechubJobUUID(), projectId, productExecutorContext.getExecutorConfig(), "");
        }
        return currentProductResult;
    }

    @Override
    public void setCurrentProductResult(ProductResult result) {
        this.currentProductResult = result;

    }

}
