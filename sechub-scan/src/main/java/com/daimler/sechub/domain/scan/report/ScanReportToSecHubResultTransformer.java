// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public interface ScanReportToSecHubResultTransformer {

	SecHubResult transform(ProductResult result) throws SecHubExecutionException;

	boolean canTransform(ProductIdentifier productIdentifier);
	

}