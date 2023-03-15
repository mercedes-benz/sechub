// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

/**
 * Implementations of this interface transforms a report product result (e.g.
 * SERECO) to a common report transformation result
 *
 * @author Albert Tregnaghi
 *
 */
public interface ReportProductResultTransformer {

    /**
     * Transforms given report product result into common report transformation
     * result
     *
     * @param result
     * @return
     * @throws SecHubExecutionException
     */
    ReportTransformationResult transform(ProductResult result) throws SecHubExecutionException;

    /**
     * @param productIdentifier
     * @return <code>true</code> when this transformer is able to transform the
     *         given product result
     */
    boolean canTransform(ProductIdentifier productIdentifier);

}