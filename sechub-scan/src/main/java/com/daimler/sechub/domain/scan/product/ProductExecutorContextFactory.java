// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

@Component
public class ProductExecutorContextFactory {

    @Autowired
    ProductResultTransactionService transactionService;
    
    public ProductExecutorContext create(List<ProductResult> formerResults, SecHubExecutionContext executionContext, ProductExecutor productExecutor, ProductExecutorConfig config) {
        
        ProductExecutorContext productExecutorContext = new ProductExecutorContext(config,formerResults);
        
        ProductExecutorCallbackImpl callback = new ProductExecutorCallbackImpl(executionContext, productExecutorContext, transactionService);
        productExecutorContext.callback=callback;
        productExecutorContext.afterCallbackSet();
        
        return productExecutorContext;
    }
    
}
