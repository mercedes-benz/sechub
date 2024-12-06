// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

@Component
public class ProductExecutorContextFactory {

    @Autowired
    ProductResultTransactionService transactionService;

    public ProductExecutorContext create(List<ProductResult> formerResults, SecHubExecutionContext sechubExecutionContext, ProductExecutor productExecutor,
            ProductExecutorConfig config) {

        ProductExecutorContext productExecutorContext = new ProductExecutorContext(config, formerResults);

        ProductExecutorCallbackImpl callback = new ProductExecutorCallbackImpl(sechubExecutionContext, productExecutorContext, transactionService);
        productExecutorContext.callback = callback;
        productExecutorContext.useFirstFormerResult();

        return productExecutorContext;
    }

}
