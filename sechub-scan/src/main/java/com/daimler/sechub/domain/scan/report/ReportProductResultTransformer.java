// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import com.daimler.sechub.domain.scan.ReportTransformationResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

/**
 * Implementations of this interface transforms a report product result (e.g.
 * SERECO) to a common report transformation result
 * 
 * @author Albert Tregnaghi
 *
 */
public interface ReportProductResultTransformer {

    /**
     * Transforms given report product result into common report ransformation
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