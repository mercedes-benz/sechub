// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;

public interface ProductExecutionStoreService {

    /**
     * Execute the registered products and store results
     *
     * @param context the execution context
     * @throws SecHubExecutionException
     */
    public void executeProductsAndStoreResults(SecHubExecutionContext context) throws SecHubExecutionException;

    /**
     * Every product execution store service is responsible for exact ONE scan type.
     *
     * @return the scan type this product execution store service is responsible
     *         for.
     */
    public ScanType getScanType();
}
