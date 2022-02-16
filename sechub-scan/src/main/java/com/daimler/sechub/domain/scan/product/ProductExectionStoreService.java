// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public interface ProductExectionStoreService {

    /**
     * Execute the registered products and store results
     *
     * @param context
     * @throws SecHubExecutionException
     */
    public void executeProductsAndStoreResults(SecHubExecutionContext context) throws SecHubExecutionException;
}
