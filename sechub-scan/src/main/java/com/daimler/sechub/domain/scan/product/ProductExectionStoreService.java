// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

/* FIXME Albert Tregnaghi, 2018-03-02: what about Transaction-Timeouts? Could happen on very long running operations and should be
 * considered! Maybe a simple @Transaction+Required on caller methods would solve this! */
public interface ProductExectionStoreService {

	/**
	 * Execute the registered products and store results
	 * 
	 * @param context
	 * @throws SecHubExecutionException
	 */
	public void executeProductsAndStoreResults(SecHubExecutionContext context) throws SecHubExecutionException;
}
