// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

@Component
public class ProductExecutorContextFactory {

    @Autowired
    ProductResultTransactionService transactionService;
    
    public ProductExecutorContext create(List<ProductResult> formerResults, SecHubExecutionContext context, ProductExecutor productExecutor) {
        
        return new ProductExecutorContext(formerResults,new ProductExecutorCallbackImpl(context, productExecutor.getIdentifier(), transactionService));
    }
    
}
