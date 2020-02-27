package com.daimler.sechub.domain.scan.product;

import static java.util.Objects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * Not a service but simple POJO - because stateful
 * @author Albert Tregnaghi
 *
 */
public class ProductExecutorCallbackImpl implements ProductExecutorCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorCallback.class);

    ProductResultTransactionService transactionService;
    ProductResult currentProductResult;

    private String projectId;

    private ProductIdentifier productIdentifier;

    private SecHubExecutionContext context;

    private AdapterMetaDataConverter metaDataConverter;
    
    public ProductExecutorCallbackImpl(SecHubExecutionContext context, ProductIdentifier productIdentifier, ProductResultTransactionService transactionService) {
        this.metaDataConverter=new AdapterMetaDataConverter();
        this.transactionService=transactionService;
        String projectId = context.getConfiguration().getProjectId();
        requireNonNull(projectId, "Project id must be set");
        this.projectId=projectId;
        this.productIdentifier=productIdentifier;
        this.context=context;
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
        result.setMetaData(getMetaDataConverter().convertToMetaDataStringOrNull(metaData));
        result = save(currentProductResult);
    }

    public AdapterMetaDataConverter getMetaDataConverter() {
        return metaDataConverter;
    }
    
    @Override
    public ProductResult save(ProductResult result) {
        if (result == null) {
            LOG.error("Product executor {} returned null as one of the results {}", productIdentifier, context.getTraceLogId());
            return null;
        }
        ProductResult newPProductResult = transactionService.persistResult(context.getTraceLogId(), result);
        return newPProductResult;
    }
    
    @Override
    public ProductResult getProductResult() {
        if (currentProductResult==null) {
            currentProductResult=new ProductResult(context.getSechubJobUUID(), projectId, productIdentifier, "");
        }
        return currentProductResult;
    }

    @Override
    public void setCurrentProductResult(ProductResult result) {
        this.currentProductResult=result;
        
    }

}
