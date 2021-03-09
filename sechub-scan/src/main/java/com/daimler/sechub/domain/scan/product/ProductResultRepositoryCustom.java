// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;
import java.util.UUID;

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigInfo;

public interface ProductResultRepositoryCustom {

	/**
	 * Returns a list of product results for given product executor configuration
	 * products results - reuse existing etc.
	 * 
	 * @param secHubJobUUID
	 * @param productExecutorConfigUUIDProvider
	 * @return list, never <code>null</code>
	 */
	List<ProductResult> findProductResults(UUID secHubJobUUID, ProductExecutorConfigInfo productExecutorConfigUUIDProvider);
	
	/**
     * Returns a list of product results which having only allowed identifiers. 
     * 
     * @param secHubJobUUID
     * @param allowedIdentifiers
     * @return list, never <code>null</code>
     */
    List<ProductResult> findAllProductResults(UUID secHubJobUUID, ProductIdentifier... allowedIdentifiers);
}
