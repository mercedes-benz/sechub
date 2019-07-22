// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;
import java.util.UUID;

public interface ProductResultRepositoryCustom {

	/**
	 * Returns a list of product results which having only allowed identifiers. The method will be very interesting when it comes to reentrants for partly failed
	 * products results - reuse existing etc.
	 * 
	 * @param secHubJobUUID
	 * @param allowedIdentifiers
	 * @return list, never <code>null</code>
	 */
	List<ProductResult> findProductResults(UUID secHubJobUUID, ProductIdentifier... allowedIdentifiers);
}
