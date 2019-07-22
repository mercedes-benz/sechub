// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import com.daimler.sechub.domain.scan.SecHubResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public interface ScanReportToSecHubResultTransformer {

	SecHubResult transform(String origin) throws SecHubExecutionException;

	boolean canTransform(ProductIdentifier productIdentifier);
	

}