package com.mercedesbenz.sechub.domain.scan.resolve;

import com.mercedesbenz.sechub.domain.scan.product.ProductResult;

public interface SpdxJsonResolver {

	/**
	 * Fetches SPDX-Json from Sereco ProductResult
	 * 
	 * @param serecoProductResult from Sereco
	 * @return SpdxJson as String or <code>null</code>
	 */
	String resolveSpdxJson(ProductResult serecoProductResult);

}