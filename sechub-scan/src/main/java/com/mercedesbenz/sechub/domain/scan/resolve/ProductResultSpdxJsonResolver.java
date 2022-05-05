// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import com.mercedesbenz.sechub.domain.scan.product.ProductResult;

public interface ProductResultSpdxJsonResolver {

    /**
     * Fetches SPDX-Json from a ProductResult
     *
     * @param serecoProductResult from Sereco
     * @return SpdxJson as String or <code>null</code>
     */
    String resolveSpdxJson(ProductResult serecoProductResult);

}